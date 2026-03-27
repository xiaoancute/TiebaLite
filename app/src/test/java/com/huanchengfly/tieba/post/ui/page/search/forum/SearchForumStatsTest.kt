package com.huanchengfly.tieba.post.ui.page.search.forum

import com.huanchengfly.tieba.post.api.models.SearchForumBean
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchForumStatsTest {
    @Test
    fun forumStatsKeepConcernThenPostCounts() {
        val forum = SearchForumBean.ForumInfoBean(
            forumNameShow = "原神",
            concernNum = "456",
            postNum = "123"
        )

        val stats = buildSearchForumStats(forum)

        assertEquals(
            listOf(
                SearchForumStatItem(SearchForumStatType.CONCERN, "456"),
                SearchForumStatItem(SearchForumStatType.POST, "123"),
            ),
            stats
        )
    }
}
