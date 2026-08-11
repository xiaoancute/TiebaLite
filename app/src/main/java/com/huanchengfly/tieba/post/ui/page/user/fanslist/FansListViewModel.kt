package com.huanchengfly.tieba.post.ui.page.user.fanslist

import androidx.compose.runtime.Immutable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.FansListBean
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.arch.wrapImmutable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
class FansListViewModel @Inject constructor() :
    BaseViewModel<FansListUiIntent, FansListPartialChange, FansListUiState, UiEvent>() {

    override fun createInitialState(): FansListUiState = FansListUiState()

    override fun createPartialChangeProducer():
        PartialChangeProducer<FansListUiIntent, FansListPartialChange, FansListUiState> =
        FansListPartialChangeProducer

    override fun dispatchEvent(partialChange: FansListPartialChange): UiEvent? = null

    private object FansListPartialChangeProducer :
        PartialChangeProducer<FansListUiIntent, FansListPartialChange, FansListUiState> {

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<FansListUiIntent>): Flow<FansListPartialChange> =
            merge(
                intentFlow.filterIsInstance<FansListUiIntent.Refresh>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<FansListUiIntent.LoadMore>()
                    .flatMapConcat { it.toPartialChangeFlow() },
            )

        private fun FansListUiIntent.Refresh.toPartialChangeFlow(): Flow<FansListPartialChange.Refresh> =
            TiebaApi.getInstance().fansListFlow(uid = uid)
                .map<FansListBean, FansListPartialChange.Refresh> {
                    FansListPartialChange.Refresh.Success(
                        page = 1,
                        hasMore = it.page.hasMore == 1,
                        users = it.userList,
                    )
                }
                .onStart { emit(FansListPartialChange.Refresh.Start) }
                .catch { emit(FansListPartialChange.Refresh.Failure(it)) }

        private fun FansListUiIntent.LoadMore.toPartialChangeFlow(): Flow<FansListPartialChange.LoadMore> =
            TiebaApi.getInstance().fansListFlow(page = page + 1, uid = uid)
                .map<FansListBean, FansListPartialChange.LoadMore> {
                    FansListPartialChange.LoadMore.Success(
                        page = page + 1,
                        hasMore = it.page.hasMore == 1,
                        users = it.userList,
                    )
                }
                .onStart { emit(FansListPartialChange.LoadMore.Start) }
                .catch { emit(FansListPartialChange.LoadMore.Failure(it)) }
    }
}

sealed interface FansListUiIntent : UiIntent {
    data class Refresh(val uid: Long) : FansListUiIntent

    data class LoadMore(
        val uid: Long,
        val page: Int,
    ) : FansListUiIntent
}

sealed interface FansListPartialChange : PartialChange<FansListUiState> {
    sealed class Refresh : FansListPartialChange {
        override fun reduce(oldState: FansListUiState): FansListUiState = when (this) {
            Start -> oldState.copy(isRefreshing = true)

            is Success -> oldState.copy(
                isRefreshing = false,
                error = null,
                currentPage = page,
                hasMore = hasMore,
                users = users.toImmutableList(),
            )

            is Failure -> oldState.copy(
                isRefreshing = false,
                error = error.wrapImmutable(),
            )
        }

        data object Start : Refresh()

        data class Success(
            val page: Int,
            val hasMore: Boolean,
            val users: List<FansListBean.FanUserBean>,
        ) : Refresh()

        data class Failure(val error: Throwable) : Refresh()
    }

    sealed class LoadMore : FansListPartialChange {
        override fun reduce(oldState: FansListUiState): FansListUiState = when (this) {
            Start -> oldState.copy(isLoadingMore = true)

            is Success -> oldState.copy(
                isLoadingMore = false,
                error = null,
                currentPage = page,
                hasMore = hasMore,
                users = (oldState.users + users).distinctBy { it.id }.toImmutableList(),
            )

            is Failure -> oldState.copy(
                isLoadingMore = false,
                error = error.wrapImmutable(),
            )
        }

        data object Start : LoadMore()

        data class Success(
            val page: Int,
            val hasMore: Boolean,
            val users: List<FansListBean.FanUserBean>,
        ) : LoadMore()

        data class Failure(val error: Throwable) : LoadMore()
    }
}

@Immutable
data class FansListUiState(
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: ImmutableHolder<Throwable>? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = false,
    val users: ImmutableList<FansListBean.FanUserBean> = persistentListOf(),
) : UiState
