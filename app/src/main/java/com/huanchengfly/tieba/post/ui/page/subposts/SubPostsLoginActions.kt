package com.huanchengfly.tieba.post.ui.page.subposts

internal data class SubPostsItemActionAvailability(
    val canTapItemToReply: Boolean,
    val showCopyMenuItem: Boolean,
    val showReplyMenuItem: Boolean,
    val showReportMenuItem: Boolean,
    val showDeleteMenuItem: Boolean,
)

internal fun resolveSubPostsItemActionAvailability(
    isLoggedIn: Boolean,
    hideReply: Boolean,
    hasCopyAction: Boolean,
    canDelete: Boolean,
): SubPostsItemActionAvailability = SubPostsItemActionAvailability(
    canTapItemToReply = isLoggedIn && !hideReply,
    showCopyMenuItem = hasCopyAction,
    showReplyMenuItem = isLoggedIn && !hideReply,
    showReportMenuItem = isLoggedIn,
    showDeleteMenuItem = isLoggedIn && canDelete,
)
