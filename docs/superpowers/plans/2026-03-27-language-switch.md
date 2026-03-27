# App Language Switch Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an in-app language switch with `Follow system`, `简体中文`, `繁體中文`, and `English`, persist the selection, restore it on startup, and expose it in the existing custom settings page.

**Architecture:** Introduce a focused `AppLanguageManager` helper that owns preference keys, normalization, locale mapping, and `AppCompatDelegate` application. Keep the settings UI thin by adding a small settings-spec builder in the custom settings package, then wire the existing `ListPref` to the helper and restore the selected locale from `App.onCreate()`.

**Tech Stack:** Kotlin, AndroidX AppCompat `LocaleListCompat`, DataStore-backed preferences, Compose settings UI, JUnit4 JVM tests, Gradle.

---

### Task 1: Preflight The Worktree

**Files:**
- Modify: `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/local.properties` (copy from main checkout if missing)

- [ ] **Step 1: Ensure the worktree has Android SDK local config**

Run:

```bash
test -f /home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/local.properties || \
  cp /home/x/My_Dev/TiebaLite-4.0-dev/local.properties /home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/local.properties
```

Expected: the command exits `0` and `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/local.properties` exists.

- [ ] **Step 2: Confirm the worktree is on the feature branch and clean**

Run:

```bash
git -C /home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch status --short --branch
```

Expected:

```text
## feature/language-switch
```

### Task 2: Add Locale Mapping Helper With Failing Tests First

**Files:**
- Create: `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/main/java/com/huanchengfly/tieba/post/utils/AppLanguageManager.kt`
- Test: `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/test/java/com/huanchengfly/tieba/post/utils/AppLanguageManagerTest.kt`

- [ ] **Step 1: Write the failing helper tests**

Create `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/test/java/com/huanchengfly/tieba/post/utils/AppLanguageManagerTest.kt` with:

```kotlin
package com.huanchengfly.tieba.post.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppLanguageManagerTest {
    @Test
    fun buildAppLanguageSpecNormalizesUnknownValuesToSystem() {
        val spec = buildAppLanguageSpec("ja")

        assertEquals(AppLanguageManager.VALUE_SYSTEM, spec.preferenceValue)
        assertNull(spec.languageTags)
    }

    @Test
    fun buildAppLanguageSpecKeepsSupportedLanguageTags() {
        val zhHans = buildAppLanguageSpec(AppLanguageManager.VALUE_ZH_HANS)
        val zhHant = buildAppLanguageSpec(AppLanguageManager.VALUE_ZH_HANT)
        val en = buildAppLanguageSpec(AppLanguageManager.VALUE_EN)

        assertEquals("zh-Hans", zhHans.languageTags)
        assertEquals("zh-Hant", zhHant.languageTags)
        assertEquals("en", en.languageTags)
    }

    @Test
    fun buildAppLanguageLocalesUsesEmptyListForSystem() {
        val locales = buildAppLanguageLocales(AppLanguageManager.VALUE_SYSTEM)

        assertEquals("", locales.toLanguageTags())
    }
}
```

- [ ] **Step 2: Run the helper test to verify it fails**

Run:

```bash
cd /home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew testDebugUnitTest --tests com.huanchengfly.tieba.post.utils.AppLanguageManagerTest
```

Expected: FAIL with unresolved references for `AppLanguageManager`, `buildAppLanguageSpec`, or `buildAppLanguageLocales`.

- [ ] **Step 3: Write the minimal helper implementation**

Create `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/main/java/com/huanchengfly/tieba/post/utils/AppLanguageManager.kt` with:

```kotlin
package com.huanchengfly.tieba.post.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object AppLanguageManager {
    const val PREF_KEY = "app_language"
    const val VALUE_SYSTEM = "system"
    const val VALUE_ZH_HANS = "zh-Hans"
    const val VALUE_ZH_HANT = "zh-Hant"
    const val VALUE_EN = "en"
}

data class AppLanguageSpec(
    val preferenceValue: String,
    val languageTags: String?,
)

fun buildAppLanguageSpec(rawValue: String?): AppLanguageSpec {
    val normalized = rawValue?.takeUnless { it.isBlank() }
    return when (normalized) {
        AppLanguageManager.VALUE_ZH_HANS -> AppLanguageSpec(
            preferenceValue = AppLanguageManager.VALUE_ZH_HANS,
            languageTags = AppLanguageManager.VALUE_ZH_HANS,
        )
        AppLanguageManager.VALUE_ZH_HANT -> AppLanguageSpec(
            preferenceValue = AppLanguageManager.VALUE_ZH_HANT,
            languageTags = AppLanguageManager.VALUE_ZH_HANT,
        )
        AppLanguageManager.VALUE_EN -> AppLanguageSpec(
            preferenceValue = AppLanguageManager.VALUE_EN,
            languageTags = AppLanguageManager.VALUE_EN,
        )
        else -> AppLanguageSpec(
            preferenceValue = AppLanguageManager.VALUE_SYSTEM,
            languageTags = null,
        )
    }
}

fun buildAppLanguageLocales(rawValue: String?): LocaleListCompat {
    val spec = buildAppLanguageSpec(rawValue)
    return spec.languageTags?.let(LocaleListCompat::forLanguageTags)
        ?: LocaleListCompat.getEmptyLocaleList()
}

fun applyAppLanguage(rawValue: String?) {
    AppCompatDelegate.setApplicationLocales(buildAppLanguageLocales(rawValue))
}

fun applySavedAppLanguage(context: Context) {
    applyAppLanguage(context.appPreferences.appLanguage)
}
```

- [ ] **Step 4: Run the helper test to verify it passes**

Run:

```bash
cd /home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew testDebugUnitTest --tests com.huanchengfly.tieba.post.utils.AppLanguageManagerTest
```

Expected: PASS with `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit the helper**

```bash
cd /home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch
git add app/src/main/java/com/huanchengfly/tieba/post/utils/AppLanguageManager.kt \
        app/src/test/java/com/huanchengfly/tieba/post/utils/AppLanguageManagerTest.kt
git commit -m "feat: add app language manager"
```

### Task 3: Add Settings Metadata And Resource Strings

**Files:**
- Create: `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/custom/AppLanguageSettingSpecs.kt`
- Create: `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/test/java/com/huanchengfly/tieba/post/ui/page/settings/custom/AppLanguageSettingSpecsTest.kt`
- Modify: `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/main/res/values/strings.xml`
- Modify: `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/main/res/values-en/strings.xml`
- Modify: `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/main/res/values-b+zh+Hant/strings.xml`

- [ ] **Step 1: Write the failing settings-spec test**

Create `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/test/java/com/huanchengfly/tieba/post/ui/page/settings/custom/AppLanguageSettingSpecsTest.kt` with:

```kotlin
package com.huanchengfly.tieba.post.ui.page.settings.custom

import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.utils.AppLanguageManager
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageSettingSpecsTest {
    @Test
    fun appLanguageSettingUsesStableKeyDefaultAndEntryOrder() {
        val spec = buildAppLanguageSettingSpec()

        assertEquals(AppLanguageManager.PREF_KEY, spec.key)
        assertEquals(AppLanguageManager.VALUE_SYSTEM, spec.defaultValue)
        assertEquals(R.string.title_settings_app_language, spec.titleResId)
        assertEquals(
            listOf(
                AppLanguageManager.VALUE_SYSTEM,
                AppLanguageManager.VALUE_ZH_HANS,
                AppLanguageManager.VALUE_ZH_HANT,
                AppLanguageManager.VALUE_EN,
            ),
            spec.entryResIds.keys.toList()
        )
    }
}
```

- [ ] **Step 2: Run the settings-spec test to verify it fails**

Run:

```bash
cd /home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew testDebugUnitTest --tests com.huanchengfly.tieba.post.ui.page.settings.custom.AppLanguageSettingSpecsTest
```

Expected: FAIL with unresolved references for `buildAppLanguageSettingSpec` or `title_settings_app_language`.

- [ ] **Step 3: Add the settings spec builder and new strings**

Create `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/custom/AppLanguageSettingSpecs.kt` with:

```kotlin
package com.huanchengfly.tieba.post.ui.page.settings.custom

import androidx.annotation.StringRes
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.utils.AppLanguageManager

data class AppLanguageSettingSpec(
    val key: String,
    val defaultValue: String,
    @StringRes val titleResId: Int,
    val entryResIds: LinkedHashMap<String, Int>,
)

fun buildAppLanguageSettingSpec(): AppLanguageSettingSpec =
    AppLanguageSettingSpec(
        key = AppLanguageManager.PREF_KEY,
        defaultValue = AppLanguageManager.VALUE_SYSTEM,
        titleResId = R.string.title_settings_app_language,
        entryResIds = linkedMapOf(
            AppLanguageManager.VALUE_SYSTEM to R.string.settings_app_language_follow_system,
            AppLanguageManager.VALUE_ZH_HANS to R.string.settings_app_language_zh_hans,
            AppLanguageManager.VALUE_ZH_HANT to R.string.settings_app_language_zh_hant,
            AppLanguageManager.VALUE_EN to R.string.settings_app_language_en,
        ),
    )
```

Add these strings to `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/main/res/values/strings.xml`:

```xml
<string name="title_settings_app_language">应用语言</string>
<string name="settings_app_language_follow_system">跟随系统</string>
<string name="settings_app_language_zh_hans">简体中文</string>
<string name="settings_app_language_zh_hant">繁體中文</string>
<string name="settings_app_language_en">English</string>
```

Add these strings to `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/main/res/values-en/strings.xml`:

```xml
<string name="title_settings_app_language">App language</string>
<string name="settings_app_language_follow_system">Follow system</string>
<string name="settings_app_language_zh_hans">简体中文</string>
<string name="settings_app_language_zh_hant">繁體中文</string>
<string name="settings_app_language_en">English</string>
```

Add these strings to `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/main/res/values-b+zh+Hant/strings.xml`:

```xml
<string name="title_settings_app_language">應用語言</string>
<string name="settings_app_language_follow_system">跟隨系統</string>
<string name="settings_app_language_zh_hans">简体中文</string>
<string name="settings_app_language_zh_hant">繁體中文</string>
<string name="settings_app_language_en">English</string>
```

- [ ] **Step 4: Run the settings-spec test to verify it passes**

Run:

```bash
cd /home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew testDebugUnitTest --tests com.huanchengfly.tieba.post.ui.page.settings.custom.AppLanguageSettingSpecsTest
```

Expected: PASS with `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit the settings metadata**

```bash
cd /home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch
git add app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/custom/AppLanguageSettingSpecs.kt \
        app/src/test/java/com/huanchengfly/tieba/post/ui/page/settings/custom/AppLanguageSettingSpecsTest.kt \
        app/src/main/res/values/strings.xml \
        app/src/main/res/values-en/strings.xml \
        app/src/main/res/values-b+zh+Hant/strings.xml
git commit -m "feat: define app language setting metadata"
```

### Task 4: Wire Preference Storage, Startup Restore, And Settings UI

**Files:**
- Modify: `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/main/java/com/huanchengfly/tieba/post/utils/AppPreferencesUtils.kt`
- Modify: `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/main/java/com/huanchengfly/tieba/post/App.kt`
- Modify: `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/custom/CustomSettingsPage.kt`

- [ ] **Step 1: Add the persisted preference field**

In `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/main/java/com/huanchengfly/tieba/post/utils/AppPreferencesUtils.kt`, add this property near the existing appearance-related preferences:

```kotlin
    var appLanguage by DataStoreDelegates.string(
        defaultValue = AppLanguageManager.VALUE_SYSTEM,
        key = AppLanguageManager.PREF_KEY
    )
```

- [ ] **Step 2: Restore the saved language during app startup**

In `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/main/java/com/huanchengfly/tieba/post/App.kt`, add the helper import and call it during `onCreate()`:

```kotlin
import com.huanchengfly.tieba.post.utils.applySavedAppLanguage
```

```kotlin
        Config.init(this)
        applySavedAppLanguage(this)
        AppIconUtil.setIcon()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
```

- [ ] **Step 3: Add the new list preference to the custom settings page**

In `/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/custom/CustomSettingsPage.kt`, add the new imports:

```kotlin
import androidx.compose.material.icons.outlined.Language
import com.huanchengfly.tieba.post.utils.applyAppLanguage
```

Then add a new `prefsItem` immediately before the dark-mode list preference:

```kotlin
            prefsItem {
                val spec = buildAppLanguageSettingSpec()
                ListPref(
                    key = spec.key,
                    title = stringResource(id = spec.titleResId),
                    defaultValue = spec.defaultValue,
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.Language,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    entries = spec.entryResIds.mapValues { (_, value) ->
                        context.getString(value)
                    },
                    onValueChange = { applyAppLanguage(it) },
                    useSelectedAsSummary = true,
                )
            }
```

- [ ] **Step 4: Run the focused new tests plus existing settings regressions**

Run:

```bash
cd /home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew testDebugUnitTest \
  --tests com.huanchengfly.tieba.post.utils.AppLanguageManagerTest \
  --tests com.huanchengfly.tieba.post.ui.page.settings.custom.AppLanguageSettingSpecsTest \
  --tests com.huanchengfly.tieba.post.ui.page.settings.oksign.OKSignSettingsPageTest \
  --tests com.huanchengfly.tieba.post.ui.page.settings.more.MoreSettingsPageTest
```

Expected: PASS with `BUILD SUCCESSFUL`.

- [ ] **Step 5: Run the debug build**

Run:

```bash
cd /home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew assembleDebug
```

Expected: PASS with `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit the integrated feature**

```bash
cd /home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch
git add app/src/main/java/com/huanchengfly/tieba/post/utils/AppPreferencesUtils.kt \
        app/src/main/java/com/huanchengfly/tieba/post/App.kt \
        app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/custom/CustomSettingsPage.kt \
        app/src/main/java/com/huanchengfly/tieba/post/utils/AppLanguageManager.kt \
        app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/custom/AppLanguageSettingSpecs.kt \
        app/src/test/java/com/huanchengfly/tieba/post/utils/AppLanguageManagerTest.kt \
        app/src/test/java/com/huanchengfly/tieba/post/ui/page/settings/custom/AppLanguageSettingSpecsTest.kt \
        app/src/main/res/values/strings.xml \
        app/src/main/res/values-en/strings.xml \
        app/src/main/res/values-b+zh+Hant/strings.xml
git commit -m "feat: add app language switch"
```

### Task 5: Final Verification And Manual Smoke Check

**Files:**
- Verify only

- [ ] **Step 1: Re-run the focused unit suite once more from a clean command**

Run:

```bash
cd /home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew testDebugUnitTest \
  --tests com.huanchengfly.tieba.post.utils.AppLanguageManagerTest \
  --tests com.huanchengfly.tieba.post.ui.page.settings.custom.AppLanguageSettingSpecsTest
```

Expected: PASS with `BUILD SUCCESSFUL`.

- [ ] **Step 2: Re-run `assembleDebug` as the final build gate**

Run:

```bash
cd /home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew assembleDebug
```

Expected: PASS with `BUILD SUCCESSFUL`.

- [ ] **Step 3: Perform manual smoke validation on device/emulator**

Check:

```text
1. Open 设置 -> 自定义设置 and confirm the new “应用语言 / App language” row exists.
2. Switch to 简体中文 and confirm the current page updates without a restart.
3. Switch to 繁體中文 and confirm the current page updates without a restart.
4. Switch to English and confirm the current page updates without a restart.
5. Switch back to Follow system, relaunch the app, and confirm the app follows the device language again.
```

- [ ] **Step 4: Confirm the branch is ready for review**

Run:

```bash
cd /home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch
git status --short --branch
git log --oneline -n 5
```

Expected:

```text
## feature/language-switch
```

plus the new feature commits at the top of `git log`.
