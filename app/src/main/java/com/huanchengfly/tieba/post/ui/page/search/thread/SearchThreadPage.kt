package com.huanchengfly.tieba.post.ui.page.search.thread

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.onGlobalEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.UserProfilePageDestination
import com.huanchengfly.tieba.post.ui.page.reading.buildSearchThreadReadingTargetCandidate
import com.huanchengfly.tieba.post.ui.page.reading.isInLocalFavorite
import com.huanchengfly.tieba.post.ui.page.reading.isInReadLater
import com.huanchengfly.tieba.post.ui.page.reading.toggleLocalFavorite
import com.huanchengfly.tieba.post.ui.page.reading.toggleReadLater
import com.huanchengfly.tieba.post.ui.page.search.SearchUiEvent
import com.huanchengfly.tieba.post.ui.widgets.compose.ErrorScreen
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.LocalShouldLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.SearchThreadList
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchThreadPage(
    keyword: String,
    initialSortType: Int = SearchThreadSortType.SORT_TYPE_NEWEST,
    viewModel: SearchThreadViewModel = pageViewModel(),
) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(SearchThreadUiIntent.Refresh(keyword, initialSortType))
        viewModel.initialized = true
    }
    val currentKeyword by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::keyword,
        initial = ""
    )
    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::isRefreshing,
        initial = true
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::isLoadingMore,
        initial = false
    )
    val error by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::error,
        initial = null
    )
    val data by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::data,
        initial = persistentListOf()
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::currentPage,
        initial = 1
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::hasMore,
        initial = true
    )
    val sortType by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::sortType,
        initial = initialSortType
    )

    onGlobalEvent<SearchThreadUiEvent.SwitchSortType> {
        viewModel.send(SearchThreadUiIntent.Refresh(keyword, it.sortType))
    }
    val shouldLoad = LocalShouldLoad.current
    LaunchedEffect(currentKeyword) {
        if (currentKeyword.isNotEmpty() && keyword != currentKeyword) {
            if (shouldLoad) {
                viewModel.send(SearchThreadUiIntent.Refresh(keyword, sortType))
            } else {
                viewModel.initialized = false
            }
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.send(SearchThreadUiIntent.Refresh(keyword, sortType)) }
    )
    val lazyListState = rememberLazyListState()

    val isEmpty by remember {
        derivedStateOf { data.isEmpty() }
    }

    onGlobalEvent<SearchUiEvent.KeywordChanged> {
        viewModel.send(SearchThreadUiIntent.Refresh(it.keyword, sortType))
    }

    StateScreen(
        modifier = Modifier.fillMaxSize(),
        isEmpty = isEmpty,
        isError = error != null,
        isLoading = isRefreshing,
        onReload = { viewModel.send(SearchThreadUiIntent.Refresh(keyword, sortType)) },
        errorScreen = {
            error?.let {
                val (e) = it
                ErrorScreen(error = e)
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LoadMoreLayout(
                isLoading = isLoadingMore,
                onLoadMore = {
                    viewModel.send(
                        SearchThreadUiIntent.LoadMore(keyword, currentPage, sortType)
                    )
                },
                loadEnd = !hasMore,
                lazyListState = lazyListState,
            ) {
                SearchThreadList(
                    data = data,
                    lazyListState = lazyListState,
                    onItemClick = {
                        val threadId = it.tid.toLongOrNull()
                        if (threadId == null) {
                            context.toastShort(R.string.toast_load_failed)
                        } else {
                            navigator.navigate(
                                ThreadPageDestination(
                                    threadId = threadId
                                )
                            )
                        }
                    },
                    onItemUserClick = {
                        val userId = it.userId.toLongOrNull()
                        if (userId == null) {
                            context.toastShort(R.string.toast_load_failed)
                        } else {
                            navigator.navigate(UserProfilePageDestination(userId))
                        }
                    },
                    onItemForumClick = {
                        navigator.navigate(
                            ForumPageDestination(
                                it.forumName
                            )
                        )
                    },
                    searchKeyword = keyword,
                    itemMenuContent = { item ->
                        SearchThreadReadingMenu(item)
                    },
                )

                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = ExtendedTheme.colors.pullRefreshIndicator,
                    contentColor = ExtendedTheme.colors.primary,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.SearchThreadReadingMenu(
    item: SearchThreadBean.ThreadInfoBean,
) {
    val context = LocalContext.current
    val target = remember(item) { buildSearchThreadReadingTargetCandidate(item) }
    var isInReadLater by remember(target) { mutableStateOf(target?.isInReadLater() == true) }
    var isInLocalFavorite by remember(target) { mutableStateOf(target?.isInLocalFavorite() == true) }

    if (target == null) {
        return
    }

    DropdownMenuItem(
        onClick = {
            isInReadLater = target.toggleReadLater()
            context.toastShort(
                if (isInReadLater) {
                    R.string.message_added_to_read_later
                } else {
                    R.string.message_removed_from_read_later
                }
            )
        }
    ) {
        Text(
            text = context.getString(
                if (isInReadLater) {
                    R.string.action_remove_read_later
                } else {
                    R.string.action_add_read_later
                }
            )
        )
    }
    DropdownMenuItem(
        onClick = {
            isInLocalFavorite = target.toggleLocalFavorite()
            context.toastShort(
                if (isInLocalFavorite) {
                    R.string.message_added_to_local_favorite
                } else {
                    R.string.message_removed_from_local_favorite
                }
            )
        }
    ) {
        Text(
            text = context.getString(
                if (isInLocalFavorite) {
                    R.string.action_remove_local_favorite
                } else {
                    R.string.action_add_local_favorite
                }
            )
        )
    }
}
