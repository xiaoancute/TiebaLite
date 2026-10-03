package com.huanchengfly.tieba.post.ui.page.threadstore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.huanchengfly.tieba.post.LocalHabitSettings
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.arch.CommonUiEvent
import com.huanchengfly.tieba.post.arch.collectUiEventWithLifecycle
import com.huanchengfly.tieba.post.navigateDebounced
import com.huanchengfly.tieba.post.ui.models.Author
import com.huanchengfly.tieba.post.ui.models.ThreadStore
import com.huanchengfly.tieba.post.ui.page.Destination
import com.huanchengfly.tieba.post.ui.page.Destination.Thread
import com.huanchengfly.tieba.post.ui.page.Destination.UserProfile
import com.huanchengfly.tieba.post.ui.page.consumeResult
import com.huanchengfly.tieba.post.ui.page.thread.ThreadFrom
import com.huanchengfly.tieba.post.ui.page.thread.ThreadResult
import com.huanchengfly.tieba.post.ui.page.thread.ThreadResultKey
import com.huanchengfly.tieba.post.ui.page.thread.ThreadSortType
import com.huanchengfly.tieba.post.ui.widgets.compose.RecordSearchField
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreIndicator
import com.huanchengfly.tieba.post.ui.widgets.compose.LocalSnackbarHostState
import com.huanchengfly.tieba.post.ui.widgets.compose.LongClickMenu
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.PullToRefreshBox
import com.huanchengfly.tieba.post.ui.widgets.compose.SharedTransitionUserHeader
import com.huanchengfly.tieba.post.ui.widgets.compose.SwipeUpLazyLoadColumn
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen

@Composable
fun ThreadStorePage(
    navigator: NavController,
    viewModel: ThreadStoreViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searching = query.isNotBlank()
    val results = remember(state.data, query) { state.data.searchCollections(query) }
    val listState = key(query.trim()) { rememberLazyListState() }

    MyScaffold(
        topBar = {
            Column {
                TitleCentredToolbar(
                    title = stringResource(R.string.title_my_collect),
                    navigationIcon = { BackNavigationIcon(onBackPressed = navigator::navigateUp) },
                )
                RecordSearchField(
                    query = query,
                    onQueryChange = viewModel::onQueryChange,
                    placeholder = stringResource(R.string.hint_search_collections),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                if (searching || state.loadMoreError != null) {
                    val message = when {
                        state.loadMoreError != null -> stringResource(
                            R.string.tip_collection_search_incomplete, state.data.size, results.size
                        )
                        state.hasMore || state.isRefreshing -> stringResource(
                            R.string.tip_collection_search_loading, state.data.size, results.size
                        )
                        else -> stringResource(
                            R.string.tip_collection_search_complete, state.data.size, results.size
                        )
                    }
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (state.loadMoreError != null) {
                        TextButton(onClick = viewModel::onRetry) {
                            Text(stringResource(R.string.button_record_search_retry))
                        }
                    }
                }
            }
        },
    ) { contentPadding ->
        val context = LocalContext.current
        val snackbarHostState = LocalSnackbarHostState.current

        viewModel.uiEvent.collectUiEventWithLifecycle { event ->
            val message = when (event) {
                is ThreadStoreUiEvent -> event.toMessage(context)
                is CommonUiEvent.Toast -> event.message.toString()
                else -> null
            }
            if (message is String) {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(message)
            }
        }

        StateScreen(
            isLoading = state.isRefreshing && state.isEmpty,
            error = state.error,
            onReload = viewModel::onRefresh,
            screenPadding = contentPadding,
        ) {
            val habit = LocalHabitSettings.current
            val onUserClicked: (Author, String) -> Unit = { author, extraKey ->
                val route = author.run { UserProfile(id, avatarUrl, name, transitionKey = extraKey) }
                navigator.navigateDebounced(route)
            }
            val onThreadClicked: (ThreadStore) -> Unit = { thread ->
                navigator.navigateDebounced(
                    route = Thread(
                        threadId = thread.id,
                        postId = thread.markPid,
                        seeLz = habit.favoriteSeeLz,
                        sortType = if (habit.favoriteDesc) ThreadSortType.BY_DESC else ThreadSortType.DEFAULT,
                        from = ThreadFrom.Store(maxPid = thread.maxPid, maxFloor = thread.postNo)
                    )
                )
            }
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::onRefresh,
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
            ) {
                SwipeUpLazyLoadColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = contentPadding,
                    isLoading = state.isLoadingMore,
                    onLoad = viewModel::onLoadMore,
                    onLazyLoad = viewModel::onLoadMore.takeIf {
                        state.hasMore && state.loadMoreError == null && !searching
                    },
                    preloadNextPage = habit.preloadNextPage,
                    bottomIndicator = {
                        LoadMoreIndicator(noMore = !state.hasMore, onThreshold = it)
                    }
                ) {
                    if (results.isEmpty() && !state.hasMore && !state.isRefreshing) {
                        item {
                            Text(
                                text = stringResource(
                                    if (searching) R.string.tip_record_search_empty else R.string.tip_records_empty
                                ),
                                modifier = Modifier.padding(24.dp),
                            )
                        }
                    }
                    items(items = results, key = { it.id }) { info ->
                        StoreItem(
                            info = info,
                            onUserClick = onUserClicked,
                            onClick = onThreadClicked,
                            onDelete = viewModel::onDelete
                        )
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            navigator.consumeResult<Destination.ThreadStore, ThreadResult>(ThreadResultKey)?.run {
                viewModel.onThreadResult(threadId, markedPostId)
            }
        }
    }
}

@Composable
private fun StoreItem(
    info: ThreadStore,
    onUserClick: (Author, transitionKey: String) -> Unit,
    onDelete: (ThreadStore) -> Unit,
    onClick: (ThreadStore) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasUpdate = info.count != 0 && info.postNo != 0

    LongClickMenu(
        menuContent = {
            TextMenuItem(text = R.string.title_collect_on, onClick = { onDelete(info) })
        },
        onClick = { onClick(info) }
    ) {
        val colorScheme = MaterialTheme.colorScheme
        Column(
            modifier = modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SharedTransitionUserHeader(
                user = info.author,
                extraKey = info.id,
                desc = if (hasUpdate) {
                    stringResource(id = R.string.tip_thread_store_update, info.postNo)
                } else {
                    null
                },
                onClick = { onUserClick(info.author, info.id.toString()) },
            ) {
                Spacer(Modifier.weight(1.0f))

                Surface (
                    shape = MaterialTheme.shapes.extraSmall,
                    color = colorScheme.secondaryContainer,
                ) {
                    Text(
                        text = stringResource(id = R.string.title_forum_name, info.forumName),
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            Text(
                text = info.title,
                color = if (info.isDeleted) colorScheme.outlineVariant else colorScheme.onSurface,
                fontSize = 15.sp,
                textDecoration = if (info.isDeleted) TextDecoration.LineThrough else null
            )

            if (info.isDeleted) {
                Text(
                    text = stringResource(id = R.string.tip_thread_store_deleted),
                    fontSize = 12.sp,
                    color = colorScheme.outlineVariant
                )
            }
        }
    }
}