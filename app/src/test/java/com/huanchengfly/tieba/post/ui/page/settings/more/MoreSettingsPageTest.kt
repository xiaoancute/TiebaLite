package com.huanchengfly.tieba.post.ui.page.settings.more

import android.content.Intent
import android.provider.Settings
import org.junit.Assert.assertEquals
import org.junit.Test

class MoreSettingsPageTest {
    @Test
    fun openByDefaultIntentSpecUsesAppDefaultsSettingsAction() {
        val spec = buildOpenByDefaultSettingsIntentSpec("com.huanchengfly.tieba.post")

        assertEquals(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, spec.action)
        assertEquals("package:com.huanchengfly.tieba.post", spec.dataUri)
        assertEquals(
            Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
            spec.flags
        )
    }
}
