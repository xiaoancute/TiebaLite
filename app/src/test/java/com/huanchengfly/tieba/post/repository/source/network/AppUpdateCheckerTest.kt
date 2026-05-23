package com.huanchengfly.tieba.post.repository.source.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateCheckerTest {
    @Test
    fun `beta tag newer than beta version name`() {
        assertTrue(
            AppUpdateChecker.isNewerVersion(
                remoteVersion = "v4.0.0-beta.4.7",
                currentVersion = "4.0.0 Beta 4.6"
            )
        )
    }

    @Test
    fun `same beta tag and version name is not newer`() {
        assertFalse(
            AppUpdateChecker.isNewerVersion(
                remoteVersion = "v4.0.0-beta.4.6",
                currentVersion = "4.0.0 Beta 4.6"
            )
        )
    }

    @Test
    fun `older beta tag is not newer`() {
        assertFalse(
            AppUpdateChecker.isNewerVersion(
                remoteVersion = "v4.0.0-beta.4.5",
                currentVersion = "4.0.0 Beta 4.6"
            )
        )
    }

    @Test
    fun `patch release newer than beta version`() {
        assertTrue(
            AppUpdateChecker.isNewerVersion(
                remoteVersion = "v4.0.1",
                currentVersion = "4.0.0 Beta 4.6"
            )
        )
    }
}
