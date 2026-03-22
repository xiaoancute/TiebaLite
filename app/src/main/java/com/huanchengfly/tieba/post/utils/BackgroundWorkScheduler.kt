package com.huanchengfly.tieba.post.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.huanchengfly.tieba.post.revival.RevivalFeatureGate
import com.huanchengfly.tieba.post.revival.RevivalFeatureRegistry
import com.huanchengfly.tieba.post.workers.NotifyWorker
import com.huanchengfly.tieba.post.workers.OKSignWorker
import com.huanchengfly.tieba.post.utils.appPreferences
import java.util.Calendar
import java.util.concurrent.TimeUnit

object BackgroundWorkScheduler {
    private const val AUTO_SIGN_WORK = "auto_sign_work"
    private const val NOTIFICATION_POLL_WORK = "notification_poll_work"
    private const val NOTIFICATION_REFRESH_WORK = "notification_refresh_work"
    private const val OKSIGN_WORK = "oksign_work"

    private val connectedNetworkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    enum class StartSignResult {
        Started,
        Disabled,
        SessionIncomplete,
    }

    fun syncAutoSign(context: Context) {
        val workManager = WorkManager.getInstance(context)
        if (
            !context.appPreferences.autoSign ||
            !RevivalFeatureRegistry.isEnabled(context, RevivalFeatureGate.AutoSign) ||
            !AccountUtil.hasCompleteSession()
        ) {
            workManager.cancelUniqueWork(AUTO_SIGN_WORK)
            return
        }
        val request = PeriodicWorkRequestBuilder<OKSignWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(nextAutoSignDelayMillis(context), TimeUnit.MILLISECONDS)
            .setConstraints(connectedNetworkConstraints)
            .build()
        workManager.enqueueUniquePeriodicWork(
            AUTO_SIGN_WORK,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    fun ensureNotificationPolling(context: Context) {
        if (
            !RevivalFeatureRegistry.isEnabled(context, RevivalFeatureGate.Notifications) ||
            !AccountUtil.hasCompleteSession()
        ) {
            cancelNotificationPolling(context)
            return
        }
        val request = PeriodicWorkRequestBuilder<NotifyWorker>(30, TimeUnit.MINUTES)
            .setConstraints(connectedNetworkConstraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            NOTIFICATION_POLL_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancelNotificationPolling(context: Context) {
        WorkManager.getInstance(context).apply {
            cancelUniqueWork(NOTIFICATION_POLL_WORK)
            cancelUniqueWork(NOTIFICATION_REFRESH_WORK)
        }
    }

    fun refreshNotificationsNow(context: Context) {
        if (
            !RevivalFeatureRegistry.isEnabled(context, RevivalFeatureGate.Notifications) ||
            !AccountUtil.hasCompleteSession()
        ) {
            cancelNotificationPolling(context)
            return
        }
        val request = OneTimeWorkRequestBuilder<NotifyWorker>()
            .setConstraints(connectedNetworkConstraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            NOTIFICATION_REFRESH_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun startSignNow(context: Context): StartSignResult {
        if (!RevivalFeatureRegistry.isEnabled(context, RevivalFeatureGate.ManualSign)) {
            return StartSignResult.Disabled
        }
        if (!AccountUtil.hasCompleteSession()) {
            return StartSignResult.SessionIncomplete
        }
        val request = OneTimeWorkRequestBuilder<OKSignWorker>()
            .setConstraints(connectedNetworkConstraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            OKSIGN_WORK,
            ExistingWorkPolicy.KEEP,
            request
        )
        return StartSignResult.Started
    }

    private fun nextAutoSignDelayMillis(context: Context): Long {
        val autoSignTime = context.appPreferences.autoSignTime ?: "09:00"
        val timeParts = autoSignTime.split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
        val nextRun = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        return (nextRun.timeInMillis - System.currentTimeMillis()).coerceAtLeast(0L)
    }
}
