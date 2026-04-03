package com.huanchengfly.tieba.post.ui.page.settings.more

import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.update.AppUpdateConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateSettingSpecsTest {
    @Test
    fun autoCheckUpdateSettingUsesStableKeyDefaultAndLabels() {
        val spec = buildAutoCheckUpdateSettingSpec()

        assertEquals(AppUpdateConfig.AUTO_CHECK_PREF_KEY, spec.key)
        assertTrue(spec.defaultChecked)
        assertEquals(R.string.title_auto_check_app_update, spec.titleResId)
        assertEquals(R.string.tip_auto_check_app_update, spec.summaryResId)
    }
}
