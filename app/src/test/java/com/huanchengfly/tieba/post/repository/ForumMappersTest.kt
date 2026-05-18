package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.protos.FrsTabInfo
import com.huanchengfly.tieba.post.api.models.protos.frsPage.NavTabInfo
import com.huanchengfly.tieba.post.ui.models.forum.NavTab
import org.junit.Assert.assertEquals
import org.junit.Test

class ForumMappersTest {

    @Test
    fun `null nav_tab_info maps to single fallback tab`() {
        val result = (null as NavTabInfo?).toNavTabs()
        assertEquals(listOf(NavTab.Fallback), result)
    }

    @Test
    fun `empty tab list maps to single fallback tab`() {
        val result = NavTabInfo(tab = emptyList()).toNavTabs()
        assertEquals(listOf(NavTab.Fallback), result)
    }

    @Test
    fun `normal multi-tab mapping preserves order and fields`() {
        val info = NavTabInfo(
            tab = listOf(
                FrsTabInfo(tabId = 301, tabName = "精华", tabType = 2, isDefault = 0),
                FrsTabInfo(tabId = 1, tabName = "热门", tabType = 1, isDefault = 1),
                FrsTabInfo(tabId = 503, tabName = "最新", tabType = 3, isDefault = 0),
            )
        )

        val result = info.toNavTabs()

        assertEquals(
            listOf(
                NavTab(tabId = 301, tabName = "精华", tabType = 2, isDefault = false),
                NavTab(tabId = 1, tabName = "热门", tabType = 1, isDefault = true),
                NavTab(tabId = 503, tabName = "最新", tabType = 3, isDefault = false),
            ),
            result
        )
    }

    @Test
    fun `when no tab has isDefault=1, the first tab becomes default`() {
        val info = NavTabInfo(
            tab = listOf(
                FrsTabInfo(tabId = 10, tabName = "A", tabType = 0, isDefault = 0),
                FrsTabInfo(tabId = 20, tabName = "B", tabType = 0, isDefault = 0),
            )
        )

        val result = info.toNavTabs()

        assertEquals(true, result[0].isDefault)
        assertEquals(false, result[1].isDefault)
    }

    @Test
    fun `essence tab is detected by name`() {
        val info = NavTabInfo(
            tab = listOf(FrsTabInfo(tabId = 301, tabName = "精华", tabType = 2, isDefault = 1))
        )
        assertEquals(true, info.toNavTabs().single().isEssence)
    }
}
