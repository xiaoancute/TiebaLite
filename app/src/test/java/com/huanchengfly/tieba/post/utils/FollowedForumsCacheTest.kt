package com.huanchengfly.tieba.post.utils

import com.huanchengfly.tieba.post.api.models.FollowedForum
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
        FollowedForumsCache.updateAll(
            listOf(
                FollowedForum(
                    avatar = "avatar-123",
                    forumId = 123L,
                    forumName = "Forum 123",
                    isSign = false,
                    levelId = 3,
                    hotNum = 12
                ),
                FollowedForum(
                    avatar = "avatar-456",
                    forumId = 456L,
                    forumName = "Forum 456",
                    isSign = true,
                    levelId = 6,
                    hotNum = 34
                )
            )
        )

        assertTrue(shouldKeepFollowedForumThread(showFollowedOnly = true, forumId = 123L))
        assertTrue(shouldKeepFollowedForumThread(showFollowedOnly = true, forumId = 456L))
        assertFalse(shouldKeepFollowedForumThread(showFollowedOnly = true, forumId = 789L))
        assertFalse(shouldKeepFollowedForumThread(showFollowedOnly = true, forumId = null))
        assertFalse(shouldKeepFollowedForumThread(showFollowedOnly = true, forumId = 0L))
    }

    @Test
    fun followedForumsCacheStoresAndRemovesCompleteForumEntries() {
        FollowedForumsCache.clear()
        val initialForum = FollowedForum(
            avatar = "avatar-123",
            forumId = 123L,
            forumName = "Forum 123",
            isSign = false,
            levelId = 3,
            hotNum = 12
        )
        val updatedForum = initialForum.copy(isSign = true, levelId = 5, hotNum = 98)

        FollowedForumsCache.updateAll(listOf(initialForum))
        assertEquals(initialForum, FollowedForumsCache.getFollowedForum(123L))

        FollowedForumsCache.updateOrAddFollowedForum(updatedForum)
        assertEquals(updatedForum, FollowedForumsCache.getFollowedForum(123L))
        assertEquals(listOf(updatedForum), FollowedForumsCache.getAllFollowedForums())

        FollowedForumsCache.removeFollowedForum(123L)
        assertNull(FollowedForumsCache.getFollowedForum(123L))
        assertTrue(FollowedForumsCache.getAllFollowedForums().isEmpty())
    }
}
