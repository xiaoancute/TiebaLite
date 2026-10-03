package com.huanchengfly.tieba.post.ui.page.threadstore

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseStateViewModel
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.repository.ThreadStoreRepository
import com.huanchengfly.tieba.post.ui.models.ThreadStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class ThreadStoreUiState(
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    // The API uses a zero-based page index (offset = page * limit).
    val currentPage: Int = -1,
    val data: List<ThreadStore> = emptyList(),
    val error: Throwable? = null,
    val loadMoreError: Throwable? = null,
) : UiState {
    val isEmpty: Boolean get() = data.isEmpty()
}

internal fun List<ThreadStore>.searchCollections(query: String): List<ThreadStore> {
    val keyword = query.trim()
    if (keyword.isEmpty()) return this
    return filter {
        it.title.contains(keyword, ignoreCase = true) ||
            it.forumName.contains(keyword, ignoreCase = true) ||
            it.author.name.contains(keyword, ignoreCase = true) ||
            it.id.toString().contains(keyword)
    }
}

@HiltViewModel
class ThreadStoreViewModel @Inject constructor(
    private val threadStoreRepo: ThreadStoreRepository,
    private val savedStateHandle: SavedStateHandle,
) : BaseStateViewModel<ThreadStoreUiState>() {
    val query: StateFlow<String> = savedStateHandle.getStateFlow("collection_query", "")
    private var loadJob: Job? = null
    private var loadGeneration = 0
    private var retryRefresh = false
    private val deletingIds = mutableSetOf<Long>()

    init { onRefresh() }

    override fun createInitialState() = ThreadStoreUiState()

    fun onQueryChange(value: String) {
        savedStateHandle["collection_query"] = value
        if (value.isBlank()) {
            // A normal refresh may finish its first page; stop automatic search pagination.
            if (!currentState.isRefreshing && currentState.isLoadingMore) {
                loadGeneration++
                loadJob?.cancel()
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        } else if (currentState.loadMoreError == null) {
            onLoadMore()
        }
    }

    fun onRefresh() {
        if (!currentState.isRefreshing) startLoad(refresh = true)
    }

    fun onLoadMore() {
        if (currentState.isRefreshing || currentState.isLoadingMore || !currentState.hasMore) return
        startLoad(refresh = currentState.currentPage < 0)
    }

    fun onRetry() {
        if (retryRefresh) onRefresh() else onLoadMore()
    }

    private fun startLoad(refresh: Boolean) {
        val generation = ++loadGeneration
        loadJob?.cancel()
        _uiState.update {
            it.copy(isRefreshing = refresh, isLoadingMore = !refresh, error = null, loadMoreError = null)
        }
        loadJob = viewModelScope.launch {
            var replace = refresh
            var page = if (refresh) 0 else currentState.currentPage + 1
            try {
                do {
                    val incoming = threadStoreRepo.load(page = page)
                    ensureActive()
                    if (generation != loadGeneration) return@launch
                    val oldData = if (replace) emptyList() else currentState.data
                    val oldIds = oldData.mapTo(HashSet()) { it.id }
                    // A repeating full page must not turn into an endless search or a false completion.
                    check(replace || incoming.size < ThreadStoreRepository.LOAD_LIMIT ||
                        incoming.any { it.id !in oldIds }) { "Collection pagination made no progress" }
                    val hasMore = incoming.size >= ThreadStoreRepository.LOAD_LIMIT
                    val data = (oldData + incoming).distinctBy { it.id }
                    _uiState.update {
                        it.copy(
                            isRefreshing = false, isLoadingMore = false,
                            currentPage = page, data = data, hasMore = hasMore,
                            error = null, loadMoreError = null,
                        )
                    }
                    replace = false
                    if (query.value.isBlank() || !hasMore) break
                    page++
                    _uiState.update { it.copy(isLoadingMore = true) }
                    delay(100)
                } while (true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (generation == loadGeneration) {
                    retryRefresh = replace
                    _uiState.update {
                        it.copy(
                            error = e.takeIf { _ -> it.isEmpty && it.currentPage < 0 },
                            loadMoreError = e,
                        )
                    }
                }
            } finally {
                if (generation == loadGeneration) {
                    _uiState.update { it.copy(isRefreshing = false, isLoadingMore = false) }
                }
            }
        }
    }

    fun onDelete(thread: ThreadStore) {
        if (!deletingIds.add(thread.id)) return
        viewModelScope.launch {
            try {
                threadStoreRepo.remove(thread)
                    .onFailure { e ->
                        if (e is CancellationException) throw e
                        emitUiEvent(ThreadStoreUiEvent.Delete.Failure(e.getErrorMessage()))
                    }
                    .onSuccess {
                        // Keep pages loaded while the deletion was in flight.
                        _uiState.update { it.copy(data = it.data.filterNot { item -> item.id == thread.id }) }
                        // Deletion shifts server offsets, so rebuild the loaded range before paging again.
                        startLoad(refresh = true)
                        emitUiEvent(ThreadStoreUiEvent.Delete.Success)
                    }
            } finally {
                deletingIds.remove(thread.id)
            }
        }
    }

    fun onThreadResult(threadId: Long, markedPostId: Long?) {
        _uiState.update { state ->
            state.copy(data = state.data.mapNotNull {
                when {
                    it.id != threadId -> it
                    markedPostId == null -> null
                    else -> it.copy(markPid = markedPostId)
                }
            })
        }
        if (markedPostId == null) startLoad(refresh = true)
    }
}
