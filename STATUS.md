# TiebaLite Recovery Status

Last updated: 2026-03-22

## Current Goal

Recover TiebaLite into a stable, reading-first Android 10+ Tieba client. Public browsing is the mainline. Account-heavy and posting-heavy features stay conservative until there is real validation evidence.

## Baseline Snapshot

- Workspace path: `/home/x/My_Dev/TiebaLite-4.0-dev`
- Local Git history: not present at the start of this run; first execution task is to create a rescue repository baseline.
- Build baseline: `:app:assembleDebug` passed on 2026-03-22 with Java 17 and the local Android 34 SDK environment.
- Live browse smoke baseline: `:app:testDebugUnitTest --tests 'com.huanchengfly.tieba.post.PublicBrowseLiveSmokeTest'` passed on 2026-03-22.
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
| Workflow baseline | In progress | `TODO.md` / `STATUS.md` creation and local Git rescue setup are first. |
| Public browse confidence | In progress | Live smoke already passes; offline fixture coverage still needs expansion. |
| Reading-first product polish | Not started | Navigation and capability alignment audit pending. |
| Modern Android and Compose debt | Not started | Compat cleanup and inset/status-bar work are still queued. |
| Experimental/account containment | Not started | Guardrails exist but more explicit labeling and isolation remain. |
| Release hardening and delivery | Not started | Final docs, scripts, and manual matrix still pending. |

## Known Good Commands

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/tmp/tblite-android-sdk-14742923
export ANDROID_SDK_ROOT=/tmp/tblite-android-sdk-14742923
export GRADLE_USER_HOME=/tmp/tblite-gradle17-local

./gradlew :app:testDebugUnitTest --tests 'com.huanchengfly.tieba.post.PublicBrowseLiveSmokeTest' --console=plain
./gradlew :app:assembleDebug --stacktrace
```

## Next Actions

1. Initialize local Git rescue history and create the baseline commit.
2. Expand public browse verification beyond live smoke into fixture-based offline tests.
3. Audit visible entry points against stable/guarded/experimental capability states.
