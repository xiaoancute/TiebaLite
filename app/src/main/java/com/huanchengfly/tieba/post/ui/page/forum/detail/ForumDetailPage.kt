package com.huanchengfly.tieba.post.ui.page.forum.detail

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.navigateDebounced
import com.huanchengfly.tieba.post.ui.models.forum.ForumDetail
import com.huanchengfly.tieba.post.ui.page.Destination
import com.huanchengfly.tieba.post.ui.page.ProvideNavigator
import com.huanchengfly.tieba.post.ui.page.photoview.PhotoViewActivity
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import com.huanchengfly.tieba.post.utils.StringUtil.getShortNumString

@Composable
fun ForumDetailPage(
    navigator: NavController,
    viewModel: ForumDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProvideNavigator(navigator) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.title_forum_info)) },
                    navigationIcon = { BackNavigationIcon(onBackPressed = navigator::navigateUp) },
                    actions = {
                        IconButton(onClick = viewModel::reload, enabled = !state.isLoading) {
                            Icon(Icons.Rounded.Refresh, stringResource(R.string.title_refresh))
                        }
                    },
                )
            },
        ) { padding ->
            StateScreen(
                modifier = Modifier.padding(padding),
                screenPadding = PaddingValues(0.dp),
                error = state.error,
                isLoading = state.isLoading,
                onReload = viewModel::reload,
            ) {
                val detail = state.detail ?: return@StateScreen
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "overview") { ForumDetailHeader(detail) }
                    item(key = "intro") {
                        IntroItem(
                            detail = detail,
                            loading = state.introductionLoading,
                            error = state.introductionError,
                            onRetry = viewModel::reloadIntroduction,
                        )
                    }
                    item(key = "rules") {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.title_forum_rule)) },
                            trailingContent = { Icon(Icons.Rounded.ChevronRight, null) },
                            modifier = Modifier.clickable {
                                navigator.navigateDebounced(Destination.ForumRuleDetail(detail.id))
                            },
                        )
                    }
                    item(key = "friends-title") {
                        SectionTitle(R.string.title_forum_friends)
                        if (detail.friendForums.isEmpty()) {
                            EmptySection(R.string.message_forum_friends_empty)
                        }
                    }
                    items(detail.friendForums, key = { "friend:" + it.name }) { friend ->
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.title_forum, friend.name)) },
                            leadingContent = { Avatar(data = friend.avatar, size = Sizes.Small) },
                            trailingContent = { Icon(Icons.Rounded.ChevronRight, null) },
                            modifier = Modifier.clickable {
                                navigator.navigateDebounced(
                                    Destination.Forum(forumName = friend.name, avatar = friend.avatar)
                                )
                            },
                        )
                    }
                    item(key = "managers-title") {
                        SectionTitle(R.string.title_forum_team)
                        SectionLoadState(
                            loading = state.managersLoading,
                            error = state.managersError,
                            errorText = R.string.message_forum_team_failed,
                            onRetry = viewModel::reloadManagers,
                        )
                        if (!state.managersLoading && state.managersError == null &&
                            detail.managerGroups.isEmpty()
                        ) {
                            EmptySection(R.string.message_forum_team_empty)
                        }
                    }
                    detail.managerGroups.forEachIndexed { groupIndex, group ->
                        item(key = "team-title:" + groupIndex) {
                            Text(
                                text = group.name.ifBlank { stringResource(R.string.title_forum_team) },
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                        items(group.members) { member ->
                            ListItem(
                                headlineContent = { Text(member.name) },
                                leadingContent = { Avatar(data = member.avatarUrl, size = Sizes.Small) },
                                trailingContent = {
                                    if (member.id > 0) Icon(Icons.Rounded.ChevronRight, null)
                                },
                                modifier = Modifier.clickable(enabled = member.id > 0) {
                                    navigator.navigateDebounced(Destination.UserProfile(member))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ForumDetailHeader(detail: ForumDetail) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                data = detail.avatar,
                size = Sizes.Medium,
                modifier = Modifier.clickable(enabled = detail.avatar.isNotBlank()) {
                    PhotoViewActivity.launchSinglePhoto(context, url = detail.avatar)
                },
            )
            Text(
                text = stringResource(R.string.title_forum, detail.name),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
        }
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatItem(detail.memberCount, R.string.text_stat_follow)
                VerticalDivider()
                StatItem(detail.threadCount, R.string.text_stat_threads)
                VerticalDivider()
                StatItem(detail.postCount, R.string.title_stat_posts_num)
            }
        }
    }
}

@Composable
private fun IntroItem(detail: ForumDetail, loading: Boolean, error: Throwable?, onRetry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(R.string.title_forum_intro)
        SelectionContainer {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (detail.slogan.isNotBlank()) {
                    Text(detail.slogan, style = MaterialTheme.typography.bodyLarge)
                }
                if (detail.introRenders.isNotEmpty()) {
                    detail.introRenders.forEach { it.Render() }
                } else if (!detail.intro.isNullOrBlank() && detail.intro != detail.slogan) {
                    Text(detail.intro, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        SectionLoadState(loading, error, R.string.message_forum_intro_failed, onRetry)
        if (!loading && error == null && detail.slogan.isBlank() &&
            detail.intro.isNullOrBlank() && detail.introRenders.isEmpty()
        ) {
            EmptySection(R.string.message_forum_intro_empty)
        }
    }
}

@Composable
private fun SectionTitle(@StringRes title: Int) {
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun EmptySection(@StringRes text: Int) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SectionLoadState(
    loading: Boolean,
    error: Throwable?,
    @StringRes errorText: Int,
    onRetry: () -> Unit,
) {
    if (loading) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Text(stringResource(R.string.message_forum_section_loading))
        }
    } else if (error != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(errorText),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.error,
            )
            TextButton(onClick = onRetry) { Text(stringResource(R.string.button_retry)) }
        }
    }
}

@Composable
private fun RowScope.StatItem(value: Long, @StringRes title: Int) {
    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.getShortNumString(), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(title), style = MaterialTheme.typography.labelMedium)
    }
}
