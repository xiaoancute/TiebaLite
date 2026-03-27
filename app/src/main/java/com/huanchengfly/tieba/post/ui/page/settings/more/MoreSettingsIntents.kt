package com.huanchengfly.tieba.post.ui.page.settings.more

import android.content.Intent
import android.net.Uri
import android.provider.Settings

internal data class OpenByDefaultSettingsIntentSpec(
    val action: String,
    val dataUri: String,
    val flags: Int,
)

internal fun buildOpenByDefaultSettingsIntentSpec(packageName: String): OpenByDefaultSettingsIntentSpec =
    OpenByDefaultSettingsIntentSpec(
        action = Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
        dataUri = "package:$packageName",
        flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
    )

internal fun buildOpenByDefaultSettingsIntent(packageName: String): Intent {
    val spec = buildOpenByDefaultSettingsIntentSpec(packageName)
    return Intent(spec.action, Uri.parse(spec.dataUri)).addFlags(spec.flags)
}
