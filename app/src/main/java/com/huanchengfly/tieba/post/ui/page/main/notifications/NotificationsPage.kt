package com.huanchengfly.tieba.post.ui.page.main.notifications

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.revival.CapabilityState
import com.huanchengfly.tieba.post.revival.RevivalFeatureGate
import com.huanchengfly.tieba.post.revival.RevivalFeatureRegistry
import com.huanchengfly.tieba.post.revival.SessionHealthStatus
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.ProvideNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.AboutPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.AccountManagePageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.LoginPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.SearchPageDestination
import com.huanchengfly.tieba.post.ui.page.main.notifications.list.NotificationsListPage
import com.huanchengfly.tieba.post.ui.page.main.notifications.list.NotificationsType
import com.huanchengfly.tieba.post.ui.widgets.compose.ActionItem
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.Button
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoadHorizontalPager
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.PagerTabIndicator
import com.huanchengfly.tieba.post.ui.widgets.compose.TabRow
import com.huanchengfly.tieba.post.ui.widgets.compose.TipScreen
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.Toolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.accountNavIconIfCompact
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Destination(
    deepLinks = [
        DeepLink(uriPattern = "tblite://notifications/{initialTab}")
    ]
)
@Composable
fun NotificationsPage(
    navigator: DestinationsNavigator,
    initialTab: Int = 0,
) {
    val pages = listOf<Pair<String, (@Composable () -> Unit)>>(
        stringResource(id = R.string.title_reply_me) to @Composable {
            NotificationsListPage(type = NotificationsType.ReplyMe)
        },
        stringResource(id = R.string.title_at_me) to @Composable {
            NotificationsListPage(type = NotificationsType.AtMe)
        },
        stringResource(id = R.string.title_agree_me) to @Composable {
            NotificationsListPage(type = NotificationsType.AgreeMe)
        }
    )
    val pagerState = rememberPagerState(
        initialPage = initialTab,
    ) { pages.size }
    val coroutineScope = rememberCoroutineScope()
    ProvideNavigator(navigator = navigator) {
        MyScaffold(
            backgroundColor = Color.Transparent,
            topBar = {
                TitleCentredToolbar(
                    title = { Text(text = stringResource(id = R.string.title_notifications)) },
                    navigationIcon = {
                        BackNavigationIcon {
                            navigator.navigateUp()
                        }
                    }
                ) {
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
                    ) {
                        pages.forEachIndexed { index, pair ->
                            Tab(
                                text = { Text(text = pair.first) },
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
        ) { paddingValues ->
            NotificationsAccessGuard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onOpenAbout = { navigator.navigate(AboutPageDestination) },
                onResolveSession = { status ->
                    if (status == SessionHealthStatus.LoggedOut) {
                        navigator.navigate(LoginPageDestination)
                    } else {
                        navigator.navigate(AccountManagePageDestination)
                    }
                }
            ) {
                LazyLoadHorizontalPager(
                    state = pagerState,
                    contentPadding = paddingValues,
                    key = { pages[it].first },
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.Top,
                    userScrollEnabled = true,
                ) {
                    pages[it].second()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationsPage(
    initialTab: Int = 0,
) {
    val navigator = LocalNavigator.current
    val pages = listOf<Pair<String, (@Composable () -> Unit)>>(
        stringResource(id = R.string.title_reply_me) to @Composable {
            NotificationsListPage(type = NotificationsType.ReplyMe)
        },
        stringResource(id = R.string.title_at_me) to @Composable {
            NotificationsListPage(type = NotificationsType.AtMe)
        },
        stringResource(id = R.string.title_agree_me) to @Composable {
            NotificationsListPage(type = NotificationsType.AgreeMe)
        }
    )
    val pagerState = rememberPagerState(
        initialPage = initialTab,
    ) { pages.size }
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            Toolbar(
                title = stringResource(id = R.string.title_notifications),
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
                ) {
                    pages.forEachIndexed { index, pair -> 
                        Tab(
                            text = { Text(text = pair.first) },
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        NotificationsAccessGuard(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            onOpenAbout = { navigator.navigate(AboutPageDestination) },
            onResolveSession = { status ->
                if (status == SessionHealthStatus.LoggedOut) {
                    navigator.navigate(LoginPageDestination)
                } else {
                    navigator.navigate(AccountManagePageDestination)
                }
            }
        ) {
            LazyLoadHorizontalPager(
                state = pagerState,
                contentPadding = paddingValues,
                key = { pages[it].first },
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
                userScrollEnabled = true,
            ) {
                pages[it].second()
            }
        }
    }
}

@Composable
private fun NotificationsAccessGuard(
    modifier: Modifier = Modifier,
    onOpenAbout: () -> Unit,
    onResolveSession: (SessionHealthStatus) -> Unit,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val account = AccountUtil.LocalAccount.current
    val notificationsCapabilityState = RevivalFeatureRegistry.state(
        context,
        RevivalFeatureGate.Notifications
    )
    val sessionHealth = remember(account) { AccountUtil.getSessionHealth(account) }

    when {
        notificationsCapabilityState == CapabilityState.Disabled -> {
            NotificationsGateScreen(
                title = stringResource(id = R.string.title_notifications_not_ready),
                message = stringResource(id = R.string.message_notifications_capability_disabled),
                primaryActionLabel = stringResource(id = R.string.button_open_about_page),
                onPrimaryAction = onOpenAbout,
                modifier = modifier
            )
        }

        !sessionHealth.isComplete -> {
            NotificationsGateScreen(
                title = stringResource(id = R.string.title_session_health),
                message = stringResource(
                    id = R.string.message_notifications_session_incomplete,
                    sessionHealth.toDisplayText(context)
                ),
                primaryActionLabel = stringResource(
                    id = if (sessionHealth.status == SessionHealthStatus.LoggedOut) {
                        R.string.button_login
                    } else {
                        R.string.title_account_manage
                    }
                ),
                onPrimaryAction = { onResolveSession(sessionHealth.status) },
                modifier = modifier
            )
        }

        else -> content()
    }
}

@Composable
private fun NotificationsGateScreen(
    title: String,
    message: String,
    primaryActionLabel: String,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TipScreen(
        title = {
            Text(text = title)
        },
        message = {
            Text(text = message)
        },
        actions = {
            Button(
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = primaryActionLabel)
            }
        },
        modifier = modifier
    )
}
