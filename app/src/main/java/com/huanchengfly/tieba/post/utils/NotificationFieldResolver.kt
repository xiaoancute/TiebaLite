package com.huanchengfly.tieba.post.utils

import android.util.Log
import com.huanchengfly.tieba.post.api.models.MessageListBean

enum class NotificationFieldSource {
    PrimaryAgree,
    LegacyFans,
    LegacyAtList,
    LegacyReplyList,
    Missing,
}

data class NotificationListSelection(
    val data: List<MessageListBean.MessageInfoBean>,
    val source: NotificationFieldSource,
) {
    val usedCompatibilityFallback: Boolean
        get() = source == NotificationFieldSource.LegacyAtList ||
                source == NotificationFieldSource.LegacyReplyList
}

data class NotificationUnreadSelection(
    val count: Int,
    val source: NotificationFieldSource,
) {
    val usedCompatibilityFallback: Boolean
        get() = source == NotificationFieldSource.LegacyFans
}

object NotificationFieldResolver {
    private const val TAG = "NotificationResolver"

    fun selectAgreeList(
        primaryAgreeList: List<MessageListBean.MessageInfoBean>?,
        legacyAtList: List<MessageListBean.MessageInfoBean>?,
        legacyReplyList: List<MessageListBean.MessageInfoBean>?,
    ): NotificationListSelection {
        return when {
            primaryAgreeList != null -> NotificationListSelection(
                data = primaryAgreeList,
                source = NotificationFieldSource.PrimaryAgree
            )

            legacyAtList != null -> {
                logFallback("AgreeMe list", "at_list")
                NotificationListSelection(
                    data = legacyAtList,
                    source = NotificationFieldSource.LegacyAtList
                )
            }

            legacyReplyList != null -> {
                logFallback("AgreeMe list", "reply_list")
                NotificationListSelection(
                    data = legacyReplyList,
                    source = NotificationFieldSource.LegacyReplyList
                )
            }

            else -> NotificationListSelection(
                data = emptyList(),
                source = NotificationFieldSource.Missing
            )
        }
    }

    fun selectAgreeUnread(
        primaryAgreeCount: String?,
        legacyFansCount: String?,
    ): NotificationUnreadSelection {
        return when {
            primaryAgreeCount != null -> NotificationUnreadSelection(
                count = primaryAgreeCount.toUnreadCount(),
                source = NotificationFieldSource.PrimaryAgree
            )

            legacyFansCount != null -> {
                logFallback("AgreeMe unread", "fans")
                NotificationUnreadSelection(
                    count = legacyFansCount.toUnreadCount(),
                    source = NotificationFieldSource.LegacyFans
                )
            }

            else -> NotificationUnreadSelection(
                count = 0,
                source = NotificationFieldSource.Missing
            )
        }
    }

    private fun String?.toUnreadCount(): Int {
        return this?.toIntOrNull() ?: 0
    }

    private fun logFallback(target: String, field: String) {
        runCatching {
            Log.w(
                TAG,
                "$target fell back to legacy field '$field'; verify with a fresh test-account sample."
            )
        }
    }
}
