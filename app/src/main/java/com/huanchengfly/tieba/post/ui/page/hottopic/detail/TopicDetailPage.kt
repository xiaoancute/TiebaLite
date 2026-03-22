package com.huanchengfly.tieba.post.ui.page.hottopic.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.RelateForumBean
import com.huanchengfly.tieba.post.api.models.ThreadInfoBean
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.getOrNull
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.ProvideNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.Container
import com.huanchengfly.tieba.post.ui.widgets.compose.ErrorScreen
import com.huanchengfly.tieba.post.ui.widgets.compose.MyLazyColumn
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.NetworkImage
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import com.huanchengfly.tieba.post.utils.StringUtil.getShortNumString
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.collections.immutable.persistentListOf

@Destination
@Composable
fun TopicDetailPage(
    topicId: String,
    topicName: String,
    navigator: DestinationsNavigator,
    viewModel: TopicDetailViewModel = pageViewModel<TopicDetailUiIntent, TopicDetailViewModel>(
        listOf(TopicDetailUiIntent.Load(topicId, topicName))
    ),
) {
    val isLoading by viewModel.uiState.collectPartialAsState(
        prop1 = TopicDetailUiState::isLoading,
        initial = true
    )
    val error by viewModel.uiState.collectPartialAsState(
        prop1 = TopicDetailUiState::error,
        initial = null
    )
    val topicInfo by viewModel.uiState.collectPartialAsState(
        prop1 = TopicDetailUiState::topicInfo,
        initial = null
    )
    val relatedForums by viewModel.uiState.collectPartialAsState(
        prop1 = TopicDetailUiState::relatedForums,
        initial = persistentListOf()
    )
    val specialTopics by viewModel.uiState.collectPartialAsState(
        prop1 = TopicDetailUiState::specialTopics,
        initial = persistentListOf()
    )
    val relatedThreads by viewModel.uiState.collectPartialAsState(
        prop1 = TopicDetailUiState::relatedThreads,
        initial = persistentListOf()
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = TopicDetailUiState::hasMore,
        initial = false
    )

    ProvideNavigator(navigator = navigator) {
        StateScreen(
            modifier = Modifier.fillMaxSize(),
            isEmpty = topicInfo == null,
            isError = error != null,
            isLoading = isLoading,
            onReload = {
                viewModel.send(TopicDetailUiIntent.Load(topicId, topicName))
            },
            errorScreen = { ErrorScreen(error = error.getOrNull()) }
        ) {
            MyScaffold(
                topBar = {
                    TitleCentredToolbar(
                        title = {
                            Text(
                                text = topicInfo?.get { topicName }.orEmpty().ifEmpty { topicName }
                            )
                        },
                        navigationIcon = {
                            BackNavigationIcon { navigator.navigateUp() }
                        }
                    )
                }
            ) { paddingValues ->
                MyLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        end = 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    topicInfo?.let { holder ->
                        item(key = "topic_header") {
                            val topic = holder.get()
                            Container {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    if (topic.topicImage.isNotBlank()) {
                                        NetworkImage(
                                            imageUri = topic.topicImage,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(2.39f)
                                                .clip(RoundedCornerShape(16.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Text(
                                        text = topic.topicName,
                                        style = MaterialTheme.typography.h5,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (topic.topicDesc.isNotBlank()) {
                                        Text(
                                            text = topic.topicDesc,
                                            style = MaterialTheme.typography.body1,
                                            color = ExtendedTheme.colors.textSecondary
                                        )
                                    }
                                    Text(
                                        text = stringResource(
                                            id = R.string.text_topic_discuss_num,
                                            topic.discussNum
                                        ),
                                        style = MaterialTheme.typography.caption,
                                        color = ExtendedTheme.colors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    if (relatedForums.isNotEmpty()) {
                        item(key = "related_forums_title") {
                            TopicSectionTitle(text = stringResource(id = R.string.title_related_forums))
                        }
                        items(
                            items = relatedForums,
                            key = { item -> item.get { forumId } },
                        ) { forum ->
                            Container {
                                TopicForumItem(
                                    forum = forum.get(),
                                    onClick = {
                                        navigator.navigate(ForumPageDestination(forum.get { forumName }))
                                    }
                                )
                            }
                        }
                    }

                    if (specialTopics.isNotEmpty()) {
                        item(key = "special_topics_title") {
                            TopicSectionTitle(text = stringResource(id = R.string.title_special_topics))
                        }
                        items(
                            items = specialTopics,
                            key = { item -> item.get { title } },
                        ) { specialTopic ->
                            Container {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = specialTopic.get { title },
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        specialTopic.get { threadList }.fastForEach { threadInfo ->
                                            TopicThreadItem(
                                                threadInfo = threadInfo,
                                                onClick = {
                                                    navigator.navigate(
                                                        ThreadPageDestination(
                                                            threadInfo.threadId.takeIf { it > 0 }
                                                                ?: threadInfo.id
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (relatedThreads.isNotEmpty()) {
                        item(key = "related_threads_title") {
                            TopicSectionTitle(text = stringResource(id = R.string.title_related_threads))
                        }
                        items(
                            items = relatedThreads,
                            key = { item -> item.get { threadId } },
                        ) { threadInfo ->
                            Container {
                                TopicThreadItem(
                                    threadInfo = threadInfo.get(),
                                    onClick = {
                                        navigator.navigate(
                                            ThreadPageDestination(
                                                threadInfo.get { threadId }.takeIf { it > 0 }
                                                    ?: threadInfo.get { id }
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }

                    if (hasMore) {
                        item(key = "topic_has_more") {
                            Container {
                                Text(
                                    text = stringResource(id = R.string.text_topic_more_available),
                                    style = MaterialTheme.typography.caption,
                                    color = ExtendedTheme.colors.textSecondary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicSectionTitle(text: String) {
    Container {
        Text(
            text = text,
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TopicForumItem(
    forum: RelateForumBean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            data = forum.avatar,
            size = Sizes.Small,
            contentDescription = null
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = forum.forumName,
                style = MaterialTheme.typography.subtitle2,
                fontWeight = FontWeight.Bold
            )
            if (forum.desc.isNotBlank()) {
                Text(
                    text = forum.desc,
                    style = MaterialTheme.typography.body2,
                    color = ExtendedTheme.colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(
                    id = R.string.text_topic_forum_stats,
                    forum.memberNum.getShortNumString(),
                    forum.threadNum.getShortNumString()
                ),
                style = MaterialTheme.typography.caption,
                color = ExtendedTheme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun TopicThreadItem(
    threadInfo: ThreadInfoBean,
    onClick: () -> Unit,
) {
    val meta = buildList {
        if (threadInfo.forumName.isNotBlank()) add("${threadInfo.forumName}吧")
        if (threadInfo.replyNum > 0) add("${threadInfo.replyNum.getShortNumString()} 回复")
        if (threadInfo.agreeNum > 0) add("${threadInfo.agreeNum.getShortNumString()} 赞")
        if (threadInfo.mediaNum.pic > 0) add("${threadInfo.mediaNum.pic} 图")
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ExtendedTheme.colors.windowBackground)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = threadInfo.title.ifBlank { threadInfo.abstractText },
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (threadInfo.abstractText.isNotBlank()) {
            Text(
                text = threadInfo.abstractText,
                style = MaterialTheme.typography.body2,
                color = ExtendedTheme.colors.textSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (meta.isNotEmpty()) {
            Text(
                text = meta.joinToString(" · "),
                style = MaterialTheme.typography.caption,
                color = ExtendedTheme.colors.textSecondary
            )
        }
    }
}
