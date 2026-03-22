package com.huanchengfly.tieba.post.ui.page.hottopic.detail

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.RelateForumBean
import com.huanchengfly.tieba.post.api.models.SpecialTopicBean
import com.huanchengfly.tieba.post.api.models.ThreadInfoBean
import com.huanchengfly.tieba.post.api.models.TopicDetailBean
import com.huanchengfly.tieba.post.api.models.TopicInfoBean
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.arch.wrapImmutable
import com.huanchengfly.tieba.post.revival.PublicBrowsePayloadGuard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@Stable
@HiltViewModel
class TopicDetailViewModel @Inject constructor() :
    BaseViewModel<TopicDetailUiIntent, TopicDetailPartialChange, TopicDetailUiState, UiEvent>() {
    override fun createInitialState(): TopicDetailUiState = TopicDetailUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<TopicDetailUiIntent, TopicDetailPartialChange, TopicDetailUiState> =
        TopicDetailPartialChangeProducer

    private object TopicDetailPartialChangeProducer :
        PartialChangeProducer<TopicDetailUiIntent, TopicDetailPartialChange, TopicDetailUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<TopicDetailUiIntent>): Flow<TopicDetailPartialChange> =
            merge(
                intentFlow.filterIsInstance<TopicDetailUiIntent.Load>()
                    .flatMapConcat { it.producePartialChange() }
            )

        private fun TopicDetailUiIntent.Load.producePartialChange(): Flow<TopicDetailPartialChange.Load> =
            TiebaApi.getInstance()
                .topicDetailFlow(topicId, topicName, page)
                .map<TopicDetailBean, TopicDetailPartialChange.Load> { response ->
                    val payload = PublicBrowsePayloadGuard.requireTopicDetailPayload(response)
                    TopicDetailPartialChange.Load.Success(
                        topicInfo = payload.topicInfo,
                        relatedForums = payload.relatedForums,
                        specialTopics = payload.specialTopics,
                        relatedThreads = payload.relatedThreads,
                        hasMore = payload.hasMore
                    )
                }
                .onStart { emit(TopicDetailPartialChange.Load.Start) }
                .catch { emit(TopicDetailPartialChange.Load.Failure(it)) }
    }
}

sealed interface TopicDetailUiIntent : UiIntent {
    data class Load(
        val topicId: String,
        val topicName: String,
        val page: Int = 1,
    ) : TopicDetailUiIntent
}

sealed interface TopicDetailPartialChange : PartialChange<TopicDetailUiState> {
    sealed class Load : TopicDetailPartialChange {
        override fun reduce(oldState: TopicDetailUiState): TopicDetailUiState =
            when (this) {
                Start -> oldState.copy(
                    isLoading = true,
                    error = null
                )

                is Success -> oldState.copy(
                    isLoading = false,
                    error = null,
                    topicInfo = topicInfo.wrapImmutable(),
                    relatedForums = relatedForums.wrapImmutable(),
                    specialTopics = specialTopics.wrapImmutable(),
                    relatedThreads = relatedThreads.wrapImmutable(),
                    hasMore = hasMore
                )

                is Failure -> oldState.copy(
                    isLoading = false,
                    error = error.wrapImmutable()
                )
            }

        data object Start : Load()

        data class Success(
            val topicInfo: TopicInfoBean,
            val relatedForums: List<RelateForumBean>,
            val specialTopics: List<SpecialTopicBean>,
            val relatedThreads: List<ThreadInfoBean>,
            val hasMore: Boolean,
        ) : Load()

        data class Failure(val error: Throwable) : Load()
    }
}

data class TopicDetailUiState(
    val isLoading: Boolean = true,
    val error: ImmutableHolder<Throwable>? = null,
    val topicInfo: ImmutableHolder<TopicInfoBean>? = null,
    val relatedForums: ImmutableList<ImmutableHolder<RelateForumBean>> = persistentListOf(),
    val specialTopics: ImmutableList<ImmutableHolder<SpecialTopicBean>> = persistentListOf(),
    val relatedThreads: ImmutableList<ImmutableHolder<ThreadInfoBean>> = persistentListOf(),
    val hasMore: Boolean = false,
) : UiState
