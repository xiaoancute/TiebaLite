package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.protos.frsPage.NavTabInfo
import com.huanchengfly.tieba.post.ui.models.forum.NavTab

/**
 * 把协议 [NavTabInfo] 的主标签数组映射成 UI 层 [NavTab] 列表.
 *
 * 行为:
 * - 接收 null / 空列表 → 返回 `[NavTab.Fallback]` 占位.
 * - 列表非空但**没有任何 isDefault=1** → 把第一个标记为 default.
 * - 协议 `tabId` / `tabName` / `tabType` / `isDefault` 直传.
 */
internal fun NavTabInfo?.toNavTabs(): List<NavTab> {
    val tabs = this?.tab.orEmpty()
    if (tabs.isEmpty()) return listOf(NavTab.Fallback)

    val anyDefault = tabs.any { it.isDefault == 1 }
    return tabs.mapIndexed { index, t ->
        NavTab(
            tabId = t.tabId,
            tabName = t.tabName,
            tabType = t.tabType,
            isDefault = if (anyDefault) t.isDefault == 1 else index == 0,
        )
    }
}
