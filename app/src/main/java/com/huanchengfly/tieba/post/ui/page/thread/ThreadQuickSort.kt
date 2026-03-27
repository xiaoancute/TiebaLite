package com.huanchengfly.tieba.post.ui.page.thread

import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.widgets.compose.HorizontalDivider
import com.huanchengfly.tieba.post.ui.widgets.compose.debounceClickable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class ThreadQuickSortOption(
    @StringRes val labelRes: Int,
    val sortType: Int,
    val isSelected: Boolean,
    val isEnabled: Boolean,
)

internal fun buildThreadQuickSortOptions(currentSortType: Int): ImmutableList<ThreadQuickSortOption> =
    persistentListOf(
        ThreadQuickSortOption(
            labelRes = R.string.title_asc,
            sortType = ThreadSortType.SORT_TYPE_ASC,
            isSelected = currentSortType == ThreadSortType.SORT_TYPE_ASC,
            isEnabled = currentSortType != ThreadSortType.SORT_TYPE_ASC,
        ),
        ThreadQuickSortOption(
            labelRes = R.string.title_desc,
            sortType = ThreadSortType.SORT_TYPE_DESC,
            isSelected = currentSortType == ThreadSortType.SORT_TYPE_DESC,
            isEnabled = currentSortType != ThreadSortType.SORT_TYPE_DESC,
        ),
    )

@Composable
internal fun ThreadQuickSortRow(
    currentSortType: Int,
    onSortSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = buildThreadQuickSortOptions(currentSortType)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.height(IntrinsicSize.Min)
    ) {
        options.forEachIndexed { index, option ->
            androidx.compose.material.Text(
                text = stringResource(option.labelRes),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .debounceClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        enabled = option.isEnabled,
                        indication = null,
                        onClick = { onSortSelected(option.sortType) }
                    ),
                fontSize = 13.sp,
                fontWeight = if (option.isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (option.isSelected) {
                    ExtendedTheme.colors.text
                } else {
                    ExtendedTheme.colors.textSecondary
                },
            )
            if (index != options.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}
