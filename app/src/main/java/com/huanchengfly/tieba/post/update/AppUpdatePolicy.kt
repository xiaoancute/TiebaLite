package com.huanchengfly.tieba.post.update

import kotlinx.serialization.json.Json

val AppUpdateJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
    coerceInputValues = true
}

fun shouldRunAutoUpdateCheck(
    enabled: Boolean,
    lastCheckedAt: Long,
    now: Long,
    intervalMillis: Long = AppUpdateConfig.AUTO_CHECK_INTERVAL_MS,
): Boolean {
    if (!enabled) return false
    if (lastCheckedAt <= 0L) return true
    return now - lastCheckedAt >= intervalMillis
}

fun selectManifestAssetUrl(release: GitHubReleaseSummary): String? =
    release.assets.firstOrNull { it.name == AppUpdateConfig.UPDATE_JSON_ASSET_NAME }?.browserDownloadUrl

fun resolveUpdateDecision(
    localState: AppUpdateLocalState,
    manifest: AppUpdateManifest,
): AppUpdateDecision {
    val versionCode = manifest.versionCode ?: return AppUpdateDecision.InvalidManifest
    if (manifest.repo != AppUpdateConfig.repoPath) return AppUpdateDecision.InvalidManifest
    if (manifest.channel != localState.channel) return AppUpdateDecision.InvalidManifest
    if (manifest.apkUrl.isNullOrBlank()) return AppUpdateDecision.InvalidManifest
    if (versionCode <= localState.currentVersionCode) return AppUpdateDecision.UpToDate
    if (versionCode == localState.ignoredVersionCode) {
        return AppUpdateDecision.Ignored(manifest)
    }
    return AppUpdateDecision.Available(manifest)
}

fun toUpdateUiReaction(
    source: AppUpdateCheckSource,
    decision: AppUpdateDecision,
): AppUpdateUiReaction = when (decision) {
    is AppUpdateDecision.Available -> AppUpdateUiReaction.ShowUpdateDialog(decision.manifest)
    AppUpdateDecision.UpToDate -> if (source == AppUpdateCheckSource.MANUAL) {
        AppUpdateUiReaction.ShowLatestToast
    } else {
        AppUpdateUiReaction.Noop
    }
    is AppUpdateDecision.Ignored -> if (source == AppUpdateCheckSource.MANUAL) {
        AppUpdateUiReaction.ShowUpdateDialog(decision.manifest)
    } else {
        AppUpdateUiReaction.Noop
    }
    is AppUpdateDecision.Failure -> if (source == AppUpdateCheckSource.MANUAL) {
        AppUpdateUiReaction.ShowFailureToast
    } else {
        AppUpdateUiReaction.Noop
    }
    AppUpdateDecision.InvalidManifest,
    AppUpdateDecision.Skipped -> AppUpdateUiReaction.Noop
}
