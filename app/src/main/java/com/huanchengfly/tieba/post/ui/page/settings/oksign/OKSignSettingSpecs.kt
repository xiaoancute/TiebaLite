package com.huanchengfly.tieba.post.ui.page.settings.oksign

import androidx.annotation.StringRes
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.utils.OKSignPreferenceKeys

data class OKSignToggleSettingSpec(
    val key: String,
    val defaultChecked: Boolean,
    @StringRes val titleResId: Int,
    @StringRes val summaryResId: Int,
)

fun buildOKSignStopOnFailureSettingSpec(): OKSignToggleSettingSpec =
    OKSignToggleSettingSpec(
        key = OKSignPreferenceKeys.STOP_ON_FAILURE,
        defaultChecked = true,
        titleResId = R.string.title_oksign_stop_on_failure,
        summaryResId = R.string.summary_oksign_stop_on_failure,
    )
