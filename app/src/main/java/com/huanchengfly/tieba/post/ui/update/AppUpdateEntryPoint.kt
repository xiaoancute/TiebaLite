package com.huanchengfly.tieba.post.ui.update

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.onGlobalEvent
import com.huanchengfly.tieba.post.repository.AppUpdateRepository
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.post.update.AppUpdateCheckSource
import com.huanchengfly.tieba.post.update.AppUpdateConfig
import com.huanchengfly.tieba.post.update.AppUpdateDecision
import com.huanchengfly.tieba.post.update.AppUpdateLocalState
import com.huanchengfly.tieba.post.update.AppUpdateManifest
import com.huanchengfly.tieba.post.update.AppUpdateUiReaction
import com.huanchengfly.tieba.post.update.toUpdateUiReaction
import com.huanchengfly.tieba.post.utils.FileUtil
import com.huanchengfly.tieba.post.utils.appPreferences

@Composable
fun AppUpdateEntryPoint(
    repository: AppUpdateRepository = remember { AppUpdateRepository() },
) {
    val context = LocalContext.current
    val dialogState = rememberDialogState()
    var showingManifest by remember { mutableStateOf<AppUpdateManifest?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    suspend fun runCheck(source: AppUpdateCheckSource) {
        if (isChecking) return
        isChecking = true
        try {
            val now = System.currentTimeMillis()
            val localState = AppUpdateLocalState(
                currentVersionCode = AppUpdateConfig.currentVersionCode,
                channel = AppUpdateConfig.currentChannel,
                ignoredVersionCode = context.appPreferences.ignoredUpdateVersionCode,
                autoCheckEnabled = context.appPreferences.autoCheckAppUpdate,
                lastCheckedAt = context.appPreferences.lastAppUpdateCheckAt,
            )
            val decision = repository.check(localState, source, now)
            if (decision !is AppUpdateDecision.Skipped) {
                context.appPreferences.lastAppUpdateCheckAt = now
            }
            when (val reaction = toUpdateUiReaction(source, decision)) {
                is AppUpdateUiReaction.ShowUpdateDialog -> {
                    showingManifest = reaction.manifest
                    dialogState.show()
                }

                AppUpdateUiReaction.ShowLatestToast -> {
                    context.toastShort(R.string.message_update_already_latest)
                }

                AppUpdateUiReaction.ShowFailureToast -> {
                    context.toastShort(R.string.toast_update_check_failed)
                }

                AppUpdateUiReaction.Noop -> Unit
            }
        } finally {
            isChecking = false
        }
    }

    showingManifest?.let { manifest ->
        AppUpdateDialog(
            dialogState = dialogState,
            manifest = manifest,
            onDownload = {
                FileUtil.downloadBySystem(
                    context = context,
                    fileType = FileUtil.FILE_TYPE_DOWNLOAD,
                    url = manifest.apkUrl,
                    fileName = manifest.apkName ?: "TiebaLite-update.apk",
                )
            },
            onIgnore = {
                context.appPreferences.ignoredUpdateVersionCode = manifest.versionCode ?: 0
            },
        )
    }

    LaunchedEffect(Unit) {
        runCheck(AppUpdateCheckSource.AUTO)
    }

    onGlobalEvent<GlobalEvent.CheckAppUpdate> {
        runCheck(if (it.manual) AppUpdateCheckSource.MANUAL else AppUpdateCheckSource.AUTO)
    }
}
