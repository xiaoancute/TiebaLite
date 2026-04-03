package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.retrofit.interfaces.GitHubReleaseApi
import com.huanchengfly.tieba.post.update.AppUpdateCheckSource
import com.huanchengfly.tieba.post.update.AppUpdateDecision
import com.huanchengfly.tieba.post.update.AppUpdateLocalState
import com.huanchengfly.tieba.post.update.GitHubReleaseAsset
import com.huanchengfly.tieba.post.update.GitHubReleaseSummary
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateRepositoryTest {
    @Test
    fun checkLoadsManifestFromReleaseAssetAndReturnsAvailableDecision() = runBlocking {
        val api = FakeGitHubReleaseApi(
            releases = listOf(
                GitHubReleaseSummary(
                    tagName = "v4.0.0-recovery.12",
                    prerelease = true,
                    publishedAt = "2026-04-01T12:00:00Z",
                    assets = listOf(
                        GitHubReleaseAsset(
                            name = "update.json",
                            browserDownloadUrl = "https://example.com/update.json"
                        )
                    )
                )
            ),
            responseBodies = mapOf(
                "https://example.com/update.json" to """
                    {
                      "repo": "xiaoancute/TiebaLite",
                      "channel": "recovery",
                      "versionCode": 390109,
                      "versionName": "4.0.0-recovery.12",
                      "tagName": "v4.0.0-recovery.12",
                      "prerelease": true,
                      "changelog": "## Changes",
                      "apkName": "release.apk",
                      "apkUrl": "https://example.com/release.apk",
                      "sha256": "abc"
                    }
                """.trimIndent()
            )
        )

        val repository = AppUpdateRepository(api)
        val result = repository.check(
            localState = AppUpdateLocalState(
                currentVersionCode = 390108,
                channel = "recovery",
                ignoredVersionCode = 0,
                autoCheckEnabled = true,
                lastCheckedAt = 0L
            ),
            source = AppUpdateCheckSource.MANUAL,
            now = 1_000L,
        )

        assertTrue(result is AppUpdateDecision.Available)
    }

    @Test
    fun checkSkipsWrongChannelManifestAndUsesLaterMatchingRelease() = runBlocking {
        val api = FakeGitHubReleaseApi(
            releases = listOf(
                GitHubReleaseSummary(
                    tagName = "v4.0.0-stable.1",
                    prerelease = false,
                    assets = listOf(
                        GitHubReleaseAsset(
                            name = "update.json",
                            browserDownloadUrl = "https://example.com/stable-update.json"
                        )
                    )
                ),
                GitHubReleaseSummary(
                    tagName = "v4.0.0-recovery.12",
                    prerelease = true,
                    assets = listOf(
                        GitHubReleaseAsset(
                            name = "update.json",
                            browserDownloadUrl = "https://example.com/recovery-update.json"
                        )
                    )
                )
            ),
            responseBodies = mapOf(
                "https://example.com/stable-update.json" to """
                    {
                      "repo": "xiaoancute/TiebaLite",
                      "channel": "stable",
                      "versionCode": 390109,
                      "versionName": "4.0.0",
                      "tagName": "v4.0.0",
                      "apkName": "stable.apk",
                      "apkUrl": "https://example.com/stable.apk",
                      "sha256": "abc"
                    }
                """.trimIndent(),
                "https://example.com/recovery-update.json" to """
                    {
                      "repo": "xiaoancute/TiebaLite",
                      "channel": "recovery",
                      "versionCode": 390110,
                      "versionName": "4.0.0-recovery.13",
                      "tagName": "v4.0.0-recovery.13",
                      "apkName": "recovery.apk",
                      "apkUrl": "https://example.com/recovery.apk",
                      "sha256": "def"
                    }
                """.trimIndent()
            )
        )

        val repository = AppUpdateRepository(api)
        val result = repository.check(
            localState = AppUpdateLocalState(
                currentVersionCode = 390108,
                channel = "recovery",
                ignoredVersionCode = 0,
                autoCheckEnabled = true,
                lastCheckedAt = 0L
            ),
            source = AppUpdateCheckSource.MANUAL,
            now = 1_000L,
        )

        assertTrue(result is AppUpdateDecision.Available)
    }

    private class FakeGitHubReleaseApi(
        private val releases: List<GitHubReleaseSummary>,
        private val responseBodies: Map<String, String>,
    ) : GitHubReleaseApi {
        override suspend fun listReleases(owner: String, repo: String): List<GitHubReleaseSummary> = releases

        override suspend fun fetchRaw(url: String): ResponseBody =
            responseBodies.getValue(url).toResponseBody("application/json".toMediaType())
    }
}
