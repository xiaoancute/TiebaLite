package com.huanchengfly.tieba.post.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppLanguageManagerTest {
    @Test
    fun buildAppLanguageSpecNormalizesUnknownValuesToSystem() {
        val spec = buildAppLanguageSpec("ja")

        assertEquals(AppLanguageManager.VALUE_SYSTEM, spec.preferenceValue)
        assertNull(spec.languageTags)
    }

    @Test
    fun buildAppLanguageSpecKeepsSupportedLanguageTags() {
        val zhHans = buildAppLanguageSpec(AppLanguageManager.VALUE_ZH_HANS)
        val zhHant = buildAppLanguageSpec(AppLanguageManager.VALUE_ZH_HANT)
        val en = buildAppLanguageSpec(AppLanguageManager.VALUE_EN)

        assertEquals("zh-Hans", zhHans.languageTags)
        assertEquals("zh-Hant", zhHant.languageTags)
        assertEquals("en", en.languageTags)
    }

    @Test
    fun buildAppLanguageLocalesUsesEmptyListForSystem() {
        val locales = buildAppLanguageLocales(AppLanguageManager.VALUE_SYSTEM)

        assertEquals("", locales.toLanguageTags())
    }
}
