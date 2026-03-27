package com.huanchengfly.tieba.post.ui.page.subposts

internal fun buildSubPostsLoadMoreIntent(
    forumId: Long,
    threadId: Long,
    postId: Long,
    subPostId: Long,
    currentPage: Int,
    loadFromSubPost: Boolean,
): SubPostsUiIntent.LoadMore = SubPostsUiIntent.LoadMore(
    forumId = forumId,
    threadId = threadId,
    postId = postId,
    subPostId = subPostId.takeIf { loadFromSubPost } ?: 0L,
    page = currentPage + 1,
)
