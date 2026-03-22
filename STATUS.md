# TiebaLite Recovery Status

Last updated: 2026-03-22

## Current Goal

Recover TiebaLite into a stable, reading-first Android 10+ Tieba client. Public browsing is the mainline. Account-heavy and posting-heavy features stay conservative until there is real validation evidence.

## Baseline Snapshot

- Workspace path: `/home/x/My_Dev/TiebaLite-4.0-dev`
- Local Git history: initialized in this run; current rescue branch is `main`.
- Baseline rescue commits:
  - `680327e` `chore: import autonomous recovery baseline`
  - `8c96ed4` `build: set aar metadata sdk fallback values`
  - `61b8cf4` `build: apply aar metadata fallback after evaluation`
  - `61fe438` `build: remove temporary aar metadata workaround`
- Build baseline: `:app:assembleDebug` passed on 2026-03-22 with Java 17 and the Android 34 SDK installed at `/home/x/Android/Sdk`.
- Live browse smoke baseline: `:app:testDebugUnitTest --tests 'com.huanchengfly.tieba.post.PublicBrowseLiveSmokeTest'` passed on 2026-03-22.
- Offline forum fixture baseline: `:app:testDebugUnitTest --tests 'com.huanchengfly.tieba.post.ForumPageFixtureTest'` passed on 2026-03-22.
- Offline thread fixture baseline: `:app:testDebugUnitTest --tests 'com.huanchengfly.tieba.post.ThreadPageFixtureTest'` passed on 2026-03-22.
- Offline hot topic fixture baseline: `:app:testDebugUnitTest --tests 'com.huanchengfly.tieba.post.HotTopicFixtureTest'` passed on 2026-03-22.
- Public browse failure-mode baseline: `:app:testDebugUnitTest --tests 'com.huanchengfly.tieba.post.PublicBrowseFailureModeTest'` passed on 2026-03-22.
- Environment correction discovered during this run:
  - the old temporary SDK path `/tmp/tblite-android-sdk-14742923` had expired and no longer contained `platforms/android-34` or `build-tools/34.0.0`
  - switching to `/home/x/Android/Sdk` restored AGP task graph resolution and normal builds
- Current validated public browse routes:
  - forum search
  - thread search
  - signed forum page
  - signed thread page
  - hot topic list
  - topic detail
- Current environment limits:
  - no connected Android device
  - no working emulator/AVD tooling in this workspace
  - no real-account validation in scope

## Product Positioning

- Stable mainline: public browsing and reading
- Guarded mainline: conservative account/session guardrails
- Experimental only: posting, replying, auto sign
- Deferred: SDK bump to 35/36-era targets and real-account end-to-end claims

## Phase Tracker

| Phase | Status | Notes |
| --- | --- | --- |
| Workflow baseline | Completed | `TODO.md`, `STATUS.md`, local Git rescue history, and reproducible SDK path are now in place. |
| Public browse confidence | In progress | Live smoke passes; T05 forum, T06 thread, T07 hot topic/topic detail fixture coverage, T08 link-routing coverage, and T09 failure-mode guardrails are now in place. |
| Reading-first product polish | In progress | T10 capability-state audit is now landing in docs and settings summaries; T11/T12 entry messaging cleanup is next. |
| Modern Android and Compose debt | Not started | Compat cleanup and inset/status-bar work are still queued. |
| Experimental/account containment | Not started | Guardrails exist but more explicit labeling and isolation remain. |
| Release hardening and delivery | Not started | Final docs, scripts, and manual matrix still pending. |

## Recent Progress

- T05 completed: added an offline signed forum payload snapshot and `ForumPageFixtureTest` to lock the current mini forum JSON shape into local regression coverage.
- The fixture test validates both raw payload structure and `ForumPageBean` parsing, including `frs_common_info`, tab layout, thread abstracts, media payloads, and author linkage.
- `MediaAdapter` no longer depends on `android.text.TextUtils`, so forum media parsing now works in pure JVM tests instead of requiring Android framework stubs.
- T06 completed: added an offline signed thread payload snapshot and `ThreadPageFixtureTest` to lock the current thread JSON shape into local regression coverage.
- The thread fixture validates `forum`, `anti`, `thread`, `post_list`, Tieba in-app links, and `ThreadContentBean` parsing without any network dependency.
- T07 completed: added offline hot topic list and topic detail snapshots plus `HotTopicFixtureTest` to lock `mul_id / mul_name / topic_info` compatibility into local regression coverage.
- `TopicDetailBean` now matches the current web payload more closely: `relate_forum` is explicitly mapped and `discuss_num` is coerced from either numeric or string primitives for serializer stability.
- T09 completed: added `PublicBrowseFailureModeTest` plus a shared `PublicBrowsePayloadGuard` so forum, thread, hot topic, and topic detail payload drift now degrades through explicit browse-specific exceptions instead of generic opaque failures.
- Topic detail parsing is now nullable/default-aware for missing optional sections, and the hot topic web model now keeps nested `topic_id / topic_name` fallback fields for route extraction when `mul_id / mul_name` disappear.
- Re-verified the browse regression set after T09: `PublicBrowseFailureModeTest`, `ForumPageFixtureTest`, `ThreadPageFixtureTest`, `HotTopicFixtureTest`, and `:app:assembleDebug` all passed on 2026-03-22.
- T08 completed: extracted pure-JVM `LinkRouting` decisions for app-level launch routing and `WebViewPage` interception, so Tieba thread/forum links, external browser fallbacks, and third-party scheme launches are now covered without relying on `android.net.Uri` in unit tests.
- Added `LinkRoutingTest` to lock current link behavior around in-app Tieba navigation, `/mo/q/checkurl` redirects, external cleartext browser fallback, optional HTTPS WebView retention, and third-party scheme dispatch.
- Re-verified the browse regression set after T08: `LinkRoutingTest`, `PublicBrowseFailureModeTest`, `ForumPageFixtureTest`, `ThreadPageFixtureTest`, `HotTopicFixtureTest`, and `:app:assembleDebug` all passed on 2026-03-22.
- T10 completed: added a visible-entry capability audit to `docs/feature-status.md`, classifying first-level and second-level routes as stable, core-account guarded, experimental, or not promised.
- Settings entry points now surface capability context directly: account management shows current session-health-aware core-account summary, and OKSign now shows manual-sign versus auto-sign capability states in the entry summary.
- `Explore` second-level tabs now recompute against login state, so the concern-feed entry no longer drifts or survives incorrectly after logout or account changes.

## Known Good Commands

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/home/x/Android/Sdk
export ANDROID_SDK_ROOT=/home/x/Android/Sdk
export GRADLE_USER_HOME=/tmp/tblite-gradle17-local

./gradlew :app:testDebugUnitTest --tests 'com.huanchengfly.tieba.post.PublicBrowseLiveSmokeTest' --console=plain
./gradlew :app:testDebugUnitTest --tests 'com.huanchengfly.tieba.post.ForumPageFixtureTest' --console=plain
./gradlew :app:testDebugUnitTest --tests 'com.huanchengfly.tieba.post.ThreadPageFixtureTest' --console=plain
./gradlew :app:testDebugUnitTest --tests 'com.huanchengfly.tieba.post.HotTopicFixtureTest' --console=plain
./gradlew :app:testDebugUnitTest --tests 'com.huanchengfly.tieba.post.LinkRoutingTest' --console=plain
./gradlew :app:testDebugUnitTest --tests 'com.huanchengfly.tieba.post.PublicBrowseFailureModeTest' --console=plain
./gradlew :app:assembleDebug --stacktrace
```

## Next Actions

1. Tighten high-risk reply/post entry points with explicit risk-self-borne messaging (`T11`).
2. Add or refine read-only recovery messaging in key browse surfaces and settings (`T12`).
3. Continue removing misleading dead-end affordances from reading-first navigation (`T13`).
