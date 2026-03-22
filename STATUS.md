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
| Public browse confidence | In progress | Live smoke already passes; offline fixture coverage still needs expansion. |
| Reading-first product polish | Not started | Navigation and capability alignment audit pending. |
| Modern Android and Compose debt | Not started | Compat cleanup and inset/status-bar work are still queued. |
| Experimental/account containment | Not started | Guardrails exist but more explicit labeling and isolation remain. |
| Release hardening and delivery | Not started | Final docs, scripts, and manual matrix still pending. |

## Known Good Commands

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/home/x/Android/Sdk
export ANDROID_SDK_ROOT=/home/x/Android/Sdk
export GRADLE_USER_HOME=/tmp/tblite-gradle17-local

./gradlew :app:testDebugUnitTest --tests 'com.huanchengfly.tieba.post.PublicBrowseLiveSmokeTest' --console=plain
./gradlew :app:assembleDebug --stacktrace
```

## Next Actions

1. Expand public browse verification beyond live smoke into fixture-based offline tests.
2. Audit visible entry points against stable/guarded/experimental capability states.
3. Update `docs/development-setup.md` to prefer the persistent SDK path and document why `/tmp` SDK paths are unsafe.
