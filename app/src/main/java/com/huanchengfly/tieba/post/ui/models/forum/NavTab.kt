package com.huanchengfly.tieba.post.ui.models.forum

import androidx.compose.runtime.Immutable

/**
 * 网页版"吧内分区"一项。对应协议 [FrsTabInfo].
 *
 * @param tabId 分区 ID,等同网页 URL 里的 `?tab=<id>`。
 *   `0` 是 [Fallback] 占位,用于协议未返回 nav_tab_info 的小吧。
 * @param tabType 协议原始 tabType。语义未文档化,先透传保存以便后续判定。
 * @param isDefault 协议侧标记为默认选中(`FrsTabInfo.isDefault == 1`)。
 */
@Immutable
data class NavTab(
    val tabId: Int,
    val tabName: String,
    val tabType: Int,
    val isDefault: Boolean,
) {
    /**
     * 是否为"精华类"分区。
     *
     * TODO(impl): tabType / tabCode 取值未文档化,先按 tabName 启发式判定。
     * 等抓包确认精华 tab 的 tabType 数值或 tabCode 后,改成稳健判定。
     */
    val isEssence: Boolean get() = tabName == ESSENCE_TAB_NAME

    companion object {
        const val ESSENCE_TAB_NAME = "精华"
        const val FALLBACK_TAB_ID = 0

        /** 协议未返回 nav_tab_info / 列表为空时的占位,行为等同旧"最新"。 */
        val Fallback: NavTab = NavTab(
            tabId = FALLBACK_TAB_ID,
            tabName = "全部",
            tabType = 0,
            isDefault = true,
        )
    }
}
