package com.huanchengfly.tieba.post.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdatePolicyTest {
    @Test
    fun autoCheckRunsWhenEnabledAndIntervalElapsed() {
        assertTrue(
            shouldRunAutoUpdateCheck(
                enabled = true,
                lastCheckedAt = 0L,
                now = 1_000L,
            )
        )
        assertFalse(
            shouldRunAutoUpdateCheck(
                enabled = true,
                lastCheckedAt = 1_000L,
                now = 2_000L,
            )
        )
    }

    @Test
    fun matchingHigherVersionWithApkProducesAvailableDecision() {
        val manifest = AppUpdateManifest(
            repo = AppUpdateConfig.repoPath,
            channel = "recovery",
            versionCode = 390109,
            versionName = "4.0.0-recovery.12",
            tagName = "v4.0.0-recovery.12",
            prerelease = true,
            changelog = "## Changes",
            apkName = "release.apk",
            apkUrl = "https://example.com/release.apk",
            sha256 = "abc"
        )
        val localState = AppUpdateLocalState(
            currentVersionCode = 390108,
            channel = "recovery",
            ignoredVersionCode = 0,
            autoCheckEnabled = true,
            lastCheckedAt = 0L,
        )

        assertEquals(
            AppUpdateDecision.Available(manifest),
            resolveUpdateDecision(localState, manifest)
        )
    }

    @Test
    fun ignoredVersionIsSilentForAutoButStillVisibleManually() {
        val manifest = AppUpdateManifest(
            repo = AppUpdateConfig.repoPath,
            channel = "recovery",
            versionCode = 390109,
            versionName = "4.0.0-recovery.12",
            apkName = "release.apk",
            apkUrl = "https://example.com/release.apk",
        )
        val decision = AppUpdateDecision.Ignored(manifest)

        assertEquals(
            AppUpdateUiReaction.Noop,
            toUpdateUiReaction(AppUpdateCheckSource.AUTO, decision)
        )
        assertEquals(
            AppUpdateUiReaction.ShowUpdateDialog(manifest),
            toUpdateUiReaction(AppUpdateCheckSource.MANUAL, decision)
        )
    }
}
