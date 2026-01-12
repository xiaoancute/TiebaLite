package com.huanchengfly.tieba.post.utils

object FollowedForumsCache {
    @Volatile
    private var forumIds: Set<Long> = emptySet()

    fun update(ids: List<Long>) {
        forumIds = ids.toHashSet()
    }

    fun isFollowed(id: Long?): Boolean {
        if (id == null || id == 0L) return false
        return forumIds.contains(id)
    }
}