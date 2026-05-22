package com.huanchengfly.tieba.post.ui.page.forum

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.arch.unsafeLazy
import com.huanchengfly.tieba.post.ui.models.forum.NavTab
import com.huanchengfly.tieba.post.ui.models.settings.ForumSortType
import com.huanchengfly.tieba.post.ui.widgets.compose.TabClickMenu
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberMenuState
import com.huanchengfly.tieba.post.ui.widgets.compose.preference.Options
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.launch

private val ForumTabShape = RoundedCornerShape(8.dp)

private val TabSortTypes: Options<Int> by unsafeLazy {
    persistentMapOf(
        ForumSortType.BY_REPLY to R.string.title_sort_by_reply,
        ForumSortType.BY_SEND to R.string.title_sort_by_send
    )
}

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
        indicator = {},
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

            if (!tab.supportsSorting) {
                Tab(
                    selected = selected,
                    onClick = onClick,
                    unselectedContentColor = unselectedContentColor,
                ) {
                    ForumTabPill(selected = selected) {
                        Text(text = tab.tabName, style = tabTextStyle)
                    }
                }
            } else {
                val menuState = rememberMenuState()
                TabClickMenu(
                    selected = selected,
                    onClick = onClick,
                    menuContent = {
                        ListPickerMenuItems(
                            items = TabSortTypes,
                            picked = sortType,
                            onItemPicked = onSortTypeChanged
                        )
                    },
                    menuState = menuState,
                    unselectedContentColor = unselectedContentColor,
                ) {
                    val rotate by animateFloatAsState(
                        targetValue = if (menuState.expanded) 180f else 0f,
                        label = "ForumTabArrowRotate"
                    )
                    val alpha by animateFloatAsState(
                        targetValue = if (selected) 1f else 0f,
                        label = "ForumTabArrowAlpha"
                    )

                    ForumTabPill(selected = selected) {
                        Text(text = tab.tabName, style = tabTextStyle)

                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.graphicsLayer {
                                rotationZ = rotate
                                this.alpha = alpha
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ForumTabPill(
    selected: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (selected) {
        colorScheme.primary.copy(alpha = 0.12f)
    } else {
        Color.Transparent
    }
    val borderColor = if (selected) colorScheme.primary else Color.Transparent

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .height(36.dp)
                .clip(ForumTabShape)
                .background(containerColor)
                .border(width = 1.dp, color = borderColor, shape = ForumTabShape)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}
