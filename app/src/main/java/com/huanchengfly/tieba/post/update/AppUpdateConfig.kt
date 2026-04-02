package com.huanchengfly.tieba.post.update

import com.huanchengfly.tieba.post.BuildConfig

object AppUpdateConfig {
    const val REPO_OWNER = "xiaoancute"
    const val REPO_NAME = "TiebaLite"
    const val UPDATE_JSON_ASSET_NAME = "update.json"

    const val AUTO_CHECK_PREF_KEY = "auto_check_app_update"
    const val IGNORED_VERSION_CODE_PREF_KEY = "ignored_update_version_code"
    const val LAST_CHECK_AT_PREF_KEY = "last_update_check_at"

    const val AUTO_CHECK_INTERVAL_MS = 24L * 60L * 60L * 1000L

    val currentChannel: String
        get() = BuildConfig.UPDATE_CHANNEL

    val currentVersionCode: Int
        get() = BuildConfig.APP_VERSION_CODE

    val repoPath: String
        get() = "$REPO_OWNER/$REPO_NAME"
}
