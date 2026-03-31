package com.huanchengfly.tieba.post.utils

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PersistableBundle
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.retrofit.doIfFailure
import com.huanchengfly.tieba.post.api.retrofit.doIfSuccess
import com.huanchengfly.tieba.post.components.dialogs.LoadingDialog
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.page.destinations.WebViewPageDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import java.util.Calendar

object TiebaUtil {
    private fun ClipData.setIsSensitive(isSensitive: Boolean): ClipData = apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, isSensitive)
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun copyText(
        context: Context,
        text: String?,
        toast: String = context.getString(R.string.toast_copy_success),
        isSensitive: Boolean = false
    ) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Tieba Lite", text).setIsSensitive(isSensitive)
        cm.setPrimaryClip(clipData)
        context.toastShort(toast)
    }

    fun initAutoSign(context: Context) {
        BackgroundWorkScheduler.syncAutoSign(context)
    }

    @JvmStatic
    @JvmOverloads
    fun startSign(context: Context, showFeedback: Boolean = true) {
        when (BackgroundWorkScheduler.startSignNow(context)) {
            BackgroundWorkScheduler.StartSignResult.Started -> {
                context.appPreferences.signDay = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
                if (showFeedback) {
                    context.toastShort(R.string.toast_oksign_start)
                }
            }

            BackgroundWorkScheduler.StartSignResult.Disabled -> {
                if (showFeedback) {
                    context.toastShort(R.string.toast_sign_capability_disabled)
                }
            }

            BackgroundWorkScheduler.StartSignResult.SessionIncomplete -> {
                if (showFeedback) {
                    context.toastShort(
                        context.getString(
                            R.string.toast_sign_session_incomplete,
                            AccountUtil.getSessionHealth().toDisplayText(context)
                        )
                    )
                }
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun shareText(context: Context, text: String, title: String? = null) {
        context.startActivity(Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "${if (title != null) "「$title」\n" else ""}$text\n（分享自贴吧 Lite）"
            )
        })
    }

    suspend fun reportPost(
        context: Context,
        navigator: DestinationsNavigator,
        postId: String,
    ) {
        val dialog = LoadingDialog(context).apply { show() }
        TiebaApi.getInstance()
            .checkReportPostAsync(postId)
            .doIfSuccess {
                dialog.dismiss()
                navigator.navigate(
                    WebViewPageDestination(it.data.url)
                )
            }
            .doIfFailure {
                dialog.dismiss()
                context.toastShort(R.string.toast_load_failed)
            }
    }
}
