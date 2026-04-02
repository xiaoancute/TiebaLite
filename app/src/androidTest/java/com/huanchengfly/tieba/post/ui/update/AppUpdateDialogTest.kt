package com.huanchengfly.tieba.post.ui.update

import androidx.activity.ComponentActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.TiebaLiteTheme
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.post.update.AppUpdateManifest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppUpdateDialogTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun updateDialogShowsVersionAndButtons() {
        composeRule.setContent {
            val dialogState = rememberDialogState()
            LaunchedEffect(Unit) { dialogState.show() }

            TiebaLiteTheme {
                AppUpdateDialog(
                    dialogState = dialogState,
                    manifest = AppUpdateManifest(
                        versionName = "4.0.0-recovery.12",
                        publishedAt = "2026-04-01T12:00:00Z",
                        changelog = "## Changes\n- Fix startup checks",
                        apkUrl = "https://example.com/release.apk",
                        apkName = "release.apk"
                    ),
                    onDownload = {},
                    onIgnore = {},
                )
            }
        }

        composeRule.onNodeWithText("4.0.0-recovery.12").assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.button_download_update)
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.button_ignore_this_version)
        ).assertIsDisplayed()
    }

    @Test
    fun updateDialogHidesDownloadWhenApkUrlMissing() {
        composeRule.setContent {
            val dialogState = rememberDialogState()
            LaunchedEffect(Unit) { dialogState.show() }

            TiebaLiteTheme {
                AppUpdateDialog(
                    dialogState = dialogState,
                    manifest = AppUpdateManifest(
                        versionName = "4.0.0-recovery.12",
                        changelog = "## Changes"
                    ),
                    onDownload = {},
                    onIgnore = {},
                )
            }
        }

        composeRule.assertTextAbsent(composeRule.activity.getString(R.string.button_download_update))
    }

    private fun ComposeTestRule.assertTextAbsent(text: String) {
        assertTrue(onAllNodesWithText(text).fetchSemanticsNodes().isEmpty())
    }
}
