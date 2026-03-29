package com.huanchengfly.tieba.post.ui.page.thread

data class SavedThreadProgress(
    val postId: Long,
    val seeLz: Boolean,
)

data class ThreadResumeState(
    val postId: Long,
    val seeLz: Boolean,
    val restoredFromProgress: Boolean,
)

fun resolveThreadResumeState(
    initialPostId: Long,
    initialSeeLz: Boolean,
    rememberProgress: Boolean,
    savedProgress: SavedThreadProgress?,
): ThreadResumeState {
    if (initialPostId != 0L) {
        return ThreadResumeState(
            postId = initialPostId,
            seeLz = initialSeeLz,
            restoredFromProgress = false,
        )
    }

    if (rememberProgress && savedProgress != null && savedProgress.postId != 0L) {
        return ThreadResumeState(
            postId = savedProgress.postId,
            seeLz = savedProgress.seeLz,
            restoredFromProgress = true,
        )
    }

    return ThreadResumeState(
        postId = initialPostId,
        seeLz = initialSeeLz,
        restoredFromProgress = false,
    )
}
