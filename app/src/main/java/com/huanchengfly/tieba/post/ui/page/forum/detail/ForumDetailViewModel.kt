package com.huanchengfly.tieba.post.ui.page.forum.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.repository.ForumRepository
import com.huanchengfly.tieba.post.ui.models.forum.ForumDetail
import com.huanchengfly.tieba.post.ui.page.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForumDetailUiState(
    val isLoading: Boolean = true,
    val error: Throwable? = null,
    val detail: ForumDetail? = null,
    val introductionLoading: Boolean = false,
    val introductionError: Throwable? = null,
    val managersLoading: Boolean = false,
    val managersError: Throwable? = null,
): UiState

@HiltViewModel
class ForumDetailViewModel internal constructor(
    private val forumName: String,
    private val forumRepo: ForumRepository
) : ViewModel() {

    @Inject
    constructor(savedStateHandle: SavedStateHandle, forumRepo: ForumRepository) : this(
        savedStateHandle.toRoute<Destination.ForumDetail>().forumName,
        forumRepo,
    )

    private val _state: MutableStateFlow<ForumDetailUiState> = MutableStateFlow(ForumDetailUiState())
    val state: StateFlow<ForumDetailUiState> = _state.asStateFlow()
    private var introductionJob: Job? = null
    private var managersJob: Job? = null

    init {
        loadDetails()
    }

    fun reload() {
        if (!_state.value.isLoading) {
            loadDetails()
        }
    }

    private fun loadDetails() = viewModelScope.launch {
        introductionJob?.cancel()
        managersJob?.cancel()
        _state.update { ForumDetailUiState(isLoading = true) }
        try {
            val detail = forumRepo.loadForumDetail(forumName = forumName)
            _state.update { ForumDetailUiState(isLoading = false, detail = detail) }
            reloadIntroduction()
            reloadManagers()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.update { ForumDetailUiState(isLoading = false, error = e) }
        }
    }

    fun reloadIntroduction() {
        val forumId = state.value.detail?.id ?: return
        if (introductionJob?.isActive == true) return
        introductionJob = viewModelScope.launch {
            _state.update { it.copy(introductionLoading = true, introductionError = null) }
            try {
                val intro = forumRepo.loadForumIntroduction(forumId)
                _state.update { state ->
                    state.copy(
                        introductionLoading = false,
                        detail = state.detail?.let { detail ->
                            detail.copy(
                                slogan = intro.slogan.ifBlank { detail.slogan },
                                introRenders = intro.content,
                            )
                        },
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(introductionLoading = false, introductionError = e) }
            }
        }
    }

    fun reloadManagers() {
        val forumId = state.value.detail?.id ?: return
        if (managersJob?.isActive == true) return
        managersJob = viewModelScope.launch {
            _state.update { it.copy(managersLoading = true, managersError = null) }
            try {
                val groups = forumRepo.loadForumManagers(forumId)
                _state.update {
                    it.copy(managersLoading = false, detail = it.detail?.copy(managerGroups = groups))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(managersLoading = false, managersError = e) }
            }
        }
    }
}
