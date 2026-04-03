package com.huanchengfly.tieba.post.ui.page.settings.more

import androidx.annotation.StringRes
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.update.AppUpdateConfig

data class AppUpdateSettingSpec(
    val key: String,
    val defaultChecked: Boolean,
    @StringRes val titleResId: Int,
    @StringRes val summaryResId: Int,
)

fun buildAutoCheckUpdateSettingSpec(): AppUpdateSettingSpec =
    AppUpdateSettingSpec(
        key = AppUpdateConfig.AUTO_CHECK_PREF_KEY,
        defaultChecked = true,
        titleResId = R.string.title_auto_check_app_update,
        summaryResId = R.string.tip_auto_check_app_update,
    )
