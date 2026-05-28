package com.huanchengfly.tieba.post.ui.models.forum

import androidx.compose.runtime.Immutable

/**
 * 网页版"吧内分区"一项。对应协议 FrsTabInfo。
 *
 * @param tabId 分区 ID, 等同网页 URL 里的 `?tab=<id>`。
 *   `0` 是 [Fallback] 占位, 用于协议未返回 nav_tab_info 的小吧。
 * @param tabType 协议原始 tabType。语义未文档化, 先透传保存以便后续判定。
 * @param isDefault 协议侧标记为默认选中。
 */
@Immutable
data class NavTab(
    val tabId: Int,
    val tabName: String,
    val tabType: Int,
    val isDefault: Boolean,
    val isGeneralTab: Boolean = false,
) {
    val isHot: Boolean get() = tabId == HOT_TAB_ID || tabType == HOT_TAB_TYPE || tabName == HOT_TAB_NAME

    /**
     * 是否为"精华类"分区。
     *
     * TODO: tabType / tabCode 取值未文档化, 先按 tabName 启发式判定。
     */
    val isEssence: Boolean get() = tabName == ESSENCE_TAB_NAME

    val supportsSorting: Boolean get() = tabId == FALLBACK_TAB_ID || tabName == LATEST_TAB_NAME || isGeneralTab

    val usesTimeSortLabel: Boolean get() = tabId == FALLBACK_TAB_ID || tabName == LATEST_TAB_NAME

    val usesAppFrs: Boolean get() = tabId == FALLBACK_TAB_ID

    val pcSortType: Int get() = when {
        isHot -> HOT_PC_SORT_TYPE
        isEssence -> 0
        else -> 0
    }

    companion object {
        const val ESSENCE_TAB_NAME = "精华"
        const val HOT_TAB_NAME = "热门"
        const val LATEST_TAB_NAME = "最新"
        const val FALLBACK_TAB_ID = 0
        const val HOT_TAB_ID = 1
        const val HOT_TAB_TYPE = 13
        const val HOT_PC_SORT_TYPE = 3

        /** 协议未返回 nav_tab_info / 列表为空时的占位, 行为等同旧"最新"。 */
        val Fallback: NavTab = NavTab(
            tabId = FALLBACK_TAB_ID,
            tabName = "全部",
            tabType = 0,
            isDefault = true,
        )
    }
}
