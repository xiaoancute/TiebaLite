package com.huanchengfly.tieba.post.ui.page.subposts

import org.junit.Assert.assertEquals
import org.junit.Test

class SubPostsPagingIntentTest {
    @Test
    fun loadMoreKeepsSubPostIdWhenOpenedFromSpecificSubPost() {
        val intent = buildSubPostsLoadMoreIntent(
            forumId = 1L,
            threadId = 2L,
            postId = 3L,
            subPostId = 4L,
            currentPage = 2,
            loadFromSubPost = true,
        )

        assertEquals(4L, intent.subPostId)
        assertEquals(3, intent.page)
    }

    @Test
    fun loadMoreClearsSubPostIdWhenOpenedFromRegularPost() {
        val intent = buildSubPostsLoadMoreIntent(
            forumId = 1L,
            threadId = 2L,
            postId = 3L,
            subPostId = 4L,
            currentPage = 2,
            loadFromSubPost = false,
        )

        assertEquals(0L, intent.subPostId)
        assertEquals(3, intent.page)
    }
}
