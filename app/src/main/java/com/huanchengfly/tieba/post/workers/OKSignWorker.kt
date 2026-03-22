package com.huanchengfly.tieba.post.workers

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.SignResultBean
import com.huanchengfly.tieba.post.models.SignDataBean
import com.huanchengfly.tieba.post.pendingIntentFlagImmutable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.ProgressListener
import com.huanchengfly.tieba.post.utils.SingleAccountSigner
import com.huanchengfly.tieba.post.utils.appPreferences
import com.huanchengfly.tieba.post.utils.extension.addFlag
import java.util.Calendar

class OKSignWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), ProgressListener {
    private var lastSignData: SignDataBean? = null

    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(applicationContext)
    }

    override suspend fun doWork(): Result {
        applicationContext.appPreferences.signDay = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
        val loginInfo = AccountUtil.getLoginInfo()
        if (loginInfo == null) {
            showCompletionNotification(
                applicationContext.getString(R.string.title_oksign_fail),
                applicationContext.getString(R.string.text_login_first)
            )
            return Result.success()
        }
        val sessionHealth = AccountUtil.getSessionHealth(loginInfo)
        if (!sessionHealth.isComplete) {
            showCompletionNotification(
                applicationContext.getString(R.string.title_oksign_fail),
                sessionHealth.toDisplayText(applicationContext)
            )
            return Result.success()
        }
        setForeground(
            createForegroundInfo(
                applicationContext.getString(R.string.title_loading_data),
                applicationContext.getString(R.string.text_please_wait)
            )
        )
        return runCatching {
            SingleAccountSigner(applicationContext, loginInfo)
                .apply { setProgressListener(this@OKSignWorker) }
                .start()
        }.fold(
            onSuccess = { Result.success() },
            onFailure = {
                showCompletionNotification(
                    applicationContext.getString(R.string.title_oksign_fail),
                    it.localizedMessage ?: applicationContext.getString(R.string.text_login_failed_default)
                )
                Result.success()
            }
        )
    }

    override fun onStart(total: Int) {
        updateProgressNotification(
            applicationContext.getString(R.string.title_start_sign),
            null
        )
    }

    override fun onProgressStart(signDataBean: SignDataBean, current: Int, total: Int) {
        lastSignData = signDataBean
        updateProgressNotification(
            applicationContext.getString(
                R.string.title_signing_progress,
                signDataBean.userName,
                current,
                total
            ),
            applicationContext.getString(
                R.string.title_forum_name,
                signDataBean.forumName
            )
        )
    }

    override fun onProgressFinish(
        signDataBean: SignDataBean,
        signResultBean: SignResultBean,
        current: Int,
        total: Int,
    ) {
        updateProgressNotification(
            applicationContext.getString(
                R.string.title_signing_progress,
                signDataBean.userName,
                current + 1,
                total
            ),
            if (signResultBean.userInfo?.signBonusPoint != null) {
                applicationContext.getString(
                    R.string.text_singing_progress_exp,
                    signDataBean.forumName,
                    signResultBean.userInfo.signBonusPoint
                )
            } else {
                applicationContext.getString(R.string.text_singing_progress, signDataBean.forumName)
            }
        )
    }

    override fun onFinish(success: Boolean, signedCount: Int, total: Int) {
        showCompletionNotification(
            applicationContext.getString(R.string.title_oksign_finish),
            if (total > 0) {
                applicationContext.getString(R.string.text_oksign_done, signedCount)
            } else {
                applicationContext.getString(R.string.text_oksign_no_signable)
            },
            applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        )
        applicationContext.sendBroadcast(Intent(OKSignWork.ACTION_SIGN_SUCCESS_ALL))
    }

    override fun onFailure(current: Int, total: Int, errorCode: Int, errorMsg: String) {
        val signData = lastSignData
        if (signData == null) {
            showCompletionNotification(
                applicationContext.getString(R.string.title_oksign_fail),
                errorMsg
            )
        } else {
            updateProgressNotification(
                applicationContext.getString(
                    R.string.title_signing_progress,
                    signData.userName,
                    current + 1,
                    total
                ),
                applicationContext.getString(
                    R.string.text_singing_progress_fail,
                    signData.forumName,
                    errorMsg
                )
            )
        }
    }

    private fun createNotification(title: String, text: String?, ongoing: Boolean): Notification {
        createNotificationChannel()
        return NotificationCompat.Builder(applicationContext, OKSignWork.NOTIFICATION_CHANNEL_ID)
            .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
            .setContentText(text)
            .setContentTitle(title)
            .setSubText(applicationContext.getString(R.string.title_oksign))
            .setSmallIcon(R.drawable.ic_oksign)
            .setAutoCancel(!ongoing)
            .setOngoing(ongoing)
            .setStyle(NotificationCompat.BigTextStyle())
            .setColor(ThemeUtils.getColorByAttr(applicationContext, R.attr.colorPrimary))
            .build()
    }

    private fun createForegroundInfo(title: String, text: String?): ForegroundInfo {
        val notification = createNotification(title, text, ongoing = true).apply {
            flags = flags.addFlag(NotificationCompat.FLAG_ONGOING_EVENT)
        }
        return ForegroundInfo(OKSignWork.NOTIFICATION_ID, notification)
    }

    private fun updateProgressNotification(title: String, text: String?) {
        setForegroundAsync(createForegroundInfo(title, text))
    }

    private fun showCompletionNotification(
        title: String,
        text: String,
        intent: Intent? = null,
    ) {
        if (!canPostNotification()) {
            return
        }
        val notification = createNotification(title, text, ongoing = false)
        val finalNotification = if (intent != null) {
            NotificationCompat.Builder(applicationContext, OKSignWork.NOTIFICATION_CHANNEL_ID)
                .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
                .setContentText(text)
                .setContentTitle(title)
                .setSubText(applicationContext.getString(R.string.title_oksign))
                .setSmallIcon(R.drawable.ic_oksign)
                .setAutoCancel(true)
                .setStyle(NotificationCompat.BigTextStyle())
                .setColor(ThemeUtils.getColorByAttr(applicationContext, R.attr.colorPrimary))
                .setContentIntent(
                    PendingIntent.getActivity(
                        applicationContext,
                        0,
                        intent,
                        pendingIntentFlagImmutable()
                    )
                )
                .build()
        } else {
            notification
        }
        notificationManager.notify(OKSignWork.NOTIFICATION_ID, finalNotification)
    }

    private fun createNotificationChannel() {
        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(
                OKSignWork.NOTIFICATION_CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_LOW
            )
                .setName(applicationContext.getString(R.string.title_oksign))
                .setLightsEnabled(false)
                .setShowBadge(false)
                .build()
        )
    }

    private fun canPostNotification(): Boolean {
        return ActivityCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
