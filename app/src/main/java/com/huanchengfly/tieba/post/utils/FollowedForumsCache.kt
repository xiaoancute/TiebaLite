package com.huanchengfly.tieba.post.utils

import com.huanchengfly.tieba.post.api.models.FollowedForum

object FollowedForumsCache {
    @Volatile
    private var followedForums: Map<Long, FollowedForum> = emptyMap()

    fun updateAll(forums: List<FollowedForum>) {
        followedForums = forums.associateBy { it.forumId }
    }

    fun clear() {
        followedForums = emptyMap()
    }

    fun isFollowed(id: Long?): Boolean {
        if (id == null || id == 0L) return false
        return followedForums.containsKey(id)
    }

    fun updateOrAddFollowedForum(forum: FollowedForum) {
        if (forum.forumId == 0L) return
        synchronized(this) {
            val newForums = followedForums.toMutableMap()
            newForums[forum.forumId] = forum
            followedForums = newForums
        }
    }

    fun removeFollowedForum(id: Long?) {
        if (id == null || id == 0L || !followedForums.containsKey(id)) return
        synchronized(this) {
            val newForums = followedForums.toMutableMap()
            newForums.remove(id)
            followedForums = newForums
        }
    }

    fun getFollowedForum(id: Long?): FollowedForum? {
        if (id == null || id == 0L) return null
        return followedForums[id]
    }

    fun getAllFollowedForums(): List<FollowedForum> {
        return followedForums.values.toList()
    }
}

internal fun shouldKeepFollowedForumThread(
    showFollowedOnly: Boolean,
    forumId: Long?,
): Boolean {
    return !showFollowedOnly || FollowedForumsCache.isFollowed(forumId)
}
