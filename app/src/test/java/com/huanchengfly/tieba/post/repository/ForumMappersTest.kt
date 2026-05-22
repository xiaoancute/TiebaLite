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
    fun `normal multi-tab mapping keeps all web tabs`() {
        val info = NavTabInfo(
            tab = listOf(
                FrsTabInfo(tabId = 301, tabName = "精华", tabType = 2, isDefault = 0),
                FrsTabInfo(tabId = 1, tabName = "热门", tabType = 1, isDefault = 1),
                FrsTabInfo(tabId = 503, tabName = "最新", tabType = 3, isDefault = 0),
                FrsTabInfo(tabId = 20928, tabName = "开黑", tabType = 15, isDefault = 0, isGeneralTab = 1),
            )
        )

        val result = info.toNavTabs()

        assertEquals(
            listOf(
                NavTab(tabId = 301, tabName = "精华", tabType = 2, isDefault = false),
                NavTab(tabId = 1, tabName = "热门", tabType = 1, isDefault = true),
                NavTab(tabId = 503, tabName = "最新", tabType = 3, isDefault = false),
                NavTab(tabId = 20928, tabName = "开黑", tabType = 15, isDefault = false, isGeneralTab = true),
            ),
            result
        )
    }

    @Test
    fun `when no tab has isDefault=1, the first tab becomes default`() {
        val info = NavTabInfo(
            tab = listOf(
                FrsTabInfo(tabId = 301, tabName = "精华", tabType = 2, isDefault = 0),
                FrsTabInfo(tabId = 503, tabName = "最新", tabType = 3, isDefault = 0),
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

    @Test
    fun `fallback latest and general tabs support sorting`() {
        val info = NavTabInfo(
            tab = listOf(
                FrsTabInfo(tabId = 301, tabName = "精华", tabType = 2, isDefault = 0),
                FrsTabInfo(tabId = 1, tabName = "热门", tabType = 1, isDefault = 1),
                FrsTabInfo(tabId = 503, tabName = "最新", tabType = 3, isDefault = 0),
                FrsTabInfo(tabId = 20928, tabName = "开黑", tabType = 15, isDefault = 0, isGeneralTab = 1),
            )
        )

        val result = info.toNavTabs()

        assertEquals(true, NavTab.Fallback.supportsSorting)
        assertEquals(false, result[0].supportsSorting)
        assertEquals(false, result[1].supportsSorting)
        assertEquals(true, result[2].supportsSorting)
        assertEquals(true, result[3].supportsSorting)
    }

    @Test
    fun `hot tab is detected by id type or name and forces pc hot sort type`() {
        val byId = NavTab(tabId = NavTab.HOT_TAB_ID, tabName = "", tabType = 0, isDefault = false)
        val byType = NavTab(tabId = 999, tabName = "", tabType = NavTab.HOT_TAB_TYPE, isDefault = false)
        val byName = NavTab(tabId = 999, tabName = NavTab.HOT_TAB_NAME, tabType = 0, isDefault = false)

        listOf(byId, byType, byName).forEach {
            assertEquals(true, it.isHot)
            assertEquals(NavTab.HOT_PC_SORT_TYPE, it.pcSortType)
        }
    }

    @Test
    fun `only fallback tab uses protobuf frs request path`() {
        val latest = NavTab(tabId = 503, tabName = "最新", tabType = 3, isDefault = true)
        val fallback = NavTab.Fallback

        assertEquals(true, fallback.usesAppFrs)
        assertEquals(false, latest.usesAppFrs)
    }
}
