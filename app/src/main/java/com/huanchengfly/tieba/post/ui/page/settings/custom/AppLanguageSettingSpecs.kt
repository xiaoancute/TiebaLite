package com.huanchengfly.tieba.post.ui.page.settings.custom

import androidx.annotation.StringRes
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.utils.AppLanguageManager

data class AppLanguageListSettingSpec(
    val key: String,
    val defaultValue: String,
    @StringRes val titleResId: Int,
    val entryResIds: Map<String, Int>,
)

fun buildAppLanguageSettingSpec(): AppLanguageListSettingSpec =
    AppLanguageListSettingSpec(
        key = AppLanguageManager.PREF_KEY,
        defaultValue = AppLanguageManager.VALUE_SYSTEM,
        titleResId = R.string.title_settings_app_language,
        entryResIds = linkedMapOf(
            AppLanguageManager.VALUE_SYSTEM to R.string.settings_app_language_follow_system,
            AppLanguageManager.VALUE_ZH_HANS to R.string.settings_app_language_zh_hans,
            AppLanguageManager.VALUE_ZH_HANT to R.string.settings_app_language_zh_hant,
            AppLanguageManager.VALUE_EN to R.string.settings_app_language_en,
        ),
    )
