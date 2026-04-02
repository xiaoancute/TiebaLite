package com.huanchengfly.tieba.post.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubReleaseAsset(
    @SerialName("name") val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
)

@Serializable
data class GitHubReleaseSummary(
    @SerialName("tag_name") val tagName: String,
    @SerialName("prerelease") val prerelease: Boolean = false,
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("assets") val assets: List<GitHubReleaseAsset> = emptyList(),
)

@Serializable
data class AppUpdateManifest(
    val repo: String? = null,
    val channel: String? = null,
    val versionCode: Int? = null,
    val versionName: String? = null,
    val tagName: String? = null,
    val publishedAt: String? = null,
    val prerelease: Boolean = false,
    val changelog: String? = null,
    val apkName: String? = null,
    val apkUrl: String? = null,
    val sha256: String? = null,
)

data class AppUpdateLocalState(
    val currentVersionCode: Int,
    val channel: String,
    val ignoredVersionCode: Int,
    val autoCheckEnabled: Boolean,
    val lastCheckedAt: Long,
)

enum class AppUpdateCheckSource {
    AUTO,
    MANUAL,
}

sealed interface AppUpdateDecision {
    data class Available(val manifest: AppUpdateManifest) : AppUpdateDecision
    data object UpToDate : AppUpdateDecision
    data class Ignored(val manifest: AppUpdateManifest) : AppUpdateDecision
    data object InvalidManifest : AppUpdateDecision
    data class Failure(val throwable: Throwable) : AppUpdateDecision
    data object Skipped : AppUpdateDecision
}

sealed interface AppUpdateUiReaction {
    data class ShowUpdateDialog(val manifest: AppUpdateManifest) : AppUpdateUiReaction
    data object ShowLatestToast : AppUpdateUiReaction
    data object ShowFailureToast : AppUpdateUiReaction
    data object Noop : AppUpdateUiReaction
}
