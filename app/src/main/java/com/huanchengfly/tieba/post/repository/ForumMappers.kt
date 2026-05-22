package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.protos.frsPage.NavTabInfo
import com.huanchengfly.tieba.post.ui.models.forum.NavTab

/**
 * 把协议 [NavTabInfo] 的主标签数组映射成 UI 层 [NavTab] 列表。
 */
internal fun NavTabInfo?.toNavTabs(): List<NavTab> {
    val tabs = this?.tab.orEmpty()
    if (tabs.isEmpty()) return listOf(NavTab.Fallback)

    return tabs.map { tab ->
        NavTab(
            tabId = tab.tabId,
            tabName = tab.tabName,
            tabType = tab.tabType,
            isDefault = tab.isDefault == 1,
            isGeneralTab = tab.isGeneralTab == 1,
        )
    }
        .ifEmpty { listOf(NavTab.Fallback) }
        .ensureDefaultTab()
}

private fun List<NavTab>.ensureDefaultTab(): List<NavTab> {
    if (any { it.isDefault }) return this
    return mapIndexed { index, tab ->
        if (index == 0) tab.copy(isDefault = true) else tab
    }
}
