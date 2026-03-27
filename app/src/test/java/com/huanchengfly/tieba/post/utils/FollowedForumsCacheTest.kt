package com.huanchengfly.tieba.post.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FollowedForumsCacheTest {
    @Test
    fun followedOnlyFilterAllowsAllThreadsWhenSwitchIsOff() {
        assertTrue(shouldKeepFollowedForumThread(showFollowedOnly = false, forumId = null))
        assertTrue(shouldKeepFollowedForumThread(showFollowedOnly = false, forumId = 0L))
        assertTrue(shouldKeepFollowedForumThread(showFollowedOnly = false, forumId = 123L))
    }

    @Test
    fun followedOnlyFilterMatchesByForumId() {
        FollowedForumsCache.clear()
        FollowedForumsCache.update(listOf(123L, 456L))

        assertTrue(shouldKeepFollowedForumThread(showFollowedOnly = true, forumId = 123L))
        assertTrue(shouldKeepFollowedForumThread(showFollowedOnly = true, forumId = 456L))
        assertFalse(shouldKeepFollowedForumThread(showFollowedOnly = true, forumId = 789L))
        assertFalse(shouldKeepFollowedForumThread(showFollowedOnly = true, forumId = null))
        assertFalse(shouldKeepFollowedForumThread(showFollowedOnly = true, forumId = 0L))
    }
}
