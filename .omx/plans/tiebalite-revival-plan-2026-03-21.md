# TiebaLite Revival Plan

Date: 2026-03-21
Scope: Bring the client back to a usable, maintainable state on modern Android and current Tieba server behavior.

## Requirements Summary

- Keep the existing app architecture and UI shell rather than rewriting from scratch.
- Prioritize survival of core flows: login, browse forum/thread, reply/post, notifications, favorites/history.
- Treat server compatibility and Android platform compatibility as higher priority than visual refresh.
- Reduce reliance on retired or deprecated platform services.
- Avoid large-scope refactors until core functionality is proven working again.

## Current State

- Core app shell is intact: main navigation covers home, explore, notifications, and user areas in [app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/MainPage.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/MainPage.kt#L139).
- Core content flows exist: forum, thread, subposts, reply, search, favorites, history, user profile.
- Video playback support exists in feed rendering in [app/src/main/java/com/huanchengfly/tieba/post/ui/widgets/compose/FeedCard.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/ui/widgets/compose/FeedCard.kt#L462).
- Hot topic support exists via `topicListFlow()` in [app/src/main/java/com/huanchengfly/tieba/post/ui/page/hottopic/list/HotTopicListViewModel.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/ui/page/hottopic/list/HotTopicListViewModel.kt#L42).
- Message center is incomplete: only reply and @ tabs are surfaced in [app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/notifications/NotificationsPage.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/notifications/NotificationsPage.kt#L49), while API support for `agreeMe()` already exists in [app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/ITiebaApi.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/ITiebaApi.kt#L735).
- Login depends on WebView cookie extraction from Baidu login pages in [app/src/main/java/com/huanchengfly/tieba/post/ui/page/login/LoginPage.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/ui/page/login/LoginPage.kt#L72) and [app/src/main/java/com/huanchengfly/tieba/post/ui/page/login/LoginPage.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/ui/page/login/LoginPage.kt#L255).
- Network compatibility currently relies on multiple spoofed Tieba client versions in [app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt#L83).
- App Center is still wired for analytics/crash/update distribution in [app/src/main/java/com/huanchengfly/tieba/post/App.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/App.kt#L95).
- One-key sign-in still depends on deprecated `IntentService` in [app/src/main/java/com/huanchengfly/tieba/post/services/OKSignService.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/services/OKSignService.kt#L32).
- Permissions and device identifiers are broad and legacy-heavy in [app/src/main/AndroidManifest.xml](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/AndroidManifest.xml#L9), [app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt#L64), and [app/src/main/java/com/huanchengfly/tieba/post/utils/MobileInfoUtil.java](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/utils/MobileInfoUtil.java#L11).
- Test coverage is effectively absent: only example tests exist in [app/src/androidTest/java/com/huanchengfly/tieba/post/ExampleInstrumentedTest.java](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/androidTest/java/com/huanchengfly/tieba/post/ExampleInstrumentedTest.java) and `app/src/test/java/.../ExampleUnitTest.java`.

## Acceptance Criteria

- The app can complete login with at least one working modern Baidu/Tieba flow on a current Android device.
- The app can open home, forum, thread, favorites, history, search, and reply flows without fatal errors.
- Notifications support at minimum reply, @, and like/agree messages.
- The app no longer depends on App Center Distribute for updates.
- One-key sign-in no longer depends on `IntentService`.
- Core networking constants and request signatures are centralized enough that updating client spoof parameters does not require a repo-wide hunt.
- At least one smoke test path exists for login-independent browsing logic and one for notification parsing or repository logic.

## Phase Plan

### P0: Make It Build And Observe

Goal: Get a reproducible local build and enough diagnostics to stop guessing.

Steps:

1. Normalize local build prerequisites.
   Files:
   [app/build.gradle.kts](/home/x/My_Dev/TiebaLite-4.0-dev/app/build.gradle.kts#L37)
   [gradle/sweet-dependency/sweet-dependency-config.yaml](/home/x/My_Dev/TiebaLite-4.0-dev/gradle/sweet-dependency/sweet-dependency-config.yaml#L35)
   Actions:
   - Accept/install SDK 34 components.
   - Confirm AGP/Kotlin/KSP/Wire compatibility on the current toolchain.
   - Document exact build env in README or a new dev doc.

2. Add a compatibility debug checklist.
   Files:
   [README.md](/home/x/My_Dev/TiebaLite-4.0-dev/README.md#L11)
   Actions:
   - Record current known-good Android version, SDK setup, and login preconditions.
   - Record what flows currently fail and how to reproduce them.

3. Add first smoke-test targets around parsing and repository logic.
   Files:
   [app/src/test/java/com/huanchengfly/tieba/post/ExampleUnitTest.java](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/test/java/com/huanchengfly/tieba/post/ExampleUnitTest.java)
   [app/src/main/java/com/huanchengfly/tieba/post/repository/FrsPageRepository.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/repository/FrsPageRepository.kt)
   [app/src/main/java/com/huanchengfly/tieba/post/repository/PbPageRepository.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/repository/PbPageRepository.kt)

Exit condition:
- Debug build succeeds locally.
- We have a short “known issues” doc instead of tribal memory.

### P1: Stabilize Server Compatibility

Goal: Restore the highest-risk online flows without changing product scope yet.

Steps:

1. Centralize and audit client spoof/version parameters.
   Files:
   [app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/api/retrofit/RetrofitTiebaApi.kt#L83)
   [app/src/main/java/com/huanchengfly/tieba/post/api/HttpConstant.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/api/HttpConstant.kt#L1)
   Actions:
   - Move app-version spoof strings into a dedicated config object.
   - Annotate which endpoints require which spoof family.
   - Remove accidental duplication where possible.

2. Revalidate login.
   Files:
   [app/src/main/java/com/huanchengfly/tieba/post/ui/page/login/LoginPage.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/ui/page/login/LoginPage.kt#L72)
   [app/src/main/java/com/huanchengfly/tieba/post/utils/AccountUtil.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/utils/AccountUtil.kt#L143)
   Actions:
   - Confirm cookie extraction still works against current Baidu login.
   - Add failure-state diagnostics for missing `BDUSS` or `STOKEN`.
   - Add a fallback path if WebView lands on extra verification or unexpected hosts.

3. Revalidate browse/post flows in priority order.
   Files:
   [app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/ForumPage.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/ForumPage.kt)
   [app/src/main/java/com/huanchengfly/tieba/post/ui/page/thread/ThreadPage.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/ui/page/thread/ThreadPage.kt)
   [app/src/main/java/com/huanchengfly/tieba/post/ui/page/reply/ReplyPage.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/ui/page/reply/ReplyPage.kt)
   Actions:
   - Verify forum list, thread page, subposts, and reply submission against live endpoints.
   - Note any fields that vanished or changed semantics.

Exit condition:
- Login, forum browse, thread browse, and reply all work on a real account.

### P2: Fill The Most Visible Product Gaps

Goal: Bring the app from “works again” to “doesn’t feel abandoned”.

Steps:

1. Complete notifications.
   Files:
   [app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/notifications/NotificationsPage.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/notifications/NotificationsPage.kt#L49)
   [app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/notifications/list/NotificationsListViewModel.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/notifications/list/NotificationsListViewModel.kt#L71)
   [app/src/main/java/com/huanchengfly/tieba/post/services/NotifyJobService.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/services/NotifyJobService.kt#L53)
   Actions:
   - Add “赞过我” tab and badge handling.
   - Update push/local notification routing to match.

2. Audit discover/feed relevance.
   Files:
   [app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/explore/ExplorePage.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/explore/ExplorePage.kt)
   [app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/explore/personalized/PersonalizedViewModel.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/ui/page/main/explore/personalized/PersonalizedViewModel.kt)
   Actions:
   - Check whether personalized, concern, and hot tabs still align with live Tieba behavior.
   - Remove or relabel dead/low-value surfaces if the upstream data is unstable.

3. Review web fallback boundaries.
   Files:
   [app/src/main/java/com/huanchengfly/tieba/post/utils/utils.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/utils/utils.kt#L201)
   [app/src/main/java/com/huanchengfly/tieba/post/ui/page/webview/WebViewPage.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/ui/page/webview/WebViewPage.kt)
   Actions:
   - Make unsupported flows degrade cleanly into WebView or browser instead of “功能未实现”.

Exit condition:
- Everyday use feels coherent even if some advanced features still fall back to web.

### P3: Modernize Android Platform Integration

Goal: Remove the most brittle Android-side technical debt.

Steps:

1. Replace deprecated sign-in service path.
   Files:
   [app/src/main/java/com/huanchengfly/tieba/post/services/OKSignService.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/services/OKSignService.kt#L32)
   [app/src/main/AndroidManifest.xml](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/AndroidManifest.xml#L24)
   Actions:
   - Migrate from `IntentService` to `WorkManager` or a foreground-service-safe pattern.
   - Revisit `FOREGROUND_SERVICE_SPECIAL_USE` necessity.

2. Remove retired update distribution.
   Files:
   [app/src/main/java/com/huanchengfly/tieba/post/App.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/App.kt#L95)
   [app/build.gradle.kts](/home/x/My_Dev/TiebaLite-4.0-dev/app/build.gradle.kts#L198)
   Actions:
   - Remove App Center Distribute.
   - Decide whether analytics/crashes remain, and if so migrate to a maintained provider or disable by default.

3. Tighten permissions and identity collection.
   Files:
   [app/src/main/AndroidManifest.xml](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/AndroidManifest.xml#L9)
   [app/src/main/java/com/huanchengfly/tieba/post/utils/MobileInfoUtil.java](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/utils/MobileInfoUtil.java#L11)
   [app/src/main/java/com/huanchengfly/tieba/post/components/OAIDGetter.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/components/OAIDGetter.kt)
   Actions:
   - Minimize requested permissions.
   - Make tracking/device-ID behavior explicit and optional where feasible.

Exit condition:
- The app no longer depends on deprecated Android components or retired backend services.

### P4: Quality And Maintenance

Goal: Make continued revival sustainable.

Steps:

1. Add repository/viewmodel tests around login parsing, notifications, and thread/favorite transformations.
2. Add a lightweight compatibility matrix doc.
3. Add logging toggles for network handshake, login, and push/message parsing.
4. Audit strings for outdated copy such as “该功能尚未实现”.

Exit condition:
- A future maintainer can diagnose breakage without reverse-engineering the whole app.

## Recommended First Sprint

1. Fix local build and document the environment.
2. Verify live login and cookie extraction.
3. Verify forum/thread/reply against live endpoints.
4. Add “赞过我” to notifications and local badge/update logic.
5. Remove App Center Distribute path.

This sprint gives the best ratio of survival value to effort.

## Risks And Mitigations

- Risk: Tieba server-side anti-abuse rules reject spoofed clients.
  Mitigation:
  Centralize version spoofing, capture failing endpoints, and keep a small compatibility surface.

- Risk: WebView login breaks due to Baidu verification flow changes.
  Mitigation:
  Add explicit detection and debug telemetry around missing cookies and unexpected redirects.

- Risk: Background sign-in is killed or policy-blocked on newer Android.
  Mitigation:
  Migrate to supported scheduling and foreground execution patterns before polishing secondary features.

- Risk: Privacy-sensitive identifiers trigger user distrust or store review issues.
  Mitigation:
  Remove unneeded permissions first and gate remaining device identifiers behind clear rationale.

- Risk: The project revives technically but still feels outdated.
  Mitigation:
  After P1, spend one focused sprint on message completeness, feed coherence, and fallback polish.

## Verification Steps

- Build debug APK from a clean environment.
- Log in with a real account and verify account persistence after app restart.
- Open home, one forum, one thread, one subpost chain, and submit one reply.
- Open favorites and history flows and verify navigation state restoration.
- Verify notifications page supports reply, @, and like categories.
- Trigger one-key sign-in and confirm success/failure notification behavior on Android 13+.
- Run unit tests for parsing/repository logic added in P0 or P4.

## Recommendation

Do not start with a UI redesign.
Start with P0 and P1, because if login and endpoint compatibility are shaky, every prettier layer above them will be built on sand.
