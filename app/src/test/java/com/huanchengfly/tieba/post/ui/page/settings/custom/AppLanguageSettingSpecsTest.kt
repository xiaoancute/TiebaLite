package com.huanchengfly.tieba.post.ui.page.settings.custom

import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.utils.AppLanguageManager
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageSettingSpecsTest {
    @Test
    fun appLanguageSettingUsesStableKeyDefaultAndEntryOrder() {
        val spec = buildAppLanguageSettingSpec()

        assertEquals(AppLanguageManager.PREF_KEY, spec.key)
        assertEquals(AppLanguageManager.VALUE_SYSTEM, spec.defaultValue)
        assertEquals(R.string.title_settings_app_language, spec.titleResId)
        assertEquals(
            listOf(
                AppLanguageManager.VALUE_SYSTEM,
                AppLanguageManager.VALUE_ZH_HANS,
                AppLanguageManager.VALUE_ZH_HANT,
                AppLanguageManager.VALUE_EN,
            ),
            spec.entryResIds.keys.toList()
        )
    }
}
