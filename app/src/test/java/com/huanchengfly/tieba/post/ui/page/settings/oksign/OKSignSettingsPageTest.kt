package com.huanchengfly.tieba.post.ui.page.settings.oksign

import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.utils.OKSignPreferenceKeys
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OKSignSettingsPageTest {
    @Test
    fun stopOnFailureSettingUsesStablePreferenceKeyAndDefaults() {
        val spec = buildOKSignStopOnFailureSettingSpec()

        assertEquals(OKSignPreferenceKeys.STOP_ON_FAILURE, spec.key)
        assertEquals("oksign_stop_on_failure", spec.key)
        assertTrue(spec.defaultChecked)
        assertEquals(R.string.title_oksign_stop_on_failure, spec.titleResId)
        assertEquals(R.string.summary_oksign_stop_on_failure, spec.summaryResId)
    }
}
