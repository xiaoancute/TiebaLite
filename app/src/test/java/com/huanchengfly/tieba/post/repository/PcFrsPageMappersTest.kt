package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.web.PcFrsTab
import com.huanchengfly.tieba.post.api.models.web.PcFrsPageResponse
import com.huanchengfly.tieba.post.api.models.web.PcNavTabInfo
import com.huanchengfly.tieba.post.ui.models.ThreadTimeType
import com.huanchengfly.tieba.post.ui.models.settings.ForumSortType
import org.junit.Assert.assertEquals
import org.junit.Test

class PcFrsPageMappersTest {

    @Test
    fun `pc sign matches captured browser request`() {
        val fields = linkedMapOf(
            "kw" to "%E7%8E%8B%E8%80%85%E8%8D%A3%E8%80%80",
            "pn" to "1",
            "is_good" to "0",
            "cid" to "",
            "sort_type" to "3",
            "tab_id" to "1",
            "tab_type" to "13",
            "tab_name" to "热门",
            "forum_id" to "837839",
            "is_newfrs" to "1",
            "is_newfeed" to "1",
            "rn" to "30",
            "rn_need" to "10",
            "tbs" to "5e39fbabef2e40ec1779285019",
            "subapp_type" to "pc",
            "_client_type" to "20",
        )

        assertEquals("d7826fd340279518fbfd4f7e1ec61881", fields.withPcSign()["sign"])
    }

    @Test
    fun `pc tabs map to nav tabs with general flag`() {
        val tabs = listOf(
            PcFrsTab(tabId = 301, tabType = 12, tabName = "精华", isGeneralTab = 0),
            PcFrsTab(tabId = 1, tabType = 13, tabName = "热门", isGeneralTab = 0),
            PcFrsTab(tabId = 503, tabType = 14, tabName = "最新", isGeneralTab = 0),
            PcFrsTab(tabId = 20928, tabType = 15, tabName = "开黑", isGeneralTab = 1),
        )

        val result = tabs.toNavTabs()

        assertEquals(4, result.size)
        assertEquals(false, result[0].isGeneralTab)
        assertEquals(true, result[0].isDefault)
        assertEquals(true, result[3].isGeneralTab)
    }

    @Test
    fun `pc response uses page data tab id as default tab`() {
        val response = PcFrsPageResponse(
            pageDataTabId = 1,
            frsTabDefault = 301,
            navTabInfo = PcNavTabInfo(
                tab = listOf(
                    PcFrsTab(tabId = 301, tabType = 12, tabName = "精华", isGeneralTab = 0),
                    PcFrsTab(tabId = 1, tabType = 13, tabName = "热门", isGeneralTab = 0),
                    PcFrsTab(tabId = 503, tabType = 14, tabName = "最新", isGeneralTab = 0),
                )
            )
        )

        val result = response.toNavTabs()

        assertEquals(false, result[0].isDefault)
        assertEquals(true, result[1].isDefault)
        assertEquals(false, result[2].isDefault)
    }

    @Test
    fun `pc thread time uses reply time for reply sorting`() {
        val result = selectThreadDisplayTime(
            sortType = ForumSortType.BY_REPLY,
            createTime = 1_700_000_000L,
            replyTime = 1_800_000_000L,
        )

        assertEquals(1_800_000_000_000L, result.timeMillis)
        assertEquals(ThreadTimeType.REPLY, result.type)
    }

    @Test
    fun `pc thread time uses publish time for send sorting`() {
        val result = selectThreadDisplayTime(
            sortType = ForumSortType.BY_SEND,
            createTime = 1_700_000_000L,
            replyTime = 1_800_000_000L,
        )

        assertEquals(1_700_000_000_000L, result.timeMillis)
        assertEquals(ThreadTimeType.PUBLISH, result.type)
    }

    @Test
    fun `pc thread time uses reply time for hot tab`() {
        val result = selectThreadDisplayTime(
            sortType = ForumSortType.BY_REPLY,
            createTime = 1_700_000_000L,
            replyTime = 1_800_000_000L,
        )

        assertEquals(1_800_000_000_000L, result.timeMillis)
        assertEquals(ThreadTimeType.REPLY, result.type)
    }

    @Test
    fun `pc thread time falls back to publish time when reply time is missing`() {
        val result = selectThreadDisplayTime(
            sortType = ForumSortType.BY_REPLY,
            createTime = 1_700_000_000L,
            replyTime = 0L,
        )

        assertEquals(1_700_000_000_000L, result.timeMillis)
        assertEquals(ThreadTimeType.REPLY, result.type)
    }
}
