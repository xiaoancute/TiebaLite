package com.huanchengfly.tieba.post.ui.page.settings

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.bumptech.glide.integration.compose.GlideImage
import com.huanchengfly.tieba.post.BuildConfig
import com.huanchengfly.tieba.post.LocalWindowAdaptiveInfo
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.components.TiebaWebView
import com.huanchengfly.tieba.post.repository.source.network.AppUpdateChecker
import com.huanchengfly.tieba.post.repository.source.network.AppUpdateInfo
import com.huanchengfly.tieba.post.theme.TiebaLiteTheme
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.common.theme.compose.clickableNoIndication
import com.huanchengfly.tieba.post.ui.icons.GitHubInvertocat
import com.huanchengfly.tieba.post.ui.icons.License
import com.huanchengfly.tieba.post.ui.page.welcome.UaWebView
import com.huanchengfly.tieba.post.ui.widgets.compose.AlertDialog
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogNegativeButton
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogPositiveButton
import com.huanchengfly.tieba.post.ui.widgets.compose.NegativeButton
import com.huanchengfly.tieba.post.ui.widgets.compose.StrongBox
import com.huanchengfly.tieba.post.ui.widgets.compose.preference.preference
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val URL_PROJECT_GITHUB = AppUpdateChecker.PROJECT_GITHUB_URL

@Composable
fun AboutPage(
    modifier: Modifier = Modifier,
    checkingUpdate: Boolean = false,
    onBackClicked: () -> Unit = {},
    onCheckUpdateClicked: () -> Unit = {},
    onDisclaimerClicked: () -> Unit = {},
    onHomePageClicked: () -> Unit = {},
    onLicenseClicked: () -> Unit = {},
) {
    val context = LocalContext.current
    val windowSizeClass = LocalWindowAdaptiveInfo.current.windowSizeClass
    val isWindowHeightExpanded = windowSizeClass.isHeightAtLeastBreakpoint(
        heightDpBreakpoint = WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND
    )
    val icons = remember {
        listOf(
            R.mipmap.ic_launcher_new_round,
            R.mipmap.ic_launcher_new_invert_round,
            R.mipmap.ic_launcher_round,
        )
    }

    val buildTime = remember {
        val buildDate = Date(BuildConfig.BUILD_TIME * 1000)
        // DateTimeFormatter#ISO_INSTANT
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(buildDate)
    }

    SettingsScaffold(
        modifier = modifier,
        titleRes = R.string.title_about,
        titleHorizontalAlignment = Alignment.CenterHorizontally,
        onBack = onBackClicked,
    ) {
        customPreference {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isWindowHeightExpanded) {
                    Spacer(modifier = Modifier.height(48.dp))
                } else {
                    Spacer(modifier = Modifier.height(36.dp))
                }

                StrongBox {
                    var iconIndex by rememberSaveable { mutableIntStateOf(0) }
                    GlideImage(
                        model = icons[iconIndex],
                        contentDescription = null,
                        modifier = Modifier
                            .size(96.dp)
                            .clickableNoIndication {
                                iconIndex = (iconIndex + 1).takeIf { it in icons.indices } ?: 0 // Loop icons
                            }
                    )
                }

                Image(
                    painter = painterResource(R.drawable.ic_splash_text),
                    contentDescription = null,
                    modifier = Modifier
                        .size(240.dp, 96.dp)
                        .offset(y = (-8).dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Text(
                    text = stringResource(R.string.welcome_intro_subtitle),
                    modifier = Modifier.offset(y = (-24).dp),
                    style = MaterialTheme.typography.titleMedium
                )

                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                    Column(
                        modifier = Modifier.offset(y = -(20).dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                        Text(text = "${BuildConfig.BUILD_TYPE}#${BuildConfig.BUILD_GIT}")
                        Text(text = buildTime)
                    }
                }
            }
        }

        customPreference {
            if (isWindowHeightExpanded){
                Spacer(modifier = Modifier.height(96.dp))
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        group {
            preference(
                title = context.getString(R.string.title_disclaimer),
                icon = Icons.Rounded.Info,
                onClick = onDisclaimerClicked
            )

            preference(
                title = context.getString(R.string.title_check_update),
                summary = context.getString(
                    if (checkingUpdate) R.string.summary_check_update_loading else R.string.summary_check_update
                ),
                icon = Icons.Outlined.Refresh,
                enabled = !checkingUpdate,
                onClick = onCheckUpdateClicked,
            )

            preference(
                title = context.getString(R.string.about_source_code),
                summary = URL_PROJECT_GITHUB,
                icon = GitHubInvertocat,
                onClick = onHomePageClicked,
            )

            preference(
                title = context.getString(R.string.about_license),
                summary = "GNU GENERAL PUBLIC LICENSE Version 3",
                icon = Icons.Rounded.License,
                onClick = onLicenseClicked,
            )
        }
    }
}

@Composable
fun AboutPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val disclaimerDialogState = rememberDialogState()
    val updateDialogState = rememberDialogState()
    var checkingUpdate by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }

    fun launchCustomTab(url: String) {
        TiebaWebView.launchCustomTab(context, Uri.parse(url))
    }

    fun checkUpdate() {
        if (checkingUpdate) return

        coroutineScope.launch {
            checkingUpdate = true
            runCatching {
                AppUpdateChecker.checkLatestRelease()
            }.onSuccess { info ->
                if (info.hasUpdate) {
                    updateInfo = info
                    updateDialogState.show()
                } else {
                    context.toastShort(R.string.toast_no_update_available)
                }
            }.onFailure {
                context.toastShort(R.string.toast_exception, it.message ?: context.getString(R.string.error_unknown))
            }
            checkingUpdate = false
        }
    }

    AboutPage(
        checkingUpdate = checkingUpdate,
        onBackClicked = onBack,
        onCheckUpdateClicked = ::checkUpdate,
        onDisclaimerClicked = disclaimerDialogState::show,
        onHomePageClicked = { launchCustomTab(URL_PROJECT_GITHUB) },
        onLicenseClicked = { launchCustomTab("${URL_PROJECT_GITHUB}/blob/main/LICENSE") },
    )

    AlertDialog(
        dialogState = disclaimerDialogState,
        buttons = {
            NegativeButton(text = stringResource(R.string.btn_close)) {
                disclaimerDialogState.show = false
            }
        }
    ) {
        UaWebView(modifier = Modifier.height(480.dp))
    }

    updateInfo?.let { info ->
        AlertDialog(
            dialogState = updateDialogState,
            title = {
                Text(text = stringResource(R.string.dialog_title_update_available))
            },
            buttons = {
                DialogNegativeButton(text = stringResource(R.string.btn_close))
                DialogPositiveButton(text = stringResource(R.string.button_open)) {
                    launchCustomTab(info.releaseUrl)
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = stringResource(R.string.dialog_message_update_available, info.versionName))
                if (info.releaseNotes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = info.releaseNotes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview("AboutPage", showBackground = true, backgroundColor = -1L)
@Composable
private fun AboutPagePreview() = TiebaLiteTheme {
    AboutPage()
}
