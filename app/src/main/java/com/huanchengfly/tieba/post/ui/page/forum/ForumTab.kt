package com.huanchengfly.tieba.post.ui.page.forum

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.arch.unsafeLazy
import com.huanchengfly.tieba.post.ui.models.forum.NavTab
import com.huanchengfly.tieba.post.ui.models.settings.ForumSortType
import com.huanchengfly.tieba.post.ui.widgets.compose.FancyAnimatedIndicatorWithModifier
import com.huanchengfly.tieba.post.ui.widgets.compose.TabClickMenu
import com.huanchengfly.tieba.post.ui.widgets.compose.preference.Options
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.launch

private val TabSortTypes: Options<Int> by unsafeLazy {
    persistentMapOf(
        ForumSortType.BY_REPLY to R.string.title_sort_by_reply,
        ForumSortType.BY_SEND to R.string.title_sort_by_send
    )
}

/**
 * 顶部主标签栏. 由 [navTabs] 动态出 N 个 tab.
 *
 * 排序菜单 [TabClickMenu] 仅挂在**非精华**类 tab 上 —— 精华 tab 的排序由协议侧固定.
 */
@Composable
fun ForumTab(
    modifier: Modifier = Modifier,
    navTabs: List<NavTab>,
    pagerState: PagerState,
    sortType: Int,
    onSortTypeChanged: (sortType: Int) -> Unit,
) {
    val currentPage = pagerState.currentPage
    val coroutineScope = rememberCoroutineScope()

    val unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tabTextStyle = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp)

    SecondaryScrollableTabRow(
        selectedTabIndex = currentPage,
        indicator = { FancyAnimatedIndicatorWithModifier(index = currentPage) },
        divider = {},
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 0.dp,
        modifier = modifier,
    ) {
        navTabs.forEachIndexed { index, tab ->
            val selected = index == currentPage
            val onClick: () -> Unit = {
                coroutineScope.launch { pagerState.animateScrollToPage(index) }
            }

            if (tab.isEssence) {
                Tab(selected = selected, onClick = onClick, unselectedContentColor = unselectedContentColor) {
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = tab.tabName, style = tabTextStyle)
                    }
                }
            } else {
                TabClickMenu(
                    selected = selected,
                    onClick = onClick,
                    text = { Text(text = tab.tabName, style = tabTextStyle) },
                    menuContent = {
                        ListPickerMenuItems(
                            items = TabSortTypes,
                            picked = sortType,
                            onItemPicked = onSortTypeChanged
                        )
                    },
                    unselectedContentColor = unselectedContentColor,
                )
            }
        }
    }
}
