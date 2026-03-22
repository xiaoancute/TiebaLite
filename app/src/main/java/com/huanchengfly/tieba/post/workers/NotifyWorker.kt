package com.huanchengfly.tieba.post.workers

import android.app.NotificationManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.services.UnreadNotificationHelper
import com.huanchengfly.tieba.post.utils.AccountUtil
import kotlinx.coroutines.flow.first

class NotifyWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        UnreadNotificationHelper.ensureChannels(applicationContext, notificationManager)
        if (!AccountUtil.hasCompleteSession()) {
            UnreadNotificationHelper.clear(applicationContext, notificationManager)
            return Result.success()
        }
        return runCatching {
            TiebaApi.getInstance().msgFlow().first()
        }.fold(
            onSuccess = {
                UnreadNotificationHelper.dispatch(applicationContext, notificationManager, it)
                Result.success()
            },
            onFailure = {
                Result.retry()
            }
        )
    }
}
