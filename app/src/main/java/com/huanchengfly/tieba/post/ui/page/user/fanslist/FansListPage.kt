package com.huanchengfly.tieba.post.ui.page.user.fanslist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.huanchengfly.tieba.post.LocalHabitSettings
import com.huanchengfly.tieba.post.PaddingNone
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.FansListBean
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.getOrNull
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.navigateDebounced
import com.huanchengfly.tieba.post.ui.page.Destination
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.Container
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.PullToRefreshBox
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.SwipeUpLazyLoadColumn
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.defaultBottomIndicator
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import com.huanchengfly.tieba.post.utils.StringUtil
import kotlinx.collections.immutable.persistentListOf

@Composable
fun FansListPage(
    uid: Long,
    navigator: NavController,
    viewModel: FansListViewModel = pageViewModel(),
) {
    fun refresh() {
        viewModel.send(FansListUiIntent.Refresh(uid))
    }

    com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad(loaded = viewModel.initialized) {
        refresh()
        viewModel.initialized = true
    }

    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = FansListUiState::isRefreshing,
        initial = true,
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = FansListUiState::isLoadingMore,
        initial = false,
    )
    val error by viewModel.uiState.collectPartialAsState(
        prop1 = FansListUiState::error,
        initial = null,
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = FansListUiState::currentPage,
        initial = 1,
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = FansListUiState::hasMore,
        initial = false,
    )
    val users by viewModel.uiState.collectPartialAsState(
        prop1 = FansListUiState::users,
        initial = persistentListOf(),
    )
    val isEmpty by remember(users) { derivedStateOf { users.isEmpty() } }
    val lazyListState = rememberLazyListState()

    MyScaffold(
        topBar = {
            TitleCentredToolbar(
                title = stringResource(R.string.title_fans_list),
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = navigator::navigateUp)
                },
            )
        },
    ) { contentPadding ->
        StateScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            isEmpty = isEmpty,
            isLoading = isRefreshing,
            error = error.getOrNull(),
            onReload = ::refresh,
            screenPadding = PaddingNone,
        ) {
            PullToRefreshBox(
                modifier = Modifier.fillMaxSize(),
                isRefreshing = isRefreshing,
                onRefresh = ::refresh,
            ) {
                Container {
                    SwipeUpLazyLoadColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = lazyListState,
                        isLoading = isLoadingMore,
                        onLazyLoad = {
                            viewModel.send(FansListUiIntent.LoadMore(uid, currentPage))
                        }.takeIf { hasMore },
                        preloadNextPage = LocalHabitSettings.current.preloadNextPage,
                        bottomIndicator = defaultBottomIndicator,
                    ) {
                        items(users, key = { it.id }) { item ->
                            FansListItem(
                                item = item,
                                onClick = {
                                    navigator.navigateDebounced(
                                        Destination.UserProfile(
                                            uid = item.id,
                                            avatar = StringUtil.getAvatarUrl(item.portrait),
                                            nickname = item.nameShow,
                                            username = item.name,
                                            transitionKey = "fans-list-${item.id}",
                                        )
                                    )
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
private fun FansListItem(
    item: FansListBean.FanUserBean,
    onClick: () -> Unit,
) {
    val displayName = item.nameShow.takeUnless { it.isNullOrBlank() } ?: item.name.orEmpty()
    val username = item.name.takeUnless { it.isNullOrBlank() || it == displayName }
    val supportingText = item.followFrom.takeUnless { it.isNullOrBlank() }
        ?: item.intro.takeUnless { it.isNullOrBlank() }
        ?: username

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Avatar(
            data = StringUtil.getAvatarUrl(item.portrait),
            size = Sizes.Medium,
            contentDescription = displayName,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
