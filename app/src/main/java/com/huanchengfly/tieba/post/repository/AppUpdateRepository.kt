package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.GitHubApi
import com.huanchengfly.tieba.post.api.retrofit.interfaces.GitHubReleaseApi
import com.huanchengfly.tieba.post.update.AppUpdateCheckSource
import com.huanchengfly.tieba.post.update.AppUpdateConfig
import com.huanchengfly.tieba.post.update.AppUpdateDecision
import com.huanchengfly.tieba.post.update.AppUpdateJson
import com.huanchengfly.tieba.post.update.AppUpdateLocalState
import com.huanchengfly.tieba.post.update.AppUpdateManifest
import com.huanchengfly.tieba.post.update.resolveUpdateDecision
import com.huanchengfly.tieba.post.update.selectManifestAssetUrl
import com.huanchengfly.tieba.post.update.shouldRunAutoUpdateCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppUpdateRepository(
    private val api: GitHubReleaseApi = GitHubApi.releaseService,
) {
    suspend fun check(
        localState: AppUpdateLocalState,
        source: AppUpdateCheckSource,
        now: Long = System.currentTimeMillis(),
    ): AppUpdateDecision = withContext(Dispatchers.IO) {
        if (
            source == AppUpdateCheckSource.AUTO &&
            !shouldRunAutoUpdateCheck(
                enabled = localState.autoCheckEnabled,
                lastCheckedAt = localState.lastCheckedAt,
                now = now,
            )
        ) {
            return@withContext AppUpdateDecision.Skipped
        }

        runCatching {
            val releases = api.listReleases(AppUpdateConfig.REPO_OWNER, AppUpdateConfig.REPO_NAME)
            for (release in releases) {
                val manifestAssetUrl = selectManifestAssetUrl(release) ?: continue
                val manifestJson = api.fetchRaw(manifestAssetUrl).string()
                val manifest = AppUpdateJson.decodeFromString<AppUpdateManifest>(manifestJson)
                val decision = resolveUpdateDecision(localState, manifest)
                if (decision != AppUpdateDecision.InvalidManifest) {
                    return@runCatching decision
                }
            }
            AppUpdateDecision.UpToDate
        }.getOrElse(AppUpdateDecision::Failure)
    }
}
