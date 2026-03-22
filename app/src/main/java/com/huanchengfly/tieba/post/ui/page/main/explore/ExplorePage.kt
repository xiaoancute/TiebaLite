package com.huanchengfly.tieba.post.ui.page.main.explore

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.emitGlobalEvent
import com.huanchengfly.tieba.post.arch.onGlobalEvent
import com.huanchengfly.tieba.post.revival.RevivalFeatureRegistry
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.SearchPageDestination
import com.huanchengfly.tieba.post.ui.page.main.explore.concern.ConcernPage
import com.huanchengfly.tieba.post.ui.page.main.explore.hot.HotPage
import com.huanchengfly.tieba.post.ui.page.main.explore.personalized.PersonalizedPage
import com.huanchengfly.tieba.post.ui.widgets.compose.ActionItem
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoadHorizontalPager
import com.huanchengfly.tieba.post.ui.widgets.compose.PagerTabIndicator
import com.huanchengfly.tieba.post.ui.widgets.compose.RevivalNotice
import com.huanchengfly.tieba.post.ui.widgets.compose.TabRow
import com.huanchengfly.tieba.post.ui.widgets.compose.Toolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.accountNavIconIfCompact
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch


@Immutable
data class ExplorePageItem(
    val id: String,
    val name: @Composable (selected: Boolean) -> Unit,
    val content: @Composable () -> Unit,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ColumnScope.ExplorePageTab(
    pagerState: PagerState,
    pages: ImmutableList<ExplorePageItem>
) {
    val coroutineScope = rememberCoroutineScope()

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            PagerTabIndicator(
                pagerState = pagerState,
                tabPositions = tabPositions
            )
        },
        divider = {},
        backgroundColor = Color.Transparent,
        contentColor = ExtendedTheme.colors.onTopBar,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .width(76.dp * pages.size),
    ) {
        pages.fastForEachIndexed { index, item ->
            Tab(
                text = { item.name(pagerState.currentPage == index) },
                selected = pagerState.currentPage == index,
                onClick = {
                    coroutineScope.launch {
                        if (pagerState.currentPage == index) {
                            emitGlobalEvent(GlobalEvent.Refresh(item.id))
                        } else {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun TabText(
    text: String,
    selected: Boolean
) {
    val style = MaterialTheme.typography.button.copy(
        letterSpacing = 0.75.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center
    )
    Text(text = text, style = style)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExplorePage() {
    val account = LocalAccount.current
    val context = LocalContext.current
    val navigator = LocalNavigator.current

    val loggedIn = account != null

    val pages = remember(loggedIn) {
        listOfNotNull(
            if (loggedIn) ExplorePageItem(
                "concern",
                { TabText(text = stringResource(id = R.string.title_concern), selected = it) },
                { ConcernPage() }
            ) else null,
            ExplorePageItem(
                "personalized",
                { TabText(text = stringResource(id = R.string.title_personalized), selected = it) },
                { PersonalizedPage() }
            ),
            ExplorePageItem(
                "hot",
                { TabText(text = stringResource(id = R.string.title_hot), selected = it) },
                { HotPage() }
            ),
        ).toImmutableList()
    }
    val pagerState = rememberPagerState(initialPage = if (account != null) 1 else 0) { pages.size }
    val coroutineScope = rememberCoroutineScope()
    var selectedPageId by rememberSaveable { mutableStateOf("personalized") }

    LaunchedEffect(pagerState, pages) {
        snapshotFlow { pagerState.currentPage }
            .collect { currentPage ->
                pages.getOrNull(currentPage)?.id?.let { pageId ->
                    selectedPageId = pageId
                }
            }
    }

    LaunchedEffect(pages, selectedPageId) {
        val targetPage = pages.indexOfFirst { it.id == selectedPageId }
            .takeIf { it >= 0 }
            ?: 0
        if (targetPage != pagerState.currentPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    onGlobalEvent<GlobalEvent.Refresh>(
        filter = { it.key == "explore" }
    ) {
        coroutineScope.emitGlobalEvent(GlobalEvent.Refresh(pages[pagerState.currentPage].id))
    }

    Scaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            Toolbar(
                title = stringResource(id = R.string.title_explore),
                navigationIcon = accountNavIconIfCompact(),
                actions = {
                    ActionItem(
                        icon = Icons.Rounded.Search,
                        contentDescription = stringResource(id = R.string.title_search)
                    ) {
                        navigator.navigate(SearchPageDestination)
                    }
                },
            ) {
                ExplorePageTab(pagerState = pagerState, pages = pages)
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            RevivalNotice(
                text = RevivalFeatureRegistry.buildExploreBrowseSummary(context, loggedIn),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyLoadHorizontalPager(
                contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
                state = pagerState,
                key = { pages[it].id },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.Top,
                userScrollEnabled = true,
            ) {
                pages[it].content()
            }
        }
    }
}
