package com.huanchengfly.tieba.post.ui.page.forum.threadlist

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.arch.BaseStateViewModel
import com.huanchengfly.tieba.post.arch.CommonUiEvent
import com.huanchengfly.tieba.post.arch.TbLiteExceptionHandler
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.arch.emitGlobalEventSuspend
import com.huanchengfly.tieba.post.arch.stateInViewModel
import com.huanchengfly.tieba.post.repository.ExploreRepository.Companion.distinctById
import com.huanchengfly.tieba.post.repository.ForumRepository
import com.huanchengfly.tieba.post.repository.PbPageRepository
import com.huanchengfly.tieba.post.repository.user.SettingsRepository
import com.huanchengfly.tieba.post.ui.models.Like
import com.huanchengfly.tieba.post.ui.models.ThreadItem
import com.huanchengfly.tieba.post.ui.models.forum.NavTab
import com.huanchengfly.tieba.post.ui.models.settings.ForumSortType
import com.huanchengfly.tieba.post.ui.page.forum.threadlist.ForumThreadListViewModel.Companion.ForumVMFactory
import com.huanchengfly.tieba.post.ui.page.main.explore.concern.ConcernViewModel.Companion.updateLikeStatus
import com.huanchengfly.tieba.post.ui.page.main.explore.concern.ConcernViewModel.Companion.updateLikeStatusUiStateCommon
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlin.math.min

@Stable
@HiltViewModel(assistedFactory = ForumVMFactory::class)
class ForumThreadListViewModel @AssistedInject constructor(
    @Assisted val forumName: String,
    @Assisted val forumId: Long,
    @Assisted val tab: NavTab,
    private val forumRepo: ForumRepository,
    private val threadRepo: PbPageRepository,
    settingsRepo: SettingsRepository,
) : BaseStateViewModel<ForumThreadListUiState>() {

    override val errorHandler = TbLiteExceptionHandler(TAG) { _, e, suppressed ->
        if (suppressed && currentState.threads.isNotEmpty()) {
            _uiState.update { it.copy(isRefreshing = false, isLoadingMore = false, error = null) }
            sendUiEvent(CommonUiEvent.ToastError(e))
        } else {
            _uiState.update { it.copy(isRefreshing = false, isLoadingMore = false, error = e) }
        }
    }

    private val sortTypeFlow: Flow<Int>? =
        if (tab.supportsSorting) forumRepo.getSortType(forumName) else null

    override fun createInitialState(): ForumThreadListUiState = ForumThreadListUiState(isRefreshing = true)

    val hideBlocked: StateFlow<Boolean> = settingsRepo.blockSettings
        .map { it.hideBlocked }
        .stateInViewModel(initialValue = true)

    init {
        launchInVM { loadInternal(sortType = null, subClassifyId = null) }
    }

    private suspend fun loadInternal(sortType: Int?, subClassifyId: Int?, forceNew: Boolean = false) {
        _uiState.update { it.copy(isRefreshing = true) }
        val effectiveSort = sortType ?: sortTypeFlow?.first() ?: 0
        val data = forumRepo.loadByTab(
            forum = forumName,
            page = 1,
            sortType = effectiveSort,
            tab = tab,
            subClassifyId = subClassifyId,
            forceNew = forceNew,
        )
        _uiState.update {
            ForumThreadListUiState(
                subClassifyId = it.subClassifyId,
                threads = data.threads,
                threadIds = data.threadIds,
                currentPage = 1,
                hasMore = data.hasMore,
            )
        }
    }

    fun onSubClassifyIdChanged(classifyId: Int) {
        if (!tab.isEssence) return
        val state = _uiState.updateAndGet { it.copy(subClassifyId = classifyId) }
        if (state.isRefreshing) return
        launchInVM {
            loadInternal(sortType = null, subClassifyId = classifyId, forceNew = classifyId != 0)
        }
    }

    fun onSortTypeChanged(@ForumSortType sortType: Int?) {
        if (!tab.supportsSorting) return
        if (currentState.isRefreshing) return
        launchInVM {
            loadInternal(sortType = sortType, subClassifyId = null, forceNew = false)
        }
    }

    fun onRefresh() {
        if (currentState.isRefreshing) return
        launchInVM {
            loadInternal(
                sortType = sortTypeFlow?.first(),
                subClassifyId = if (tab.isEssence) (currentState.subClassifyId ?: 0) else null,
                forceNew = true,
            )
        }
    }

    fun loadMore() {
        val state = currentState
        if (state.isLoadingMore) return else _uiState.update { it.copy(isLoadingMore = true) }

        launchInVM {
            val effectiveSort = sortTypeFlow?.first() ?: 0
            if (state.threadIds.isNotEmpty()) {
                val size = min(state.threadIds.size, 30)
                val threadIds = state.threadIds.subList(0, size)
                val newList = forumRepo.threadList(forumId, forumName, state.currentPage, effectiveSort, threadIds)
                val threadList = (state.threads + newList).distinctById()

                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        isLoadingMore = false,
                        threads = threadList,
                        threadIds = state.threadIds.drop(size),
                        hasMore = threadList.isNotEmpty()
                    )
                }
            } else {
                val page = state.currentPage + 1
                val data = forumRepo.loadMoreByTab(
                    forum = forumName,
                    page = page,
                    sortType = effectiveSort,
                    tab = tab,
                    subClassifyId = if (tab.isEssence) state.subClassifyId else null,
                )
                val threadList = (state.threads + data.threads).distinctById()
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        isLoadingMore = false,
                        threads = threadList,
                        threadIds = data.threadIds,
                        currentPage = page,
                        hasMore = data.hasMore
                    )
                }
            }
        }
    }

    fun onThreadLikeClicked(thread: ThreadItem) = launchInVM {
        updateLikeStatusUiStateCommon(
            thread = thread,
            onRequestLikeThread = threadRepo::requestLikeThread,
            onEvent = ::emitGlobalEventSuspend
        ) { threadId, liked, loading ->
            _uiState.update { it.copy(threads = it.threads.updateLikeStatus(threadId, liked, loading)) }
        }
    }

    fun onThreadResult(threadId: Long, like: Like): Unit = launchInVM {
        val newThreads = currentState.threads.updateLikeStatus(threadId, like)
        if (newThreads != null) {
            _uiState.update { it.copy(threads = newThreads) }
        }
    }

    companion object {
        private const val TAG = "ForumThreadListViewMode"

        @AssistedFactory
        interface ForumVMFactory {
            fun create(forumName: String, forumId: Long, tab: NavTab): ForumThreadListViewModel
        }
    }
}

data class ForumThreadListUiState(
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val subClassifyId: Int? = null,
    val threads: List<ThreadItem> = emptyList(),
    val threadIds: List<Long> = emptyList(),
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val error: Throwable? = null,
) : UiState

sealed interface ForumThreadListUiEvent : UiEvent {
    data class SortTypeChanged(val sortType: Int) : ForumThreadListUiEvent

    data class ClassifyChanged(val tabId: Int, val subClassifyId: Int) : ForumThreadListUiEvent

    data class Refresh(val tabId: Int) : ForumThreadListUiEvent
}
