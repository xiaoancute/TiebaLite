package com.huanchengfly.tieba.post.ui.page.search.forum

import com.huanchengfly.tieba.post.api.models.SearchForumBean
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchForumSummaryTextTest {
    @Test
    fun preferredSummary_usesSloganWhenAvailable() {
        val forum = SearchForumBean.ForumInfoBean(
            slogan = "尽在LOL吧！",
            intro = null,
        )

        assertEquals("尽在LOL吧！", forum.preferredSummaryText())
    }

    @Test
    fun preferredSummary_fallsBackToIntroWhenSloganMissing() {
        val forum = SearchForumBean.ForumInfoBean(
            slogan = "",
            intro = "这是贴吧简介",
        )

        assertEquals("这是贴吧简介", forum.preferredSummaryText())
    }

    @Test
    fun preferredSummary_returnsNullWhenBothFieldsBlank() {
        val forum = SearchForumBean.ForumInfoBean(
            slogan = " ",
            intro = null,
        )

        assertNull(forum.preferredSummaryText())
    }
}
