package com.huanchengfly.tieba.post.ui.page.subposts

import org.junit.Assert.assertEquals
import org.junit.Test

class SubPostsActionIdsTest {
    @Test
    fun loadedIdsOverrideRouteIdsWhenAvailable() {
        val resolved = resolveSubPostsActionIds(
            routeForumId = 100L,
            routePostId = 200L,
            loadedForumId = 101L,
            loadedPostId = 201L,
        )

        assertEquals(101L, resolved.forumId)
        assertEquals(201L, resolved.postId)
    }

    @Test
    fun routeIdsRemainWhenLoadedIdsAreMissing() {
        val resolved = resolveSubPostsActionIds(
            routeForumId = 100L,
            routePostId = 200L,
            loadedForumId = null,
            loadedPostId = null,
        )

        assertEquals(100L, resolved.forumId)
        assertEquals(200L, resolved.postId)
    }
}
