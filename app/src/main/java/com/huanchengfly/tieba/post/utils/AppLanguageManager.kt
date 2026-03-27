package com.huanchengfly.tieba.post.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object AppLanguageManager {
    const val PREF_KEY = "app_language"
    const val VALUE_SYSTEM = "system"
    const val VALUE_ZH_HANS = "zh-Hans"
    const val VALUE_ZH_HANT = "zh-Hant"
    const val VALUE_EN = "en"
}

data class AppLanguageSpec(
    val preferenceValue: String,
    val languageTags: String?,
)

fun buildAppLanguageSpec(rawValue: String?): AppLanguageSpec {
    val normalized = rawValue?.takeUnless { it.isBlank() }
    return when (normalized) {
        AppLanguageManager.VALUE_ZH_HANS -> AppLanguageSpec(
            preferenceValue = AppLanguageManager.VALUE_ZH_HANS,
            languageTags = AppLanguageManager.VALUE_ZH_HANS,
        )
        AppLanguageManager.VALUE_ZH_HANT -> AppLanguageSpec(
            preferenceValue = AppLanguageManager.VALUE_ZH_HANT,
            languageTags = AppLanguageManager.VALUE_ZH_HANT,
        )
        AppLanguageManager.VALUE_EN -> AppLanguageSpec(
            preferenceValue = AppLanguageManager.VALUE_EN,
            languageTags = AppLanguageManager.VALUE_EN,
        )
        else -> AppLanguageSpec(
            preferenceValue = AppLanguageManager.VALUE_SYSTEM,
            languageTags = null,
        )
    }
}

fun buildAppLanguageLocales(rawValue: String?): LocaleListCompat {
    val spec = buildAppLanguageSpec(rawValue)
    return spec.languageTags?.let(LocaleListCompat::forLanguageTags)
        ?: LocaleListCompat.getEmptyLocaleList()
}

fun applyAppLanguage(rawValue: String?) {
    AppCompatDelegate.setApplicationLocales(buildAppLanguageLocales(rawValue))
}

fun applySavedAppLanguage(context: Context) {
    applyAppLanguage(context.applicationContext.appPreferences.appLanguage)
}
