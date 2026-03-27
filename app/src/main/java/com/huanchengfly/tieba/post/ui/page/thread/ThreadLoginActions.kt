package com.huanchengfly.tieba.post.ui.page.thread

internal data class ThreadPostActionAvailability(
    val canTapCardToReply: Boolean,
    val showReplyMenuItem: Boolean,
    val showCopyMenuItem: Boolean,
    val showReportMenuItem: Boolean,
    val showFavoriteMenuItem: Boolean,
    val showDeleteMenuItem: Boolean,
)

internal fun resolveThreadPostActionAvailability(
    isLoggedIn: Boolean,
    hideReply: Boolean,
    hasCopyAction: Boolean,
    hasFavoriteAction: Boolean,
    canDelete: Boolean,
): ThreadPostActionAvailability = ThreadPostActionAvailability(
    canTapCardToReply = isLoggedIn && !hideReply,
    showReplyMenuItem = isLoggedIn && !hideReply,
    showCopyMenuItem = hasCopyAction,
    showReportMenuItem = isLoggedIn,
    showFavoriteMenuItem = isLoggedIn && hasFavoriteAction,
    showDeleteMenuItem = isLoggedIn && canDelete,
)

internal data class ThreadSubPostActionAvailability(
    val showCopyMenuItem: Boolean,
    val showReplyMenuItem: Boolean,
    val showReportMenuItem: Boolean,
)

internal fun resolveThreadSubPostActionAvailability(
    isLoggedIn: Boolean,
    hideReply: Boolean,
    hasCopyAction: Boolean,
): ThreadSubPostActionAvailability = ThreadSubPostActionAvailability(
    showCopyMenuItem = hasCopyAction,
    showReplyMenuItem = isLoggedIn && !hideReply,
    showReportMenuItem = isLoggedIn,
)

internal data class ThreadMenuActionAvailability(
    val showCollectToggle: Boolean,
    val showReportItem: Boolean,
    val showDeleteItem: Boolean,
)

internal fun resolveThreadMenuActionAvailability(
    isLoggedIn: Boolean,
    canDelete: Boolean,
): ThreadMenuActionAvailability = ThreadMenuActionAvailability(
    showCollectToggle = isLoggedIn,
    showReportItem = isLoggedIn,
    showDeleteItem = isLoggedIn && canDelete,
)
