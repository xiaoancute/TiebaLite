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
| Public browse confidence | In progress | Live smoke passes; T05 forum, T06 thread, and T07 hot topic/topic detail fixture coverage are in place; failure-mode tests are next. |
| Reading-first product polish | Not started | Navigation and capability alignment audit pending. |
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
./gradlew :app:assembleDebug --stacktrace
```

## Next Actions

1. Add failure-mode parsing tests for browse payload drift (`T09`).
2. Start auditing visible entry points against stable/guarded/experimental capability states (`T10`).
3. Tighten high-risk reply/post entry points with explicit risk-self-borne messaging (`T11`).
