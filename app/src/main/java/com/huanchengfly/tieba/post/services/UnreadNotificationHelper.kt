package com.huanchengfly.tieba.post.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ActivityCompat
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.MsgBean
import com.huanchengfly.tieba.post.pendingIntentFlagImmutable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.NotificationFieldResolver

object UnreadNotificationHelper {
    const val ACTION_NEW_MESSAGE = "com.huanchengfly.tieba.post.action.NEW_MESSAGE"
    const val CHANNEL_GROUP = "20"
    const val CHANNEL_AT = "3"
    const val CHANNEL_AT_NAME = "提到我的"
    const val CHANNEL_AGREE = "4"
    const val CHANNEL_AGREE_NAME = "赞过我的"
    const val CHANNEL_TOTAL = "total"
    const val ID_REPLY = 20
    const val ID_AT = 21
    const val ID_AGREE = 22
    private const val CHANNEL_GROUP_NAME = "消息通知"
    private const val CHANNEL_REPLY = "2"
    private const val CHANNEL_REPLY_NAME = "回复我的"

    fun ensureChannels(
        context: Context,
        notificationManager: NotificationManager,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelGroup = NotificationChannelGroup(CHANNEL_GROUP, CHANNEL_GROUP_NAME)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                channelGroup.description = "贴吧的各种消息通知"
            }
            notificationManager.createNotificationChannelGroup(channelGroup)
            createChannel(notificationManager, CHANNEL_REPLY, CHANNEL_REPLY_NAME)
            createChannel(notificationManager, CHANNEL_AT, CHANNEL_AT_NAME)
            createChannel(notificationManager, CHANNEL_AGREE, CHANNEL_AGREE_NAME)
        }
    }

    fun clear(
        context: Context,
        notificationManager: NotificationManager,
    ) {
        notificationManager.cancel(ID_REPLY)
        notificationManager.cancel(ID_AT)
        notificationManager.cancel(ID_AGREE)
        broadcastCount(context, CHANNEL_REPLY, 0)
        broadcastCount(context, CHANNEL_AT, 0)
        broadcastCount(context, CHANNEL_AGREE, 0)
        broadcastCount(context, CHANNEL_TOTAL, 0)
    }

    fun dispatch(
        context: Context,
        notificationManager: NotificationManager,
        msgBean: MsgBean,
    ) {
        val replyCount = msgBean.message?.replyMe.toUnreadCount()
        val atCount = msgBean.message?.atMe.toUnreadCount()
        val agreeCount = NotificationFieldResolver.selectAgreeUnread(
            primaryAgreeCount = msgBean.message?.agreeMe,
            legacyFansCount = msgBean.message?.fans
        ).count

        dispatchChannel(
            context = context,
            notificationManager = notificationManager,
            count = replyCount,
            id = ID_REPLY,
            channel = CHANNEL_REPLY,
            channelName = CHANNEL_REPLY_NAME,
            title = context.getString(R.string.tips_message_reply, replyCount),
            intent = Intent(ACTION_VIEW, Uri.parse("tblite://notifications/0"))
        )
        dispatchChannel(
            context = context,
            notificationManager = notificationManager,
            count = atCount,
            id = ID_AT,
            channel = CHANNEL_AT,
            channelName = CHANNEL_AT_NAME,
            title = context.getString(R.string.tips_message_at, atCount),
            intent = Intent(ACTION_VIEW, Uri.parse("tblite://notifications/1"))
        )
        dispatchChannel(
            context = context,
            notificationManager = notificationManager,
            count = agreeCount,
            id = ID_AGREE,
            channel = CHANNEL_AGREE,
            channelName = CHANNEL_AGREE_NAME,
            title = context.getString(R.string.tips_message_agree, agreeCount),
            intent = Intent(ACTION_VIEW, Uri.parse("tblite://notifications/2"))
        )
        broadcastCount(context, CHANNEL_TOTAL, replyCount + atCount + agreeCount)
    }

    private fun dispatchChannel(
        context: Context,
        notificationManager: NotificationManager,
        count: Int,
        id: Int,
        channel: String,
        channelName: String,
        title: String,
        intent: Intent,
    ) {
        broadcastCount(context, channel, count)
        if (count > 0 && canPostNotification(context)) {
            updateNotification(
                context = context,
                notificationManager = notificationManager,
                title = title,
                id = id,
                channel = channel,
                channelName = channelName,
                intent = intent
            )
        } else {
            notificationManager.cancel(id)
        }
    }

    private fun createChannel(
        notificationManager: NotificationManager,
        id: String,
        name: String,
    ) {
        val channel = NotificationChannel(
            id,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.group = CHANNEL_GROUP
        channel.setShowBadge(true)
        notificationManager.createNotificationChannel(channel)
    }

    private fun updateNotification(
        context: Context,
        notificationManager: NotificationManager,
        title: String,
        id: Int,
        channel: String,
        channelName: String,
        intent: Intent,
    ) {
        val notification = NotificationCompat.Builder(context, channel)
            .setSubText(channelName)
            .setContentText(context.getString(R.string.tip_touch_to_view))
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_round_drafts)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    id,
                    intent,
                    pendingIntentFlagImmutable()
                )
            )
            .setColor(ThemeUtils.getColorByAttr(context, R.attr.colorPrimary))
            .build()
        notificationManager.notify(id, notification)
    }

    private fun broadcastCount(
        context: Context,
        channel: String,
        count: Int,
    ) {
        context.sendBroadcast(
            Intent()
                .setAction(ACTION_NEW_MESSAGE)
                .putExtra("channel", channel)
                .putExtra("count", count)
        )
    }

    private fun String?.toUnreadCount(): Int {
        return this?.toIntOrNull() ?: 0
    }

    private fun canPostNotification(context: Context): Boolean {
        val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!enabled) return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
