package com.huanchengfly.tieba.post.utils

import com.huanchengfly.tieba.post.workers.OKSignWork

enum class OKSignNotificationKind {
    Progress,
    Completion,
    Failure,
}

data class OKSignNotificationSpec(
    val notificationId: Int,
    val ongoing: Boolean,
    val autoCancel: Boolean,
)

fun buildOKSignNotificationSpec(
    kind: OKSignNotificationKind,
    uniqueSeed: Long = OKSignWork.NOTIFICATION_ID.toLong(),
): OKSignNotificationSpec = when (kind) {
    OKSignNotificationKind.Progress -> OKSignNotificationSpec(
        notificationId = OKSignWork.NOTIFICATION_ID,
        ongoing = true,
        autoCancel = false,
    )

    OKSignNotificationKind.Completion -> OKSignNotificationSpec(
        notificationId = OKSignWork.NOTIFICATION_ID,
        ongoing = false,
        autoCancel = true,
    )

    OKSignNotificationKind.Failure -> OKSignNotificationSpec(
        notificationId = uniqueSeed.toInt(),
        ongoing = false,
        autoCancel = true,
    )
}
