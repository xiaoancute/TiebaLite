package com.huanchengfly.tieba.post.ui.page.search.forum

import com.huanchengfly.tieba.post.api.models.SearchForumBean
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchForumDisplayNameTest {
    @Test
    fun displayName_usesForumNameShowWhenAvailable() {
        val forum = SearchForumBean.ForumInfoBean(
            forumName = "lol",
            forumNameShow = "LOL",
        )

        assertEquals("LOL", forum.displayName())
    }

    @Test
    fun displayName_fallsBackToForumNameWhenShowNameMissing() {
        val forum = SearchForumBean.ForumInfoBean(
            forumName = "lol",
            forumNameShow = null,
        )

        assertEquals("lol", forum.displayName())
    }
}
