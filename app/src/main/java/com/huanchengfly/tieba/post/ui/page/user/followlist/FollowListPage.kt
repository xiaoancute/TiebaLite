package com.huanchengfly.tieba.post.ui.page.user.followlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.huanchengfly.tieba.post.LocalHabitSettings
import com.huanchengfly.tieba.post.PaddingNone
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.FollowListBean
import com.huanchengfly.tieba.post.arch.CommonUiEvent
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.collectUiEventWithLifecycle
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
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberSnackbarHostState
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import com.huanchengfly.tieba.post.utils.LocalAccount
import com.huanchengfly.tieba.post.utils.StringUtil
import kotlinx.collections.immutable.persistentListOf

private enum class FollowListFilter {
    All,
    Mutual,
}

@Composable
fun FollowListPage(
    uid: Long,
    navigator: NavController,
    viewModel: FollowListViewModel = pageViewModel(),
) {
    val account = LocalAccount.current
    val showActions = account?.uid == uid
    val requestUid = uid.takeUnless { showActions }
    val snackbarHostState = rememberSnackbarHostState()

    fun refresh() {
        viewModel.send(FollowListUiIntent.Refresh(requestUid))
    }

    com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad(loaded = viewModel.initialized) {
        refresh()
        viewModel.initialized = true
    }

    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = FollowListUiState::isRefreshing,
        initial = true,
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = FollowListUiState::isLoadingMore,
        initial = false,
    )
    val error by viewModel.uiState.collectPartialAsState(
        prop1 = FollowListUiState::error,
        initial = null,
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = FollowListUiState::currentPage,
        initial = 1,
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = FollowListUiState::hasMore,
        initial = false,
    )
    val totalFollowNum by viewModel.uiState.collectPartialAsState(
        prop1 = FollowListUiState::totalFollowNum,
        initial = 0,
    )
    val tipsText by viewModel.uiState.collectPartialAsState(
        prop1 = FollowListUiState::tipsText,
        initial = null,
    )
    val users by viewModel.uiState.collectPartialAsState(
        prop1 = FollowListUiState::users,
        initial = persistentListOf(),
    )
    val unfollowedIds by viewModel.uiState.collectPartialAsState(
        prop1 = FollowListUiState::unfollowedIds,
        initial = emptySet(),
    )
    val pendingUserIds by viewModel.uiState.collectPartialAsState(
        prop1 = FollowListUiState::pendingUserIds,
        initial = emptySet(),
    )

    viewModel.uiEventFlow.collectUiEventWithLifecycle { event ->
        if (event is CommonUiEvent.Toast) {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(event.message.toString())
        }
    }

    var filter by rememberSaveable { mutableStateOf(FollowListFilter.All) }
    val displayUsers = remember(users, filter) {
        when (filter) {
            FollowListFilter.All -> users
            FollowListFilter.Mutual -> users.filter { it.hasConcerned == 2 }
        }
    }
    val isEmpty by remember(displayUsers) { derivedStateOf { displayUsers.isEmpty() } }
    val lazyListState = rememberLazyListState()

    MyScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            TitleCentredToolbar(
                title = stringResource(R.string.title_follow_list),
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = navigator::navigateUp)
                },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            FollowListHeader(
                totalFollowNum = totalFollowNum,
                tipsText = tipsText,
                filter = filter,
                showFilter = showActions,
                onFilterChange = { filter = it },
            )

            StateScreen(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                                viewModel.send(
                                    FollowListUiIntent.LoadMore(requestUid, currentPage)
                                )
                            }.takeIf { hasMore },
                            preloadNextPage = LocalHabitSettings.current.preloadNextPage,
                            bottomIndicator = defaultBottomIndicator,
                        ) {
                            items(displayUsers, key = { it.id }) { item ->
                                val unfollowed = item.id in unfollowedIds
                                FollowListItem(
                                    item = item,
                                    showButton = showActions,
                                    followed = !unfollowed,
                                    pending = item.id in pendingUserIds,
                                    onClick = {
                                        navigator.navigateDebounced(
                                            Destination.UserProfile(
                                                uid = item.id,
                                                avatar = StringUtil.getAvatarUrl(item.portrait),
                                                nickname = item.nameShow,
                                                username = item.name,
                                                transitionKey = "follow-list-${item.id}",
                                            )
                                        )
                                    },
                                    onButtonClick = {
                                        val portrait = item.portrait
                                        val tbs = account?.tbs
                                        if (portrait != null && tbs != null) {
                                            if (unfollowed) {
                                                viewModel.send(
                                                    FollowListUiIntent.Follow(item.id, portrait, tbs)
                                                )
                                            } else {
                                                viewModel.send(
                                                    FollowListUiIntent.Unfollow(item.id, portrait, tbs)
                                                )
                                            }
                                        }
                                    },
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
private fun FollowListHeader(
    totalFollowNum: Int,
    tipsText: String?,
    filter: FollowListFilter,
    showFilter: Boolean,
    onFilterChange: (FollowListFilter) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.text_follow_list_count, totalFollowNum),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (showFilter) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = filter == FollowListFilter.All,
                        onClick = { onFilterChange(FollowListFilter.All) },
                        label = { Text(stringResource(R.string.filter_follow_all)) },
                    )
                    FilterChip(
                        selected = filter == FollowListFilter.Mutual,
                        onClick = { onFilterChange(FollowListFilter.Mutual) },
                        label = { Text(stringResource(R.string.filter_follow_mutual)) },
                    )
                }
            }
        }

        if (!tipsText.isNullOrBlank()) {
            Text(
                text = tipsText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FollowListItem(
    item: FollowListBean.FollowUserBean,
    showButton: Boolean,
    followed: Boolean,
    pending: Boolean,
    onClick: () -> Unit,
    onButtonClick: () -> Unit,
) {
    val displayName = item.nameShow.takeUnless { it.isNullOrBlank() } ?: item.name.orEmpty()
    val username = item.name.takeUnless { it.isNullOrBlank() || it == displayName }

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

            val supportingText = item.intro.takeUnless { it.isNullOrBlank() } ?: username
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

        if (showButton) {
            TextButton(
                onClick = onButtonClick,
                enabled = !pending,
            ) {
                Text(
                    text = stringResource(
                        when {
                            !followed -> R.string.button_follow
                            item.hasConcerned == 2 -> R.string.filter_follow_mutual
                            else -> R.string.text_followed
                        }
                    )
                )
            }
        }
    }
}
