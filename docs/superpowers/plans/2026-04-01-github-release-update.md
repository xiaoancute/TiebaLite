# GitHub Release Update Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add automatic and manual update checks for `xiaoancute/TiebaLite`, limited to the currently installed `recovery` channel, using a GitHub Release `update.json` manifest and system-managed APK downloads.

**Architecture:** Add a small update subsystem with three layers: pure policy/helpers for channel filtering and UI reactions, a Retrofit GitHub client plus repository for fetching release manifests, and one activity-level Compose entry point that owns auto/manual checking and the update dialog. Extend the existing tag-release workflow to generate and upload `update.json` beside the APK.

**Tech Stack:** Kotlin, Compose, DataStore preferences, Retrofit, kotlinx.serialization, OkHttp, GitHub Actions shell + `jq`.

---

## File Map

- Create: `app/src/main/java/com/huanchengfly/tieba/post/update/AppUpdateConfig.kt`
  Holds repo constants, manifest asset name, new preference keys, update interval, and current build channel/version helpers.

- Create: `app/src/main/java/com/huanchengfly/tieba/post/update/AppUpdateModels.kt`
  Defines release DTOs, `update.json` manifest model, local state model, result model, and check source enum.

- Create: `app/src/main/java/com/huanchengfly/tieba/post/update/AppUpdatePolicy.kt`
  Keeps pure decision helpers: interval gating, release asset selection, manifest validation, version comparison, and UI reaction selection.

- Create: `app/src/main/java/com/huanchengfly/tieba/post/api/GitHubApi.kt`
  Provides a lightweight Retrofit client for `api.github.com`.

- Create: `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/interfaces/GitHubReleaseApi.kt`
  Declares GitHub Releases endpoints and raw manifest download endpoint.

- Create: `app/src/main/java/com/huanchengfly/tieba/post/repository/AppUpdateRepository.kt`
  Fetches release list + `update.json`, parses the manifest, and returns a typed update decision.

- Create: `app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/more/AppUpdateSettingSpecs.kt`
  Gives the update auto-check switch a stable key/default/title/summary surface for unit testing.

- Create: `app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/about/AboutPageLinks.kt`
  Centralizes the maintained GitHub source URL and the manual update event factory so About page wiring is not hardcoded inline.

- Create: `app/src/main/java/com/huanchengfly/tieba/post/ui/update/AppUpdateDialog.kt`
  Renders the update prompt with changelog, remind-later, ignore-version, and download buttons.

- Create: `app/src/main/java/com/huanchengfly/tieba/post/ui/update/AppUpdateEntryPoint.kt`
  Runs startup auto-checks, listens for manual check events, shows toast/dialog feedback, and updates local state.

- Create: `app/src/test/java/com/huanchengfly/tieba/post/ui/page/settings/more/AppUpdateSettingSpecsTest.kt`
  Locks the new preference key/default/title/summary contract.

- Create: `app/src/test/java/com/huanchengfly/tieba/post/update/AppUpdatePolicyTest.kt`
  Covers interval logic, channel matching, ignored versions, missing download URLs, and manual-vs-auto UI reactions.

- Create: `app/src/test/java/com/huanchengfly/tieba/post/repository/AppUpdateRepositoryTest.kt`
  Verifies manifest lookup and parsing with a fake GitHub API.

- Create: `app/src/test/java/com/huanchengfly/tieba/post/ui/page/settings/about/AboutPageLinksTest.kt`
  Guards the maintained GitHub repo link and manual update event wiring.

- Create: `app/src/androidTest/java/com/huanchengfly/tieba/post/ui/update/AppUpdateDialogTest.kt`
  Verifies the dialog renders version/changelog/buttons and hides download when no APK URL exists.

- Create: `app/src/test/resources/fixtures/github-update-manifest-recovery.json`
  Sample `update.json` fixture for policy/repository tests.

- Create: `.github/scripts/generate-update-json.sh`
  Generates `update.json` from tag metadata, APK output, changelog file, and checksum.

- Create: `.github/scripts/test-generate-update-json.sh`
  Smoke-tests the generator script with a fake APK and release notes.

- Modify: `app/build.gradle.kts`
  Exposes stable build metadata for update checks via `BuildConfig.APP_VERSION_CODE` and `BuildConfig.UPDATE_CHANNEL`.

- Modify: `app/src/main/java/com/huanchengfly/tieba/post/utils/AppPreferencesUtils.kt`
  Adds typed accessors for auto-check enabled, ignored version code, and last check timestamp.

- Modify: `app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/more/MoreSettingsPage.kt`
  Adds the auto-check update switch to settings.

- Modify: `app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/about/AboutPage.kt`
  Repoints source code link to the maintained repo and adds the manual “检查更新” action.

- Modify: `app/src/main/java/com/huanchengfly/tieba/post/arch/GlobalEvent.kt`
  Adds a `CheckAppUpdate(manual: Boolean)` event for About page to request a global update check.

- Modify: `app/src/main/java/com/huanchengfly/tieba/post/MainActivityV2.kt`
  Mounts the update entry point once near the existing global dialogs.

- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`
- Modify: `app/src/main/res/values-b+zh+Hant/strings.xml`
  Adds update settings strings, button labels, toast text, and update dialog labels.

- Modify: `.github/workflows/build.yml`
  Calls the generator script, uploads `update.json`, and keeps tag rereleases clobber-safe.

## Task 1: Expose Stable Update Config and Settings Surface

**Files:**
- Create: `app/src/main/java/com/huanchengfly/tieba/post/update/AppUpdateConfig.kt`
- Create: `app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/more/AppUpdateSettingSpecs.kt`
- Create: `app/src/test/java/com/huanchengfly/tieba/post/ui/page/settings/more/AppUpdateSettingSpecsTest.kt`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/java/com/huanchengfly/tieba/post/utils/AppPreferencesUtils.kt`
- Modify: `app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/more/MoreSettingsPage.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`
- Modify: `app/src/main/res/values-b+zh+Hant/strings.xml`

- [ ] **Step 1: Write the failing settings contract test**

Create `app/src/test/java/com/huanchengfly/tieba/post/ui/page/settings/more/AppUpdateSettingSpecsTest.kt`:

```kotlin
package com.huanchengfly.tieba.post.ui.page.settings.more

import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.update.AppUpdateConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateSettingSpecsTest {
    @Test
    fun autoCheckUpdateSettingUsesStableKeyDefaultAndLabels() {
        val spec = buildAutoCheckUpdateSettingSpec()

        assertEquals(AppUpdateConfig.AUTO_CHECK_PREF_KEY, spec.key)
        assertTrue(spec.defaultChecked)
        assertEquals(R.string.title_auto_check_app_update, spec.titleResId)
        assertEquals(R.string.tip_auto_check_app_update, spec.summaryResId)
    }
}
```

- [ ] **Step 2: Run the new test to confirm the surface does not exist yet**

Run:

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :app:testDebugUnitTest --tests "com.huanchengfly.tieba.post.ui.page.settings.more.AppUpdateSettingSpecsTest"
```

Expected: FAIL with unresolved references for `AppUpdateConfig`, `buildAutoCheckUpdateSettingSpec`, or the new string IDs.

- [ ] **Step 3: Add build metadata, preference keys, settings spec, and the settings toggle**

Add these `buildConfigField` entries inside `android.defaultConfig` in `app/build.gradle.kts`:

```kotlin
        buildConfigField("int", "APP_VERSION_CODE", applicationVersionCode.toString())
        buildConfigField(
            "String",
            "UPDATE_CHANNEL",
            "\"${if (isPerVersion) property.preReleaseName else "stable"}\""
        )
```

Create `app/src/main/java/com/huanchengfly/tieba/post/update/AppUpdateConfig.kt`:

```kotlin
package com.huanchengfly.tieba.post.update

import com.huanchengfly.tieba.post.BuildConfig

object AppUpdateConfig {
    const val REPO_OWNER = "xiaoancute"
    const val REPO_NAME = "TiebaLite"
    const val UPDATE_JSON_ASSET_NAME = "update.json"

    const val AUTO_CHECK_PREF_KEY = "auto_check_app_update"
    const val IGNORED_VERSION_CODE_PREF_KEY = "ignored_update_version_code"
    const val LAST_CHECK_AT_PREF_KEY = "last_update_check_at"

    const val AUTO_CHECK_INTERVAL_MS = 24L * 60L * 60L * 1000L

    val currentChannel: String
        get() = BuildConfig.UPDATE_CHANNEL

    val currentVersionCode: Int
        get() = BuildConfig.APP_VERSION_CODE

    val repoPath: String
        get() = "$REPO_OWNER/$REPO_NAME"
}
```

Create `app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/more/AppUpdateSettingSpecs.kt`:

```kotlin
package com.huanchengfly.tieba.post.ui.page.settings.more

import androidx.annotation.StringRes
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.update.AppUpdateConfig

data class AppUpdateSettingSpec(
    val key: String,
    val defaultChecked: Boolean,
    @StringRes val titleResId: Int,
    @StringRes val summaryResId: Int,
)

fun buildAutoCheckUpdateSettingSpec(): AppUpdateSettingSpec = AppUpdateSettingSpec(
    key = AppUpdateConfig.AUTO_CHECK_PREF_KEY,
    defaultChecked = true,
    titleResId = R.string.title_auto_check_app_update,
    summaryResId = R.string.tip_auto_check_app_update,
)
```

Add typed preferences in `app/src/main/java/com/huanchengfly/tieba/post/utils/AppPreferencesUtils.kt`:

```kotlin
    var autoCheckAppUpdate by DataStoreDelegates.boolean(
        defaultValue = true,
        key = AppUpdateConfig.AUTO_CHECK_PREF_KEY
    )

    var ignoredUpdateVersionCode by DataStoreDelegates.int(
        defaultValue = 0,
        key = AppUpdateConfig.IGNORED_VERSION_CODE_PREF_KEY
    )

    var lastAppUpdateCheckAt by DataStoreDelegates.long(
        defaultValue = 0L,
        key = AppUpdateConfig.LAST_CHECK_AT_PREF_KEY
    )
```

Add the switch in `app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/more/MoreSettingsPage.kt` before the “关于” item:

```kotlin
            val updateSettingSpec = buildAutoCheckUpdateSettingSpec()

            prefsItem {
                SwitchPref(
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.BugReport,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    key = updateSettingSpec.key,
                    title = stringResource(id = updateSettingSpec.titleResId),
                    defaultChecked = updateSettingSpec.defaultChecked,
                    summary = { stringResource(id = updateSettingSpec.summaryResId) },
                )
            }
```

Add strings:

`app/src/main/res/values/strings.xml`

```xml
    <string name="title_auto_check_app_update">自动检查应用更新</string>
    <string name="tip_auto_check_app_update">启动应用时静默检查当前通道是否有新版本</string>
```

`app/src/main/res/values-en/strings.xml`

```xml
    <string name="title_auto_check_app_update">Auto-check app updates</string>
    <string name="tip_auto_check_app_update">Silently check this release channel for updates when the app starts</string>
```

`app/src/main/res/values-b+zh+Hant/strings.xml`

```xml
    <string name="title_auto_check_app_update">自動檢查應用更新</string>
    <string name="tip_auto_check_app_update">啟動應用時靜默檢查目前通道是否有新版本</string>
```

- [ ] **Step 4: Run the focused unit test and the settings compile target**

Run:

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :app:testDebugUnitTest --tests "com.huanchengfly.tieba.post.ui.page.settings.more.AppUpdateSettingSpecsTest"
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :app:compileDebugKotlin
```

Expected: PASS for the unit test, then a successful Kotlin compile with no unresolved `AppUpdateConfig` or string references.

- [ ] **Step 5: Commit the settings/config surface**

Run:

```bash
git add app/build.gradle.kts \
  app/src/main/java/com/huanchengfly/tieba/post/update/AppUpdateConfig.kt \
  app/src/main/java/com/huanchengfly/tieba/post/utils/AppPreferencesUtils.kt \
  app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/more/AppUpdateSettingSpecs.kt \
  app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/more/MoreSettingsPage.kt \
  app/src/test/java/com/huanchengfly/tieba/post/ui/page/settings/more/AppUpdateSettingSpecsTest.kt \
  app/src/main/res/values/strings.xml \
  app/src/main/res/values-en/strings.xml \
  app/src/main/res/values-b+zh+Hant/strings.xml
git commit -m "build: add app update config surface"
```

## Task 2: Add Pure Update Models, Policy, and UI Reaction Rules

**Files:**
- Create: `app/src/main/java/com/huanchengfly/tieba/post/update/AppUpdateModels.kt`
- Create: `app/src/main/java/com/huanchengfly/tieba/post/update/AppUpdatePolicy.kt`
- Create: `app/src/test/java/com/huanchengfly/tieba/post/update/AppUpdatePolicyTest.kt`
- Create: `app/src/test/resources/fixtures/github-update-manifest-recovery.json`

- [ ] **Step 1: Write failing policy tests for interval gating, version selection, and manual-vs-auto reactions**

Create `app/src/test/java/com/huanchengfly/tieba/post/update/AppUpdatePolicyTest.kt`:

```kotlin
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
```

- [ ] **Step 2: Run the policy test and confirm the helper layer is missing**

Run:

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :app:testDebugUnitTest --tests "com.huanchengfly.tieba.post.update.AppUpdatePolicyTest"
```

Expected: FAIL with unresolved references for `AppUpdateManifest`, `AppUpdateLocalState`, `AppUpdateDecision`, `shouldRunAutoUpdateCheck`, `resolveUpdateDecision`, or `toUpdateUiReaction`.

- [ ] **Step 3: Add the update domain models, pure policy, and fixture**

Create `app/src/main/java/com/huanchengfly/tieba/post/update/AppUpdateModels.kt`:

```kotlin
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
```

Create `app/src/main/java/com/huanchengfly/tieba/post/update/AppUpdatePolicy.kt`:

```kotlin
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
```

Create `app/src/test/resources/fixtures/github-update-manifest-recovery.json`:

```json
{
  "repo": "xiaoancute/TiebaLite",
  "channel": "recovery",
  "versionCode": 390109,
  "versionName": "4.0.0-recovery.12",
  "tagName": "v4.0.0-recovery.12",
  "publishedAt": "2026-04-01T12:00:00Z",
  "prerelease": true,
  "changelog": "## Changes\n- Fix startup checks",
  "apkName": "release-4.0.0-recovery.12.apk",
  "apkUrl": "https://example.com/release-4.0.0-recovery.12.apk",
  "sha256": "abc123"
}
```

- [ ] **Step 4: Re-run the pure-policy unit test**

Run:

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :app:testDebugUnitTest --tests "com.huanchengfly.tieba.post.update.AppUpdatePolicyTest"
```

Expected: PASS, proving the pure gating and reaction logic is stable before any network or UI work is added.

- [ ] **Step 5: Commit the pure update rules**

Run:

```bash
git add app/src/main/java/com/huanchengfly/tieba/post/update/AppUpdateModels.kt \
  app/src/main/java/com/huanchengfly/tieba/post/update/AppUpdatePolicy.kt \
  app/src/test/java/com/huanchengfly/tieba/post/update/AppUpdatePolicyTest.kt \
  app/src/test/resources/fixtures/github-update-manifest-recovery.json
git commit -m "test: add app update policy helpers"
```

## Task 3: Add GitHub Release Client and Repository

**Files:**
- Create: `app/src/main/java/com/huanchengfly/tieba/post/api/GitHubApi.kt`
- Create: `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/interfaces/GitHubReleaseApi.kt`
- Create: `app/src/main/java/com/huanchengfly/tieba/post/repository/AppUpdateRepository.kt`
- Create: `app/src/test/java/com/huanchengfly/tieba/post/repository/AppUpdateRepositoryTest.kt`

- [ ] **Step 1: Write the failing repository test against a fake GitHub API**

Create `app/src/test/java/com/huanchengfly/tieba/post/repository/AppUpdateRepositoryTest.kt`:

```kotlin
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
```

- [ ] **Step 2: Run the repository test and confirm the API/repository layer is missing**

Run:

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :app:testDebugUnitTest --tests "com.huanchengfly.tieba.post.repository.AppUpdateRepositoryTest"
```

Expected: FAIL with unresolved references for `GitHubReleaseApi`, `AppUpdateRepository`, or the repository constructor signature.

- [ ] **Step 3: Add the Retrofit GitHub client and repository**

Create `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/interfaces/GitHubReleaseApi.kt`:

```kotlin
package com.huanchengfly.tieba.post.api.retrofit.interfaces

import com.huanchengfly.tieba.post.update.GitHubReleaseSummary
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url

interface GitHubReleaseApi {
    @Headers(
        "Accept: application/vnd.github+json",
        "X-GitHub-Api-Version: 2022-11-28"
    )
    @GET("repos/{owner}/{repo}/releases")
    suspend fun listReleases(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
    ): List<GitHubReleaseSummary>

    @Streaming
    @GET
    suspend fun fetchRaw(@Url url: String): ResponseBody
}
```

Create `app/src/main/java/com/huanchengfly/tieba/post/api/GitHubApi.kt`:

```kotlin
package com.huanchengfly.tieba.post.api

import com.huanchengfly.tieba.post.api.retrofit.NullOnEmptyConverterFactory
import com.huanchengfly.tieba.post.api.retrofit.converter.kotlinx.serialization.asConverterFactory
import com.huanchengfly.tieba.post.api.retrofit.interceptors.CommonHeaderInterceptor
import com.huanchengfly.tieba.post.api.retrofit.interfaces.GitHubReleaseApi
import com.huanchengfly.tieba.post.update.AppUpdateJson
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object GitHubApi {
    private val connectionPool = ConnectionPool()

    val releaseService: GitHubReleaseApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(NullOnEmptyConverterFactory())
            .addConverterFactory(AppUpdateJson.asConverterFactory())
            .client(
                OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(
                        CommonHeaderInterceptor(
                            Header.USER_AGENT to { System.getProperty("http.agent") },
                        )
                    )
                    .connectionPool(connectionPool)
                    .build()
            )
            .build()
            .create(GitHubReleaseApi::class.java)
    }
}
```

Create `app/src/main/java/com/huanchengfly/tieba/post/repository/AppUpdateRepository.kt`:

```kotlin
package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.GitHubApi
import com.huanchengfly.tieba.post.api.retrofit.interfaces.GitHubReleaseApi
import com.huanchengfly.tieba.post.update.AppUpdateCheckSource
import com.huanchengfly.tieba.post.update.AppUpdateDecision
import com.huanchengfly.tieba.post.update.AppUpdateJson
import com.huanchengfly.tieba.post.update.AppUpdateLocalState
import com.huanchengfly.tieba.post.update.AppUpdateManifest
import com.huanchengfly.tieba.post.update.AppUpdateConfig
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
```

- [ ] **Step 4: Run the repository test and the focused policy regression suite**

Run:

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :app:testDebugUnitTest \
  --tests "com.huanchengfly.tieba.post.repository.AppUpdateRepositoryTest" \
  --tests "com.huanchengfly.tieba.post.update.AppUpdatePolicyTest"
```

Expected: PASS for both classes, proving the repository respects the previously locked policy rules.

- [ ] **Step 5: Commit the fetch/parsing layer**

Run:

```bash
git add app/src/main/java/com/huanchengfly/tieba/post/api/GitHubApi.kt \
  app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/interfaces/GitHubReleaseApi.kt \
  app/src/main/java/com/huanchengfly/tieba/post/repository/AppUpdateRepository.kt \
  app/src/test/java/com/huanchengfly/tieba/post/repository/AppUpdateRepositoryTest.kt
git commit -m "feat: add github release update repository"
```

## Task 4: Wire the Global Update UI and About Page Trigger

**Files:**
- Create: `app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/about/AboutPageLinks.kt`
- Create: `app/src/main/java/com/huanchengfly/tieba/post/ui/update/AppUpdateDialog.kt`
- Create: `app/src/main/java/com/huanchengfly/tieba/post/ui/update/AppUpdateEntryPoint.kt`
- Create: `app/src/test/java/com/huanchengfly/tieba/post/ui/page/settings/about/AboutPageLinksTest.kt`
- Create: `app/src/androidTest/java/com/huanchengfly/tieba/post/ui/update/AppUpdateDialogTest.kt`
- Modify: `app/src/main/java/com/huanchengfly/tieba/post/arch/GlobalEvent.kt`
- Modify: `app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/about/AboutPage.kt`
- Modify: `app/src/main/java/com/huanchengfly/tieba/post/MainActivityV2.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`
- Modify: `app/src/main/res/values-b+zh+Hant/strings.xml`

- [ ] **Step 1: Write the failing unit and Compose UI tests**

Create `app/src/test/java/com/huanchengfly/tieba/post/ui/page/settings/about/AboutPageLinksTest.kt`:

```kotlin
package com.huanchengfly.tieba.post.ui.page.settings.about

import com.huanchengfly.tieba.post.arch.GlobalEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class AboutPageLinksTest {
    @Test
    fun sourceCodeUrlPointsToMaintainedRepository() {
        assertEquals("https://github.com/xiaoancute/TiebaLite", ABOUT_SOURCE_CODE_URL)
    }

    @Test
    fun manualUpdateEventRequestsInteractiveCheck() {
        assertEquals(GlobalEvent.CheckAppUpdate(manual = true), buildManualCheckAppUpdateEvent())
    }
}
```

Create `app/src/androidTest/java/com/huanchengfly/tieba/post/ui/update/AppUpdateDialogTest.kt`:

```kotlin
package com.huanchengfly.tieba.post.ui.update

import androidx.activity.ComponentActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.TiebaLiteTheme
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.post.update.AppUpdateManifest
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
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.button_download_update)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.button_ignore_this_version)).assertIsDisplayed()
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

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.button_download_update)).assertDoesNotExist()
    }
}
```

- [ ] **Step 2: Run the tests and confirm the global UI surface does not exist yet**

Run:

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :app:testDebugUnitTest --tests "com.huanchengfly.tieba.post.ui.page.settings.about.AboutPageLinksTest"
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.huanchengfly.tieba.post.ui.update.AppUpdateDialogTest
```

Expected: FAIL with missing `ABOUT_SOURCE_CODE_URL`, `buildManualCheckAppUpdateEvent`, `GlobalEvent.CheckAppUpdate`, or `AppUpdateDialog`.

- [ ] **Step 3: Add the dialog, activity entry point, global event, About page trigger, and strings**

Create `app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/about/AboutPageLinks.kt`:

```kotlin
package com.huanchengfly.tieba.post.ui.page.settings.about

import com.huanchengfly.tieba.post.arch.GlobalEvent

const val ABOUT_SOURCE_CODE_URL = "https://github.com/xiaoancute/TiebaLite"

fun buildManualCheckAppUpdateEvent(): GlobalEvent.CheckAppUpdate =
    GlobalEvent.CheckAppUpdate(manual = true)
```

Extend `app/src/main/java/com/huanchengfly/tieba/post/arch/GlobalEvent.kt`:

```kotlin
    data class CheckAppUpdate(
        val manual: Boolean,
    ) : GlobalEvent
```

Create `app/src/main/java/com/huanchengfly/tieba/post/ui/update/AppUpdateDialog.kt`:

```kotlin
package com.huanchengfly.tieba.post.ui.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.widgets.compose.AlertDialog
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogNegativeButton
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogPositiveButton
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogState
import com.huanchengfly.tieba.post.update.AppUpdateManifest

@Composable
fun AppUpdateDialog(
    dialogState: DialogState,
    manifest: AppUpdateManifest,
    onDownload: () -> Unit,
    onIgnore: () -> Unit,
) {
    AlertDialog(
        dialogState = dialogState,
        title = {
            Text(text = stringResource(id = R.string.title_dialog_update, manifest.versionName ?: ""))
        },
        content = {
            Column(
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 8.dp)
            ) {
                manifest.publishedAt?.let {
                    Text(text = stringResource(id = R.string.label_update_published_at, it))
                }
                manifest.channel?.let {
                    Text(text = stringResource(id = R.string.label_update_channel, it))
                }
                Text(text = manifest.changelog.orEmpty())
            }
        },
        buttons = {
            if (!manifest.apkUrl.isNullOrBlank()) {
                DialogPositiveButton(
                    text = stringResource(id = R.string.button_download_update),
                    onClick = onDownload
                )
            }
            DialogNegativeButton(
                text = stringResource(id = R.string.button_ignore_this_version),
                onClick = onIgnore
            )
            DialogNegativeButton(
                text = stringResource(id = R.string.button_remind_later)
            )
        }
    )
}
```

Create `app/src/main/java/com/huanchengfly/tieba/post/ui/update/AppUpdateEntryPoint.kt`:

```kotlin
package com.huanchengfly.tieba.post.ui.update

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.onGlobalEvent
import com.huanchengfly.tieba.post.repository.AppUpdateRepository
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.post.update.AppUpdateCheckSource
import com.huanchengfly.tieba.post.update.AppUpdateConfig
import com.huanchengfly.tieba.post.update.AppUpdateDecision
import com.huanchengfly.tieba.post.update.AppUpdateLocalState
import com.huanchengfly.tieba.post.update.AppUpdateUiReaction
import com.huanchengfly.tieba.post.update.toUpdateUiReaction
import com.huanchengfly.tieba.post.utils.FileUtil
import com.huanchengfly.tieba.post.utils.appPreferences

@Composable
fun AppUpdateEntryPoint(
    repository: AppUpdateRepository = remember { AppUpdateRepository() },
) {
    val context = LocalContext.current
    val dialogState = rememberDialogState()
    var showingManifest by remember { mutableStateOf<com.huanchengfly.tieba.post.update.AppUpdateManifest?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    suspend fun runCheck(source: AppUpdateCheckSource) {
        if (isChecking) return
        isChecking = true
        val now = System.currentTimeMillis()
        val localState = AppUpdateLocalState(
            currentVersionCode = AppUpdateConfig.currentVersionCode,
            channel = AppUpdateConfig.currentChannel,
            ignoredVersionCode = context.appPreferences.ignoredUpdateVersionCode,
            autoCheckEnabled = context.appPreferences.autoCheckAppUpdate,
            lastCheckedAt = context.appPreferences.lastAppUpdateCheckAt,
        )
        val decision = repository.check(localState, source, now)
        if (decision !is AppUpdateDecision.Skipped) {
            context.appPreferences.lastAppUpdateCheckAt = now
        }
        when (val reaction = toUpdateUiReaction(source, decision)) {
            is AppUpdateUiReaction.ShowUpdateDialog -> {
                showingManifest = reaction.manifest
                dialogState.show()
            }
            AppUpdateUiReaction.ShowLatestToast -> context.toastShort(R.string.message_update_already_latest)
            AppUpdateUiReaction.ShowFailureToast -> context.toastShort(R.string.toast_update_check_failed)
            AppUpdateUiReaction.Noop -> Unit
        }
        isChecking = false
    }

    showingManifest?.let { manifest ->
        AppUpdateDialog(
            dialogState = dialogState,
            manifest = manifest,
            onDownload = {
                FileUtil.downloadBySystem(
                    context = context,
                    fileType = FileUtil.FILE_TYPE_DOWNLOAD,
                    url = manifest.apkUrl,
                    fileName = manifest.apkName ?: "TiebaLite-update.apk",
                )
            },
            onIgnore = {
                context.appPreferences.ignoredUpdateVersionCode = manifest.versionCode ?: 0
            },
        )
    }

    LaunchedEffect(Unit) {
        runCheck(AppUpdateCheckSource.AUTO)
    }

    onGlobalEvent<GlobalEvent.CheckAppUpdate> {
        runCheck(if (it.manual) AppUpdateCheckSource.MANUAL else AppUpdateCheckSource.AUTO)
    }
}
```

Update the source URL and add the manual check button in `app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/about/AboutPage.kt`:

```kotlin
    val coroutineScope = rememberCoroutineScope()

                TextButton(
                    shape = RoundedCornerShape(100),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = ExtendedTheme.colors.text.copy(alpha = 0.1f),
                        contentColor = ExtendedTheme.colors.text
                    ),
                    onClick = {
                        coroutineScope.emitGlobalEvent(buildManualCheckAppUpdateEvent())
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.button_check_update), modifier = Modifier.padding(vertical = 4.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    shape = RoundedCornerShape(100),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = ExtendedTheme.colors.text.copy(alpha = 0.1f),
                        contentColor = ExtendedTheme.colors.text
                    ),
                    onClick = {
                        launchUrl(context, navigator, ABOUT_SOURCE_CODE_URL)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.source_code), modifier = Modifier.padding(vertical = 4.dp))
                }
```

Mount the entry point in `app/src/main/java/com/huanchengfly/tieba/post/MainActivityV2.kt` near the other global dialogs:

```kotlin
        val okSignAlertDialogState = rememberDialogState()
        ClipBoardDetectDialog()
        AppUpdateEntryPoint()
        AlertDialog(
```

Add strings:

`app/src/main/res/values/strings.xml`

```xml
    <string name="button_check_update">检查更新</string>
    <string name="button_download_update">下载更新</string>
    <string name="button_ignore_this_version">忽略此版本</string>
    <string name="button_remind_later">以后提醒</string>
    <string name="label_update_channel">更新通道：%1$s</string>
    <string name="label_update_published_at">发布时间：%1$s</string>
    <string name="message_update_already_latest">当前已是最新版本</string>
    <string name="toast_update_check_failed">检查更新失败</string>
```

`app/src/main/res/values-en/strings.xml`

```xml
    <string name="button_check_update">Check for updates</string>
    <string name="button_download_update">Download update</string>
    <string name="button_ignore_this_version">Ignore this version</string>
    <string name="button_remind_later">Remind me later</string>
    <string name="label_update_channel">Channel: %1$s</string>
    <string name="label_update_published_at">Published: %1$s</string>
    <string name="message_update_already_latest">You already have the latest version</string>
    <string name="toast_update_check_failed">Failed to check for updates</string>
```

`app/src/main/res/values-b+zh+Hant/strings.xml`

```xml
    <string name="button_check_update">檢查更新</string>
    <string name="button_download_update">下載更新</string>
    <string name="button_ignore_this_version">忽略此版本</string>
    <string name="button_remind_later">以後提醒</string>
    <string name="label_update_channel">更新通道：%1$s</string>
    <string name="label_update_published_at">發佈時間：%1$s</string>
    <string name="message_update_already_latest">目前已是最新版本</string>
    <string name="toast_update_check_failed">檢查更新失敗</string>
```

- [ ] **Step 4: Run the focused unit/UI tests plus a debug compile**

Run:

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :app:testDebugUnitTest \
  --tests "com.huanchengfly.tieba.post.ui.page.settings.about.AboutPageLinksTest" \
  --tests "com.huanchengfly.tieba.post.update.AppUpdatePolicyTest"
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.huanchengfly.tieba.post.ui.update.AppUpdateDialogTest
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :app:assembleDebug
```

Expected: both unit classes PASS, the dialog androidTest PASSes on the emulator, and `assembleDebug` succeeds with the new activity-level entry point mounted.

- [ ] **Step 5: Commit the UI wiring**

Run:

```bash
git add app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/about/AboutPageLinks.kt \
  app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/about/AboutPage.kt \
  app/src/main/java/com/huanchengfly/tieba/post/ui/update/AppUpdateDialog.kt \
  app/src/main/java/com/huanchengfly/tieba/post/ui/update/AppUpdateEntryPoint.kt \
  app/src/main/java/com/huanchengfly/tieba/post/arch/GlobalEvent.kt \
  app/src/main/java/com/huanchengfly/tieba/post/MainActivityV2.kt \
  app/src/test/java/com/huanchengfly/tieba/post/ui/page/settings/about/AboutPageLinksTest.kt \
  app/src/androidTest/java/com/huanchengfly/tieba/post/ui/update/AppUpdateDialogTest.kt \
  app/src/main/res/values/strings.xml \
  app/src/main/res/values-en/strings.xml \
  app/src/main/res/values-b+zh+Hant/strings.xml
git commit -m "feat: wire app update ui"
```

## Task 5: Generate and Publish `update.json` in GitHub Actions

**Files:**
- Create: `.github/scripts/generate-update-json.sh`
- Create: `.github/scripts/test-generate-update-json.sh`
- Modify: `.github/workflows/build.yml`

- [ ] **Step 1: Write the failing manifest-generator smoke test**

Create `.github/scripts/test-generate-update-json.sh`:

```bash
#!/usr/bin/env bash
set -euo pipefail

tmp_dir="$(mktemp -d)"
apk_path="${tmp_dir}/release-4.0.0-recovery.12(390109).apk"
notes_path="${tmp_dir}/release-notes.md"
output_path="${tmp_dir}/update.json"

printf 'fake apk' > "${apk_path}"
cat > "${notes_path}" <<'EOF'
## Changes
- smoke
EOF

bash .github/scripts/generate-update-json.sh \
  --repo "xiaoancute/TiebaLite" \
  --channel "recovery" \
  --tag "v4.0.0-recovery.12" \
  --version-code "390109" \
  --version-name "4.0.0-recovery.12" \
  --apk "${apk_path}" \
  --notes "${notes_path}" \
  --output "${output_path}"

jq -e '.repo == "xiaoancute/TiebaLite"' "${output_path}" >/dev/null
jq -e '.channel == "recovery"' "${output_path}" >/dev/null
jq -e '.versionCode == 390109' "${output_path}" >/dev/null
jq -e '.apkName == "release-4.0.0-recovery.12(390109).apk"' "${output_path}" >/dev/null
jq -e '.apkUrl | contains("/releases/download/v4.0.0-recovery.12/")' "${output_path}" >/dev/null
jq -e '.sha256 | length > 0' "${output_path}" >/dev/null
```

- [ ] **Step 2: Run the smoke test and confirm the generator does not exist yet**

Run:

```bash
bash .github/scripts/test-generate-update-json.sh
```

Expected: FAIL because `.github/scripts/generate-update-json.sh` does not exist yet.

- [ ] **Step 3: Add the generator script and wire it into the tag-release workflow**

Create `.github/scripts/generate-update-json.sh`:

```bash
#!/usr/bin/env bash
set -euo pipefail

repo=""
channel=""
tag=""
version_code=""
version_name=""
apk=""
notes=""
output=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --repo) repo="$2"; shift 2 ;;
    --channel) channel="$2"; shift 2 ;;
    --tag) tag="$2"; shift 2 ;;
    --version-code) version_code="$2"; shift 2 ;;
    --version-name) version_name="$2"; shift 2 ;;
    --apk) apk="$2"; shift 2 ;;
    --notes) notes="$2"; shift 2 ;;
    --output) output="$2"; shift 2 ;;
    *) echo "unknown argument: $1" >&2; exit 1 ;;
  esac
done

apk_name="$(basename "${apk}")"
sha256="$(sha256sum "${apk}" | awk '{print $1}')"
published_at="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
apk_url="https://github.com/${repo}/releases/download/${tag}/${apk_name}"
changelog="$(cat "${notes}")"

jq -n \
  --arg repo "${repo}" \
  --arg channel "${channel}" \
  --arg tagName "${tag}" \
  --arg versionName "${version_name}" \
  --arg publishedAt "${published_at}" \
  --arg changelog "${changelog}" \
  --arg apkName "${apk_name}" \
  --arg apkUrl "${apk_url}" \
  --arg sha256 "${sha256}" \
  --argjson versionCode "${version_code}" \
  '{
    repo: $repo,
    channel: $channel,
    versionCode: $versionCode,
    versionName: $versionName,
    tagName: $tagName,
    publishedAt: $publishedAt,
    prerelease: true,
    changelog: $changelog,
    apkName: $apkName,
    apkUrl: $apkUrl,
    sha256: $sha256
  }' > "${output}"
```

Update `.github/workflows/build.yml` by inserting a manifest generation step after “Prepare prerelease notes”:

```yaml
      - name: Generate update manifest
        if: ${{ github.ref_type == 'tag' }}
        env:
          TAG_NAME: ${{ github.ref_name }}
        run: |
          shopt -s nullglob
          files=(./app/build/outputs/apk/release/*.apk)
          if [ "${#files[@]}" -eq 0 ]; then
            echo "::error title=Missing release asset::No release APK was produced."
            exit 1
          fi

          mkdir -p .github/release-notes/.out
          bash .github/scripts/generate-update-json.sh \
            --repo "${GITHUB_REPOSITORY}" \
            --channel "recovery" \
            --tag "${TAG_NAME}" \
            --version-code "${{ steps.read_output_metadata.outputs.version_code }}" \
            --version-name "${{ steps.read_output_metadata.outputs.version_name }}" \
            --apk "${files[0]}" \
            --notes .github/release-notes/.out/release-notes.md \
            --output .github/release-notes/.out/update.json
```

Update the publish step asset upload commands to include `update.json`:

```yaml
          assets=(./app/build/outputs/apk/release/*.apk .github/release-notes/.out/update.json)

          if gh release view "${TAG_NAME}" >/dev/null 2>&1; then
            gh release edit "${TAG_NAME}" \
              --title "${TAG_NAME}" \
              --notes-file .github/release-notes/.out/release-notes.md \
              --prerelease
            gh release upload "${TAG_NAME}" "${assets[@]}" --clobber
          else
            gh release create "${TAG_NAME}" "${assets[@]}" \
              --verify-tag \
              --title "${TAG_NAME}" \
              --notes-file .github/release-notes/.out/release-notes.md \
              --prerelease \
              --latest=false
          fi
```

Make both scripts executable:

```bash
chmod +x .github/scripts/generate-update-json.sh .github/scripts/test-generate-update-json.sh
```

- [ ] **Step 4: Run the generator smoke test and a workflow syntax check**

Run:

```bash
bash .github/scripts/test-generate-update-json.sh
bash -n .github/scripts/generate-update-json.sh
python3 - <<'PY'
import yaml
from pathlib import Path
yaml.safe_load(Path(".github/workflows/build.yml").read_text())
print("workflow yaml ok")
PY
```

Expected: the smoke test passes, the generator script passes `bash -n`, and the workflow YAML prints `workflow yaml ok`.

- [ ] **Step 5: Commit the release-manifest workflow**

Run:

```bash
git add .github/scripts/generate-update-json.sh \
  .github/scripts/test-generate-update-json.sh \
  .github/workflows/build.yml
git commit -m "build: publish update manifest"
```

## Verification

- [ ] Run the focused JVM regression set:

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :app:testDebugUnitTest \
  --tests "com.huanchengfly.tieba.post.ui.page.settings.more.AppUpdateSettingSpecsTest" \
  --tests "com.huanchengfly.tieba.post.update.AppUpdatePolicyTest" \
  --tests "com.huanchengfly.tieba.post.repository.AppUpdateRepositoryTest" \
  --tests "com.huanchengfly.tieba.post.ui.page.settings.about.AboutPageLinksTest"
```

- [ ] Run the targeted Compose dialog instrumentation test on the emulator:

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.huanchengfly.tieba.post.ui.update.AppUpdateDialogTest
```

- [ ] Run a debug assembly:

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :app:assembleDebug
```

- [ ] Re-run the manifest script smoke test:

```bash
bash .github/scripts/test-generate-update-json.sh
```

- [ ] Manual smoke on API 34 emulator:

```text
1. 打开“更多设置”，确认“自动检查应用更新”默认开启。
2. 打开“关于”，确认源码按钮跳到 https://github.com/xiaoancute/TiebaLite。
3. 点击“检查更新”：
   - 没有新版本时出现“当前已是最新版本”。
   - 远端有新版本时弹出更新对话框，并显示版本号、通道、更新时间、changelog。
4. 在对话框点击“忽略此版本”，再次冷启动应用时不再自动重复提醒同一版本。
5. 在对话框点击“下载更新”，系统下载器开始下载 APK。
```
