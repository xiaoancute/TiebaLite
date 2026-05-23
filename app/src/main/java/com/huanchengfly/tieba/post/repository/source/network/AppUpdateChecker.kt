package com.huanchengfly.tieba.post.repository.source.network

import com.huanchengfly.tieba.post.BuildConfig
import com.huanchengfly.tieba.post.api.models.GitHubReleaseBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

object AppUpdateChecker {
    const val PROJECT_GITHUB_URL = "https://github.com/xiaoancute/TiebaLite"
    const val RELEASES_URL = "$PROJECT_GITHUB_URL/releases"

    private const val RELEASES_API_URL =
        "https://api.github.com/repos/xiaoancute/TiebaLite/releases?per_page=10"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun checkLatestRelease(
        currentVersionName: String = BuildConfig.VERSION_NAME
    ): AppUpdateInfo = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(RELEASES_API_URL)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "TiebaLite")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("GitHub releases request failed: ${response.code}")
            }

            val releases = json.decodeFromString<List<GitHubReleaseBean>>(response.body.string())
            val release = releases.firstOrNull { !it.draft && it.htmlUrl.isNotBlank() }
                ?: throw IOException("No release found")
            val versionName = release.name?.takeIf { it.isNotBlank() } ?: release.tagName

            AppUpdateInfo(
                hasUpdate = isNewerVersion(release.tagName, currentVersionName),
                versionName = versionName,
                releaseUrl = release.htmlUrl,
                releaseNotes = release.body.orEmpty(),
                prerelease = release.prerelease,
            )
        }
    }

    internal fun isNewerVersion(remoteVersion: String, currentVersion: String): Boolean {
        val remoteParts = remoteVersion.versionParts()
        val currentParts = currentVersion.versionParts()
        if (remoteParts.isEmpty() || currentParts.isEmpty()) return false

        val size = maxOf(remoteParts.size, currentParts.size)
        repeat(size) { index ->
            val remotePart = remoteParts.getOrElse(index) { 0 }
            val currentPart = currentParts.getOrElse(index) { 0 }
            if (remotePart != currentPart) return remotePart > currentPart
        }
        return false
    }

    private fun String.versionParts(): List<Int> =
        Regex("\\d+").findAll(this).map { it.value.toInt() }.toList()
}

data class AppUpdateInfo(
    val hasUpdate: Boolean,
    val versionName: String,
    val releaseUrl: String,
    val releaseNotes: String,
    val prerelease: Boolean,
)
