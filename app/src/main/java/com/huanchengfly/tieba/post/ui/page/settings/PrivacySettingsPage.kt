package com.huanchengfly.tieba.post.ui.page.settings

import android.content.Intent
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AddModerator
import androidx.compose.material.icons.outlined.ContentPasteSearch
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.huanchengfly.tieba.post.BuildConfig
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.repository.user.Settings
import com.huanchengfly.tieba.post.ui.models.settings.PrivacySettings
import com.huanchengfly.tieba.post.ui.widgets.compose.LocalSnackbarHostState
import com.huanchengfly.tieba.post.ui.widgets.compose.preference.SegmentedPreference
import com.huanchengfly.tieba.post.ui.widgets.compose.preference.SettingsSegmentedPrefsScope
import com.huanchengfly.tieba.post.ui.widgets.compose.preference.toggleablePreference
import com.huanchengfly.tieba.post.utils.buildAppSettingsIntent
import kotlinx.coroutines.launch

@Composable
fun PrivacySettingsPage(settings: Settings<PrivacySettings>, onBack: () -> Unit) {
    SettingsScaffold(
        titleRes = R.string.title_settings_privacy,
        onBack = onBack,
        settings = settings,
        initialValue = PrivacySettings(),
    ) {
        appLinkPreference()

        incognitoModePreference()

        notificationPermissionPromptPreference()

        notificationSettingsPreference()

        clipboardPreference()
    }
}

fun SettingsSegmentedPrefsScope<PrivacySettings>.incognitoModePreference() {
    toggleablePreference(
        property = PrivacySettings::incognitoMode,
        title = R.string.settings_incognito_mode,
        summary = R.string.summary_incognito_mode,
        leadingIcon = Icons.Outlined.AddModerator,
    )
}

fun SettingsSegmentedPrefsScope<PrivacySettings>.appLinkPreference() = customPreference { shapes ->
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current

    SegmentedPreference(
        title = R.string.title_settings_app_link,
        shapes = shapes,
        summary = R.string.summary_app_link,
        leadingIcon = Icons.AutoMirrored.Outlined.OpenInNew,
        onClick = {
            runCatching {
                context.startActivity(
                    buildAppSettingsIntent(BuildConfig.APPLICATION_ID).apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            action = android.provider.Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
                        }
                    }
                )
            }
            .onFailure {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.error_open_settings))
                }
            }
        }
    )
}

fun SettingsSegmentedPrefsScope<PrivacySettings>.notificationPermissionPromptPreference() {
    toggleablePreference(
        property = PrivacySettings::requestNotificationPermission,
        title = R.string.title_settings_notification_permission_prompt,
        summaryOn = R.string.summary_notification_permission_prompt_on,
        summaryOff = R.string.summary_notification_permission_prompt_off,
        leadingIcon = Icons.Outlined.NotificationsActive,
        enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    )
}

fun SettingsSegmentedPrefsScope<PrivacySettings>.notificationSettingsPreference() = customPreference { shapes ->
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current

    SegmentedPreference(
        title = R.string.title_settings_notification_permission_system,
        shapes = shapes,
        summary = R.string.summary_notification_permission_system,
        leadingIcon = Icons.AutoMirrored.Outlined.OpenInNew,
        onClick = {
            runCatching {
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID)
                    }
                } else {
                    buildAppSettingsIntent(BuildConfig.APPLICATION_ID)
                }
                context.startActivity(intent)
            }
            .onFailure {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.error_open_settings))
                }
            }
        }
    )
}

fun SettingsSegmentedPrefsScope<PrivacySettings>.clipboardPreference() {
    toggleablePreference(
        property = PrivacySettings::readClipBoardLink,
        title = R.string.title_settings_clipboard_link,
        summary = R.string.summary_clipboard_link,
        leadingIcon = Icons.Outlined.ContentPasteSearch
    )
}
