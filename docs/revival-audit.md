# Revival Audit

Last updated: 2026-03-22

This file tracks which parts of the app have already been modernized and which parts still look stale, risky, or unsupported for a long-term revival.

## Already Modernized

- Background polling and auto sign no longer depend on `IntentService`, `JobService`, `JobScheduler`, or `AlarmManager`; they now route through WorkManager.
- Microsoft App Center analytics / crash / distribute integration has been removed.
- OAID collection and ZID overwrite behavior have been downgraded so empty identifiers are no longer forced into requests.
- Login flow now logs missing `BDUSS` / `STOKEN` cases after WebView cookie capture.
- Notification center has a first-pass "AgreeMe / 赞过我" path again, so the tab is no longer entirely dead.
- Device identity shims no longer read real `ANDROID_ID` or IMEI in `UIDUtil`; values are now derived from the app-local UUID instead.
- Legacy launcher shortcut permissions and the unused MIUI favorite permission have been removed from the manifest.
- Media picking no longer depends on Matisse; it now uses the system Photo Picker when available and falls back to the system document picker.
- `READ_EXTERNAL_STORAGE` has been removed from the manifest.
- `READ_MEDIA_IMAGES` has also been removed because the current media-picking flow no longer relies on broad media-library access.
- `WRITE_EXTERNAL_STORAGE` has now also been removed; older Android versions fall back to app-specific external directories for save/download flows.
- The main `c.tieba.baidu.com` Retrofit APIs now use `https://` instead of `http://`.
- Generated avatar URLs and emoticon downloads now prefer `https://` for known Tieba static hosts.
- Generated forum original-image URLs now also prefer `https://imgsrc.baidu.com` / `https://imgsa.baidu.com`.
- Unused `VIBRATE` and `ACCESS_WIFI_STATE` permissions have been removed from the manifest.
- Android 13+ notification permission is no longer requested on every app start; the app now prompts only once after login and cancels stale polling/notifications when logged out.
- Media upload now stages `content://` picker results into app-private cache instead of relying on deprecated `_data` / real-path access.
- External cleartext (`http://`) links are now pushed to the system browser / Custom Tabs instead of being rendered inside the app WebView.
- New installs now default to opening only Tieba links in the in-app WebView; external pages default to Custom Tabs / browser.
- Global event dispatch paths no longer rely on `GlobalScope`; they now use suspend calls or a dedicated app-level event scope.
- The translucent-theme blur path no longer depends on RenderScript helper classes; it now uses the in-project blur implementation directly.
- The translucent-theme settings page no longer depends on `@BindView` or `onActivityResult`; it now uses direct view lookups and the Activity Result API for image crop results.
- ButterKnife is no longer on the live execution path; the remaining base-class bind calls and Gradle dependencies have been removed.
- The orphaned custom crash-restart utility and its related backup exclusions have been removed after static verification showed no live entry points.
- A first-pass revival framework now exists for capability grading, so public browsing, core account features, and experimental features no longer share the same default exposure path.
- Account/session handling now has an explicit session-health model, so the app can distinguish complete login, incomplete cookies, and web-only login states.
- Hot-topic navigation no longer stops at the list layer; a first-pass topic detail route, ViewModel, and page now exist.
- Forum navigation is no longer hardcoded to only two fixed tabs internally; it now uses an extensible forum-surface tab model with `最新 / 精华 / 吧内搜索`.
- Auto sign is now explicitly treated as an experimental capability and is gated behind the existing experimental-features toggle.
- The main navigation has been re-centered on reading, so notifications are no longer a first-level tab, the home top bar no longer exposes one-tap sign, and forum headers stop surfacing sign as a primary action once a forum is already followed.
- Visible entry points now have a first-pass capability audit in docs, and the settings entry summaries expose which routes are stable, core-account guarded, or still experimental.
- The `Explore` second-level tab model now re-syncs with login state, so the concern-feed entry no longer lingers or shifts to the wrong tab after logout or account changes.
- Experimental reply flows now always show a risk-self-borne warning before entry, and the old setting that implied this protection could be disabled has been replaced with a fixed explanatory note.
- The settings, about, home, and explore surfaces now share explicit "public browsing first" recovery copy, so the visible product promise no longer implies a full Tieba replacement before account validation exists.

## Remaining Outdated Areas

### P1: Build Toolchain Is Still Pinned To Android 14

The project still builds against Android 14-era SDK settings and older status-bar adaptation patterns:

- `app/build.gradle.kts`
- `app/src/main/java/com/huanchengfly/tieba/post/activities/BaseActivity.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/fragments/BaseBottomSheetDialogFragment.java`
- `app/src/main/java/com/huanchengfly/tieba/post/utils/StatusBarUtil.java`

Current status:

- `buildToolsVersion`, `compileSdk`, and `targetSdk` are all still pinned to `34`.
- A lot of the classic Activity UI path still depends on `ImmersionBar`, `fitsSystemWindows`, `FLAG_TRANSLUCENT_STATUS`, and old fullscreen/status-bar flags.

Why this feels outdated:

- Even if the app compiles today, it is still tuned for Android 14-era assumptions.
- Any future target SDK bump toward Android 15 / 16 is likely to surface layout regressions around edge-to-edge, translucent pages, custom toolbars, and bottom sheets.
- This means the project is not only carrying old code patterns; its release toolchain is also behind the current Android platform curve.

Suggested direction:

- First make the current SDK 34 build reproducible on a complete local environment.
- Then stage a dedicated SDK bump branch and audit the shared status-bar / inset helpers before touching lots of feature pages.
- Prefer modern `WindowInsets`-based handling where practical instead of deepening the global `ImmersionBar` dependency.

### P2: WebView Location Has Been Removed

The in-app WebView geolocation path has now been disabled and the manifest location permissions were removed:

- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/webview/WebViewPage.kt`
- `app/src/main/AndroidManifest.xml`

Current status:

- WebView geolocation requests are denied in code.
- `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` have been removed from the manifest.

Why this still feels outdated:

- A forum client usually should not need app-wide location access just to open embedded web pages.
- The old implementation was niche and carried a broad permission footprint.

Suggested direction:

- Keep it disabled unless a real modern use case appears.
- If it ever returns, reintroduce it behind a settings switch and a strict domain allowlist.

### P2: Legacy Storage Permission Flow Has Shrunk

The old storage-permission burden around media picking has mostly been removed, but download / export compatibility paths still exist:

- `app/src/main/java/com/huanchengfly/tieba/post/utils/ImageUtil.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/utils/FileUtil.kt`

Why this feels outdated:

- Media picking now uses platform pickers and no longer needs the old Matisse + permission chain.
- The upload path no longer depends on deprecated `_data` / real-path lookups; picked media is staged into app-private cache first.
- On Android 9 and below, save / download flows no longer hard-fail when broad storage permission is unavailable; they fall back to app-specific external directories.
- Public shared-directory writes are no longer a first-class path on legacy Android, which reduces permission surface but still leaves compatibility tradeoffs around where files appear to the user.

Suggested direction:

- Keep shrinking the old storage-permission surface around download and export flows.
- If Android 9 and below are eventually dropped, the remaining compatibility branches can be reduced further.

### P2: Cleartext Traffic Is Still Broadly Enabled

The app manifest still declares `android:usesCleartextTraffic="true"`:

- `app/src/main/AndroidManifest.xml`

Why this is still a risk:

- Most first-party API and static resource paths have already been migrated to `https://`.
- External `http://` links are now pushed out to the system browser path, which reduces the app's own cleartext exposure.
- But the manifest still allows cleartext globally, so this is not yet a full lock-down.

Suggested direction:

- Keep this as a deliberate compatibility choice for now.
- Revisit it only after deciding whether the app should still render arbitrary cleartext pages inside its internal WebView.

### P3: OEM Manifest Compatibility Flags Still Look Frozen In Time

The manifest still carries several old vendor- or pre-fullscreen-era compatibility flags:

- `app/src/main/AndroidManifest.xml`

Examples:

- `android:largeHeap="true"`
- `android.max_aspect`
- `android.notch_support`
- `notch.config`
- `EasyGoClient`
- `use_miui_font`
- `ScopedStorage`
- `android.supports_size_changes`

Why this feels outdated:

- Most of these flags come from older OEM fullscreen / notch adaptation eras and are rarely seen in modern apps targeting Android 14+.
- None of them currently have a direct code reference in the project, which makes them hard to justify or validate.
- But removing them blindly can still regress display behavior on specific old vendor ROMs, and `largeHeap` may still be masking image-memory pressure in a media-heavy client.

Suggested direction:

- Treat these as a compatibility debt list, not a blind-delete list.
- Only remove them after runtime checks on representative old devices or emulators.
- If `largeHeap` is revisited, profile image-heavy flows first instead of assuming it is unnecessary.

### P1: Legacy WebView Enhancement Stack Was Carried For Too Long

The old `tblite.js` WebView enhancement stack was a legacy bundle that had carried very old public-CDN dependencies:

- Previously located under `app/src/main/assets/`

Examples found during cleanup:

- `jquery 1.12.4`
- `mdui 0.4.2`
- `jquery-cookie 1.4.1`
- `clipboard.js 2.0.1`
- domains such as `cdn.bootcss.com` and `cdnjs.loli.net`

Additional finding:

- Native code currently does not show a live `addJavascriptInterface(...)` or asset-injection path for `tblite.js`.
- This suggests part of the old WebView enhancement stack may already be disconnected legacy code.
- The unreferenced legacy JS assets can likely be removed from the packaged app after static verification.

Current status:

- The orphaned legacy JS assets have been removed from `app/src/main/assets`.
- The special `ClipboardGuardCopyRequest` branch in `WebViewPage.kt` has also been removed.

Why this feels outdated:

- Some of these mirrors are unstable, renamed, or region-dependent.
- The library versions themselves are very old and increase breakage risk in embedded pages.
- The project may still be carrying large legacy assets that are no longer on the real execution path.

Suggested direction:

- Keep watching runtime behavior in case there is a hidden dynamic asset load path not visible from static search.
- Vendor the required JS/CSS locally, or replace them with minimal internal code.
- Treat this as a reliability task, not just a dependency cleanup.

### P1: "AgreeMe / 赞过我" Parsing Is Still Heuristic

The notification tab is wired back, but its list mapping still guesses which payload field is used:

- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/notifications/list/NotificationsListViewModel.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/utils/NotificationFieldResolver.kt`

Current fallback rules:

- `agree_list` / `agreeme` are now treated as the primary fields.
- Legacy fallbacks are only used when the primary field is actually missing, not when it is present but empty / `0`.
- Fallbacks now emit explicit warning logs.
- The in-app "赞过我" list now shows a compatibility notice when it falls back to legacy list fields.

Why this feels outdated:

- The feature is now safer than the old silent heuristic path.
- But without confirming the live API response from a fresh test account, the remaining legacy fallback path can still show the wrong message type.

Suggested direction:

- Capture a fresh response sample from a real account and trim the compatibility alternates down to the real payload field.

### P1: Core Account Alpha Is Guarded But Still Not Live-Verified

The core account path has now been tightened around conservative guards, but it still needs test-account confirmation before it can be called stable:

- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/notifications/NotificationsPage.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/utils/BackgroundWorkScheduler.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/utils/TiebaUtil.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/utils/AccountUtil.kt`

Current status:

- Notifications no longer enter the stable path when the session is incomplete; the page now shows an explicit session-health gate instead of silently hitting account APIs.
- Manual sign now checks capability + complete session before enqueueing work and immediately tells the user why it was blocked.
- Account exit and switch now refresh the persisted account list before rebuilding current-account state, reducing stale unread / polling residue in multi-account scenarios.

Why this still feels unfinished:

- These guards make the app safer to validate, but they do not replace real server confirmation.
- Login health, notifications, sign, and exit/switch cleanup still need a full test-account pass to confirm that live cookies, unread fields, and worker state all line up.

Suggested direction:

- Keep these paths labeled as conservative core-account Alpha behavior until a test account verifies them end to end.

### P1: Public Web Forum Navigation Has More Content Dimensions Than The App

Compared with the current public Tieba web experience, forum browsing inside the app is still flatter than the official product:

- Public web references:
  - `https://tieba.baidu.com/`
  - public Tieba thread pages currently expose forum-level entry labels such as "看贴 / 图片 / 吧主推荐 / 视频 / 玩乐"
- App references:
  - `app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/ForumPage.kt`
  - `app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/threadlist/ForumThreadListPage.kt`

Current status:

- The app forum page now uses an extensible `ForumSurfaceTab` model and currently exposes `最新 / 精华 / 吧内搜索`.
- The app does have media/video-aware thread cards and a forum search page, so this is not a raw rendering limitation.
- What is missing is the richer forum-level navigation layer that helps users pivot into image-heavy, video-heavy, or curated content slices.

Why this feels outdated:

- The modern public web product presents a forum as more than a single chronological thread list.
- Keeping only `最新 / 精华` makes the client feel closer to an older Tieba era even when the lower content models already support richer media.

Suggested direction:

- Treat forum navigation parity as a product gap, not just a UI polish issue.
- If live APIs still expose the necessary filters, add one more staged tab set around media / recommendation dimensions.
- If those APIs are gone, at least avoid implying parity with the current official web forum experience.

### P2: Hot Topic Support Stops At The List Layer

The app already has hot-topic list models and entry points, but the navigation appears to stop before a real topic detail experience:

- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/explore/hot/HotPage.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/hottopic/list/HotTopicListPage.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/api/models/TopicDetailBean.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/interfaces/AppHybridTiebaApi.kt`

Current status:

- The app can show a `话题榜` entry and a full topic list page.
- A first-pass topic detail page and destination now exist, and topic items navigate into it.
- The current gap is now depth and pagination parity, not a missing route.

Why this feels outdated:

- Current public Tieba web surfaces hot topics as real destinations, not just labels in a ranking list.
- Stopping at the list layer makes the feature feel unfinished, even though the app already carries part of the data model.

Suggested direction:

- Either finish the topic detail route end to end, or intentionally reduce the surfaced entry points until the deeper page exists.

### P2: Device Identity Compatibility Is Still A Shim

Even after privacy hardening, the client still needs compatibility identifiers such as `AID`, `CUID`, `android_id`, and `_phone_imei` across the Retrofit and protobuf layers:

- `app/src/main/java/com/huanchengfly/tieba/post/utils/UIDUtil.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/api/ProtobufRequest.kt`

Why this is still a risk:

- The app no longer leaks real hardware IDs, which is good.
- But these fields are still compatibility shims and may need live validation against today's server behavior.

Suggested direction:

- Keep the current fake-but-stable approach for now.
- Validate key flows with real accounts before removing more fields.

### P2: Legacy ViewPager Visibility Helpers Are Mostly Dead Stock

The project still carries an old fragment visibility helper stack from the classic ViewPager era:

- `app/src/main/java/com/huanchengfly/tieba/post/fragments/BaseFragment.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/utils/HandleBackUtil.java`

Current status:

- Static search did not find any current subclasses of `BaseFragment`.
- Static search also did not find a live `ViewPager` / `ViewPager2` adapter chain in the main app code.
- The old `getUserVisibleHint()` check has been removed from back-press dispatch, but the base fragment itself still remains in the tree.

Why this feels outdated:

- `setUserVisibleHint()` / `getUserVisibleHint()` are long-deprecated fragment visibility APIs.
- Keeping an apparently unused ViewPager-era base class makes the project look more coupled to old fragment lifecycles than it really is.

Suggested direction:

- Treat `BaseFragment` as a dead-code candidate rather than a migration target.
- Before deleting it, do one more runtime pass through any old navigation paths that might still instantiate legacy fragments indirectly.

## Current Revival Order

1. Rebuild successfully in a complete Android SDK environment.
2. Stage an SDK bump plan from API 34 toward current Android releases and audit shared edge-to-edge / inset behavior.
3. Verify login, thread browsing, notifications, and sign-in end to end; keep posting/replying on the experimental path until secondary-account validation exists.
4. Decide whether to keep or delete WebView geolocation.
5. Confirm the real "AgreeMe" payload and remove heuristic parsing.
