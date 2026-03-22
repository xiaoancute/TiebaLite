package com.huanchengfly.tieba.post.revival

import android.content.Context
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.utils.appPreferences

enum class CapabilityState {
    PublicStable,
    AccountCore,
    Experimental,
    Disabled,
}

enum class RevivalFeatureGate {
    PublicBrowse,
    TopicDetail,
    Login,
    ManualSign,
    Notifications,
    AutoSign,
    Posting,
    Replying,
}

object RevivalFeatureRegistry {
    fun state(context: Context, feature: RevivalFeatureGate): CapabilityState =
        when (feature) {
            RevivalFeatureGate.PublicBrowse,
            RevivalFeatureGate.TopicDetail
            -> CapabilityState.PublicStable

            RevivalFeatureGate.Login,
            RevivalFeatureGate.ManualSign,
            RevivalFeatureGate.Notifications
            -> CapabilityState.AccountCore

            RevivalFeatureGate.AutoSign,
            RevivalFeatureGate.Posting,
            RevivalFeatureGate.Replying
            -> {
                if (context.appPreferences.showExperimentalFeatures) {
                    CapabilityState.Experimental
                } else {
                    CapabilityState.Disabled
                }
            }
        }

    fun isEnabled(context: Context, feature: RevivalFeatureGate): Boolean =
        state(context, feature) != CapabilityState.Disabled

    fun buildSettingsSummary(context: Context): String =
        context.getString(
            R.string.summary_revival_status,
            state(context, RevivalFeatureGate.PublicBrowse).label(context),
            state(context, RevivalFeatureGate.Login).label(context),
            state(context, RevivalFeatureGate.AutoSign).label(context),
        )

    fun buildReadingFirstSummary(context: Context): String =
        context.getString(R.string.summary_reading_first_scope)

    fun buildHomeBrowseSummary(context: Context): String =
        context.getString(R.string.summary_public_browse_priority_home)

    fun buildExploreBrowseSummary(context: Context, loggedIn: Boolean): String =
        context.getString(
            if (loggedIn) {
                R.string.summary_public_browse_priority_explore_logged_in
            } else {
                R.string.summary_public_browse_priority_explore_logged_out
            }
        )

    fun buildAboutSummary(context: Context): String =
        context.getString(
            R.string.summary_about_revival_scope,
            buildSettingsSummary(context),
        )

    fun buildAccountManageSummary(
        context: Context,
        accountName: String?,
        sessionHealth: SessionHealth,
    ): String =
        if (accountName.isNullOrBlank()) {
            context.getString(
                R.string.summary_account_manage_revival_logged_out,
                state(context, RevivalFeatureGate.Login).label(context),
            )
        } else {
            context.getString(
                R.string.summary_account_manage_revival_logged_in,
                accountName,
                state(context, RevivalFeatureGate.Login).label(context),
                sessionHealth.toDisplayText(context),
            )
        }

    fun buildOKSignSummary(context: Context): String =
        context.getString(
            R.string.summary_settings_oksign_revival,
            state(context, RevivalFeatureGate.ManualSign).label(context),
            state(context, RevivalFeatureGate.AutoSign).label(context),
        )
}

fun CapabilityState.label(context: Context): String =
    when (this) {
        CapabilityState.PublicStable -> context.getString(R.string.capability_state_public_stable)
        CapabilityState.AccountCore -> context.getString(R.string.capability_state_account_core)
        CapabilityState.Experimental -> context.getString(R.string.capability_state_experimental)
        CapabilityState.Disabled -> context.getString(R.string.capability_state_disabled)
    }
