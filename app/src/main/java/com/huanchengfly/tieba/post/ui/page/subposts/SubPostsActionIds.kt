package com.huanchengfly.tieba.post.ui.page.subposts

internal data class SubPostsActionIds(
    val forumId: Long,
    val postId: Long,
)

internal fun resolveSubPostsActionIds(
    routeForumId: Long,
    routePostId: Long,
    loadedForumId: Long?,
    loadedPostId: Long?,
): SubPostsActionIds = SubPostsActionIds(
    forumId = loadedForumId ?: routeForumId,
    postId = loadedPostId ?: routePostId,
)
