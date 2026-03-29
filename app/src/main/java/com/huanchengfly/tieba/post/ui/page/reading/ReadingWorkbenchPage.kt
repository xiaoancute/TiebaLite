package com.huanchengfly.tieba.post.ui.page.reading

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.models.ReadingTargetType
import com.huanchengfly.tieba.post.models.database.LocalFavoriteItem
import com.huanchengfly.tieba.post.models.database.ReadLaterItem
import com.huanchengfly.tieba.post.models.database.ReadingProgress
import com.huanchengfly.tieba.post.ui.page.main.home.ContinueReadingEntryCandidate
import com.huanchengfly.tieba.post.ui.page.main.home.buildContinueReadingEntries
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.LongClickMenu
import com.huanchengfly.tieba.post.ui.widgets.compose.MyLazyColumn
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.PagerTabIndicator
import com.huanchengfly.tieba.post.ui.widgets.compose.TabRow
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.UserHeader
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import com.huanchengfly.tieba.post.utils.DateTimeUtils
import com.huanchengfly.tieba.post.utils.LocalFavoriteUtil
import com.huanchengfly.tieba.post.utils.ReadLaterUtil
import com.huanchengfly.tieba.post.utils.ReadingProgressUtil
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

private data class ReadingWorkbenchSavedItem(
    val key: String,
    val targetType: Int,
    val threadId: Long,
    val forumName: String,
    val title: String,
    val subtitle: String?,
    val avatar: String?,
    val username: String?,
    val timestamp: Long,
)

private data class ContinueReadingUiItem(
    val threadId: Long,
    val title: String,
    val forumName: String?,
    val authorName: String?,
    val authorAvatar: String?,
    val postId: Long,
    val floor: Int,
    val seeLz: Boolean,
    val timestamp: Long,
)

private fun loadContinueReadingUiItems(): List<ContinueReadingUiItem> {
    val progressByThreadId = ReadingProgressUtil.getAll()
        .associateBy { it.threadId }
    return buildContinueReadingEntries(
        entries = progressByThreadId.values.map {
            ContinueReadingEntryCandidate(
                threadId = it.threadId,
                title = it.threadTitle,
                postId = it.postId,
                timestamp = it.timestamp,
            )
        },
        limit = ReadingProgressUtil.PAGE_SIZE,
    ).mapNotNull { candidate ->
        progressByThreadId[candidate.threadId]?.let {
            ContinueReadingUiItem(
                threadId = it.threadId,
                title = it.threadTitle,
                forumName = it.forumName,
                authorName = it.authorName,
                authorAvatar = it.authorAvatar,
                postId = it.postId,
                floor = it.floor,
                seeLz = it.seeLz,
                timestamp = it.timestamp,
            )
        }
    }
}

private fun ReadLaterItem.toWorkbenchSavedItem() = ReadingWorkbenchSavedItem(
    key = if (targetType == ReadingTargetType.THREAD) {
        "thread-$threadId"
    } else {
        "forum-$forumName"
    },
    targetType = targetType,
    threadId = threadId,
    forumName = forumName,
    title = title,
    subtitle = subtitle,
    avatar = avatar,
    username = username,
    timestamp = timestamp,
)

private fun LocalFavoriteItem.toWorkbenchSavedItem() = ReadingWorkbenchSavedItem(
    key = if (targetType == ReadingTargetType.THREAD) {
        "thread-$threadId"
    } else {
        "forum-$forumName"
    },
    targetType = targetType,
    threadId = threadId,
    forumName = forumName,
    title = title,
    subtitle = subtitle,
    avatar = avatar,
    username = username,
    timestamp = timestamp,
)

private fun loadReadLaterUiItems(): List<ReadingWorkbenchSavedItem> {
    val items = ReadLaterUtil.getAll().map { it.toWorkbenchSavedItem() }
    val itemByKey = items.associateBy { it.key }
    return buildSavedReadingEntries(
        items.map {
            SavedReadingEntryCandidate(
                key = it.key,
                type = it.targetType,
                title = it.title,
                subtitle = it.subtitle,
                timestamp = it.timestamp,
            )
        }
    ).mapNotNull { itemByKey[it.key] }
}

private fun loadLocalFavoriteUiItems(): List<ReadingWorkbenchSavedItem> {
    val items = LocalFavoriteUtil.getAll().map { it.toWorkbenchSavedItem() }
    val itemByKey = items.associateBy { it.key }
    return buildSavedReadingEntries(
        items.map {
            SavedReadingEntryCandidate(
                key = it.key,
                type = it.targetType,
                title = it.title,
                subtitle = it.subtitle,
                timestamp = it.timestamp,
            )
        }
    ).mapNotNull { itemByKey[it.key] }
}

@OptIn(ExperimentalFoundationApi::class)
@Destination(
    deepLinks = [
        DeepLink(uriPattern = "tblite://reading"),
    ]
)
@Composable
fun ReadingWorkbenchPage(
    navigator: DestinationsNavigator,
) {
    val pagerState = rememberPagerState { 3 }
    val coroutineScope = rememberCoroutineScope()

    var continueReadingItems by remember { mutableStateOf(emptyList<ContinueReadingUiItem>()) }
    var readLaterItems by remember { mutableStateOf(emptyList<ReadingWorkbenchSavedItem>()) }
    var localFavoriteItems by remember { mutableStateOf(emptyList<ReadingWorkbenchSavedItem>()) }

    fun refresh() {
        continueReadingItems = loadContinueReadingUiItems()
        readLaterItems = loadReadLaterUiItems()
        localFavoriteItems = loadLocalFavoriteUiItems()
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    MyScaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TitleCentredToolbar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_reading_workbench),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.h6,
                    )
                },
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = { navigator.navigateUp() })
                },
                content = {
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        indicator = { tabPositions ->
                            PagerTabIndicator(
                                pagerState = pagerState,
                                tabPositions = tabPositions,
                            )
                        },
                        divider = {},
                        backgroundColor = Color.Transparent,
                        modifier = Modifier
                            .width(120.dp * 3)
                            .align(Alignment.CenterHorizontally),
                    ) {
                        val tabs = listOf(
                            R.string.title_continue_reading,
                            R.string.title_read_later,
                            R.string.title_local_favorites,
                        )
                        tabs.forEachIndexed { index, textRes ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                selectedContentColor = Color.White,
                                unselectedContentColor = Color.White.copy(alpha = 0.72f),
                                text = {
                                    Text(
                                        text = stringResource(id = textRes),
                                        fontSize = 13.sp,
                                    )
                                },
                            )
                        }
                    }
                },
            )
        },
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { it },
            verticalAlignment = Alignment.Top,
        ) { page ->
            when (page) {
                0 -> ContinueReadingList(
                    items = continueReadingItems,
                    onClick = {
                        navigator.navigate(
                            ThreadPageDestination(
                                threadId = it.threadId,
                                postId = it.postId,
                                seeLz = it.seeLz,
                            )
                        )
                    },
                    onDelete = {
                        ReadingProgressUtil.remove(it.threadId)
                        continueReadingItems = continueReadingItems.filterNot { item ->
                            item.threadId == it.threadId
                        }
                    },
                )

                1 -> SavedReadingList(
                    items = readLaterItems,
                    emptyText = stringResource(id = R.string.empty_read_later),
                    onClick = {
                        when (it.targetType) {
                            ReadingTargetType.THREAD -> navigator.navigate(
                                ThreadPageDestination(threadId = it.threadId)
                            )

                            ReadingTargetType.FORUM -> navigator.navigate(
                                ForumPageDestination(it.forumName)
                            )
                        }
                    },
                    onDelete = {
                        when (it.targetType) {
                            ReadingTargetType.THREAD -> ReadLaterUtil.removeThread(it.threadId)
                            ReadingTargetType.FORUM -> ReadLaterUtil.removeForum(it.forumName)
                        }
                        readLaterItems = readLaterItems.filterNot { item -> item.key == it.key }
                    },
                )

                else -> SavedReadingList(
                    items = localFavoriteItems,
                    emptyText = stringResource(id = R.string.empty_local_favorites),
                    onClick = {
                        when (it.targetType) {
                            ReadingTargetType.THREAD -> navigator.navigate(
                                ThreadPageDestination(threadId = it.threadId)
                            )

                            ReadingTargetType.FORUM -> navigator.navigate(
                                ForumPageDestination(it.forumName)
                            )
                        }
                    },
                    onDelete = {
                        when (it.targetType) {
                            ReadingTargetType.THREAD -> LocalFavoriteUtil.removeThread(it.threadId)
                            ReadingTargetType.FORUM -> LocalFavoriteUtil.removeForum(it.forumName)
                        }
                        localFavoriteItems =
                            localFavoriteItems.filterNot { item -> item.key == it.key }
                    },
                )
            }
        }
    }
}

@Composable
private fun ContinueReadingList(
    items: List<ContinueReadingUiItem>,
    onClick: (ContinueReadingUiItem) -> Unit,
    onDelete: (ContinueReadingUiItem) -> Unit,
) {
    val context = LocalContext.current
    StateScreen(
        isEmpty = items.isEmpty(),
        isError = false,
        isLoading = false,
        emptyScreen = {
            Text(
                text = context.getString(R.string.empty_continue_reading),
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.72f),
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        MyLazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                count = items.size,
                key = { index -> items[index].threadId },
            ) { index ->
                val item = items[index]
                LongClickMenu(
                    menuContent = {
                        DropdownMenuItem(onClick = { onDelete(item) }) {
                            Text(text = stringResource(id = R.string.title_delete))
                        }
                    },
                    onClick = { onClick(item) },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        UserHeader(
                            avatar = {
                                Avatar(
                                    data = item.authorAvatar,
                                    contentDescription = null,
                                    size = 36.dp,
                                )
                            },
                            name = {
                                Text(text = item.authorName ?: item.forumName.orEmpty())
                            },
                        ) {
                            Text(
                                text = DateTimeUtils.getRelativeTimeString(context, item.timestamp),
                                fontSize = 13.sp,
                                color = MaterialTheme.colors.onBackground.copy(alpha = 0.72f),
                            )
                        }
                        Text(
                            text = item.title,
                            color = MaterialTheme.colors.onBackground,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = buildString {
                                if (!item.forumName.isNullOrBlank()) {
                                    append(item.forumName)
                                }
                                if (item.floor > 0) {
                                    if (isNotEmpty()) append(" · ")
                                    append(context.getString(R.string.label_reading_floor, item.floor))
                                }
                            },
                            color = MaterialTheme.colors.onBackground.copy(alpha = 0.72f),
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedReadingList(
    items: List<ReadingWorkbenchSavedItem>,
    emptyText: String,
    onClick: (ReadingWorkbenchSavedItem) -> Unit,
    onDelete: (ReadingWorkbenchSavedItem) -> Unit,
) {
    val context = LocalContext.current
    StateScreen(
        isEmpty = items.isEmpty(),
        isError = false,
        isLoading = false,
        emptyScreen = {
            Text(
                text = emptyText,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.72f),
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        MyLazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                count = items.size,
                key = { index -> items[index].key },
            ) { index ->
                val item = items[index]
                LongClickMenu(
                    menuContent = {
                        DropdownMenuItem(onClick = { onDelete(item) }) {
                            Text(text = stringResource(id = R.string.title_delete))
                        }
                    },
                    onClick = { onClick(item) },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        UserHeader(
                            avatar = {
                                Avatar(
                                    data = item.avatar,
                                    contentDescription = null,
                                    size = 36.dp,
                                    shape = RoundedCornerShape(10.dp),
                                )
                            },
                            name = {
                                Text(
                                    text = item.username ?: if (item.targetType == ReadingTargetType.FORUM) {
                                        context.getString(R.string.label_forum_entry)
                                    } else {
                                        context.getString(R.string.label_thread_entry)
                                    }
                                )
                            },
                        ) {
                            Text(
                                text = DateTimeUtils.getRelativeTimeString(context, item.timestamp),
                                fontSize = 13.sp,
                                color = MaterialTheme.colors.onBackground.copy(alpha = 0.72f),
                            )
                        }
                        Text(
                            text = item.title,
                            color = MaterialTheme.colors.onBackground,
                            fontWeight = FontWeight.Bold,
                        )
                        if (!item.subtitle.isNullOrBlank()) {
                            Text(
                                text = item.subtitle,
                                color = MaterialTheme.colors.onBackground.copy(alpha = 0.72f),
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}
