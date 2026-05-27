package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.web.PcFrsTab
import com.huanchengfly.tieba.post.api.models.web.PcFrsPageResponse
import com.huanchengfly.tieba.post.api.models.web.PcFeed
import com.huanchengfly.tieba.post.api.models.web.PcFeedComponent
import com.huanchengfly.tieba.post.api.models.web.PcFeedItem
import com.huanchengfly.tieba.post.api.models.web.PcFeedSocial
import com.huanchengfly.tieba.post.api.models.web.PcPageData
import com.huanchengfly.tieba.post.api.models.web.PcNavTabInfo
import com.huanchengfly.tieba.post.ui.models.ThreadTimeType
import com.huanchengfly.tieba.post.ui.models.forum.NavTab
import com.huanchengfly.tieba.post.ui.models.settings.ForumSortType
import kotlinx.coroutines.test.runTest
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
    fun `pc thread mapper uses reply time for reply sorting`() = runTest {
        val result = pcThreadResponse().toThreadItemList(
            tab = latestTab(),
            sortType = ForumSortType.BY_REPLY,
            showBothName = false,
            isBlocked = { _, _ -> false },
        )

        assertEquals(1_800_000_000_000L, result.threads.single().lastTimeMill)
        assertEquals(ThreadTimeType.REPLY, result.threads.single().timeType)
    }

    @Test
    fun `pc thread mapper uses publish time for send sorting`() = runTest {
        val result = pcThreadResponse().toThreadItemList(
            tab = latestTab(),
            sortType = ForumSortType.BY_SEND,
            showBothName = false,
            isBlocked = { _, _ -> false },
        )

        assertEquals(1_700_000_000_000L, result.threads.single().lastTimeMill)
        assertEquals(ThreadTimeType.PUBLISH, result.threads.single().timeType)
    }

    private fun latestTab() = NavTab(tabId = 503, tabName = "最新", tabType = 14, isDefault = true)

    private fun pcThreadResponse(): PcFrsPageResponse {
        return PcFrsPageResponse(
            pageData = PcPageData(
                feedList = listOf(
                    PcFeedItem(
                        feed = PcFeed(
                            businessInfoMap = mapOf(
                                "thread_id" to "1",
                                "title" to "标题",
                                "abstract" to "内容",
                                "user_id" to "2",
                                "forum_id" to "3",
                                "forum_name" to "test",
                                "create_time" to "1700000000",
                                "last_time_int" to "1800000000",
                            ),
                            components = listOf(PcFeedComponent(feedSocial = PcFeedSocial(tid = 1)))
                        )
                    )
                )
            )
        )
    }
}
