package com.huanchengfly.tieba.post.utils

object FollowedForumsCache {
    @Volatile
    private var forumNames: Set<String> = emptySet()

    fun update(names: List<String>) {
        forumNames = names.toHashSet()
    }

    fun isFollowed(name: String?): Boolean {
        if (name.isNullOrEmpty()) return false
        return forumNames.contains(name)
    }
}