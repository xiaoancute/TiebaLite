# TiebaLite Autonomous Recovery TODO

Last updated: 2026-03-22

## Mission

Bring TiebaLite back as a modern, Android 10+ oriented, reading-first Tieba client that is stable for public browsing. Keep high-risk account flows conservative. Do not treat posting, replying, or auto sign as required for this recovery cycle.

## Working Assumptions

- Primary product target: stable public browsing on modern Android.
- Current SDK baseline stays on `compileSdk = 34` / `targetSdk = 34` during the early stabilization phases.
- Main-account or test-account validation is out of scope for now; account-heavy features remain guarded.
- This workspace currently has no Git history, so the first execution step is to establish a local rescue repository for atomic commits.
- Current live evidence already shows public Tieba browse endpoints are readable through app-style signed requests and web search/topic endpoints.

## Definition Of Done

- `assembleDebug` is reproducible in the local Android SDK environment.
- Public browse routes are covered by deterministic tests plus live smoke coverage.
- Reading-first navigation and capability labeling are consistent across visible entry points.
- Experimental features are clearly marked and no longer masquerade as stable paths.
- Modern Android and Compose debt is materially reduced on the main browse path.
- Docs, status, and release notes reflect the real product state.

## Execution Rules

- Complete tasks in dependency order unless a later task becomes a blocker fix.
- After each task: implement, commit with a clear message, run the smallest meaningful verification, then update `STATUS.md`.
- If a task fails verification, enter debug -> fix -> retry before moving on.
- Keep posting/replying/auto sign behind explicit risk boundaries unless a later milestone deliberately expands scope.

## Phases

1. Workflow baseline
2. Public browse confidence
3. Reading-first product polish
4. Modern Android and Compose debt
5. Experimental/account containment
6. Release hardening and delivery

## Task List

| ID | Pri | Depends | Task | Acceptance |
| --- | --- | --- | --- | --- |
| T00 | P0 | None | Initialize a local Git rescue repository in this workspace and create the first baseline commit. | `.git` exists locally, `.gitignore` is honored, and the current snapshot is restorable by commit hash. |
| T01 | P0 | T00 | Record the known-good local build environment and baseline commands in `STATUS.md` and supporting docs. | `STATUS.md` contains Java/Android env, key Gradle commands, and the current baseline build result. |
| T02 | P0 | T00 | Create the autonomous execution bookkeeping docs: `TODO.md` as source of truth and `STATUS.md` as progress ledger. | Both files exist, list current phase, and can be updated after each task without ambiguity. |
| T03 | P0 | T00 | Inventory the current automated verification surface: unit tests, live smoke tests, build commands, and missing coverage. | `STATUS.md` names what is already covered and what is still blind. |
| T04 | P0 | T01,T03 | Stabilize the public browse live smoke suite so it covers search, forum list, thread page, hot topic list, and topic detail against current live payloads. | `PublicBrowseLiveSmokeTest` passes consistently and fails with actionable messages when payloads drift. |
| T05 | P0 | T04 | Add fixture-based tests for forum page parsing to reduce dependence on live network-only verification. | A local test covers at least one current forum payload shape and passes without network access. |
| T06 | P0 | T04 | Add fixture-based tests for thread page parsing, including anti block, forum metadata, and post list presence. | A local test validates current signed thread response essentials and passes offline. |
| T07 | P0 | T04 | Add fixture-based tests for hot topic list and topic detail parsing based on the 2026 payload structure. | Topic parsing tests assert `data.list.ret[*].mul_id/mul_name` compatibility and pass offline. |
| T08 | P0 | T03 | Add focused tests for URL routing and external link behavior so Tieba links stay in-app while external cleartext links escape to browser paths. | Link-routing tests cover Tieba vs external URLs and preserve the intended browser fallback behavior. |
| T09 | P0 | T04,T05,T06,T07 | Add failure-mode tests for public browse parsing when fields disappear, arrays are empty, or API errors are returned. | Parsing tests fail safe with clear degraded states instead of throwing opaque crashes. |
| T10 | P0 | T03 | Audit all first-level and second-level user entry points for capability state alignment with `RevivalFramework`. | Every visible entry is classified as stable, guarded, experimental, or unavailable in docs/status. |
| T11 | P0 | T10 | Tighten risky reply/post entry points so they always show explicit "risk self-borne" messaging before execution. | Reply/post surfaces no longer present as normal stable actions without warning copy. |
| T12 | P0 | T10 | Add or refine read-only messaging in key reading surfaces and settings so the product promise matches the current recovery scope. | Main reading surfaces and settings communicate "公开浏览优先" without contradicting docs. |
| T13 | P1 | T10 | Continue removing stale dead-end affordances from reading-first navigation where they still imply stable account completeness. | Main navigation and top actions no longer point users into silent-fail or misleading flows. |
| T14 | P1 | T10 | Ensure every guarded account page has an explicit degraded UI state instead of a silent empty page. | Unavailable account pages show a clear capability/session explanation and no longer fail silently. |
| T15 | P0 | T08 | Audit `WebViewPage` interception and browser fallback behavior for current Tieba/public-web usage. | Tieba internal pages still open in-app when intended, external links prefer browser/custom tabs, and tests or smoke evidence cover the behavior. |
| T16 | P1 | T04 | Harden the forum surface abstraction so the current `最新 / 精华 / 吧内搜索` model is cleanly extensible to image/video/recommend later. | `ForumSurfaceTab` and `ForumPage` no longer assume a two-tab legacy layout internally. |
| T17 | P1 | T04,T09 | Add clearer browse-path diagnostics/logging around forum, thread, and topic loading failures. | Failure logs identify route, endpoint class, and capability context without exposing sensitive data. |
| T18 | P1 | T03 | Finish the `placeholder` compatibility cleanup so only the internal compat layer touches accompanist placeholder APIs. | `rg -n "accompanist\\.placeholder|PlaceholderHighlight|fade\\(" app/src/main/java` only hits the compat layer or intentional wrappers. |
| T19 | P1 | T03 | Finish the bottom-sheet and swipeable compatibility cleanup so business pages stop importing old material swipe APIs directly. | `rg -n "SwipeableDefaults|FractionalThreshold|rememberModalBottomSheetState\\(|ModalBottomSheetValue" app/src/main/java` only hits compat wrappers or clearly-documented holdouts. |
| T20 | P1 | T18,T19 | Reduce `BaseActivity` and `StatusBarUtil` dependence on legacy immersive flags on the main browse path. | Browse-first activities/pages rely less on `ImmersionBar`, `fitsSystemWindows`, and legacy translucent status behavior. |
| T21 | P1 | T20 | Audit XML/layout `fitsSystemWindows` usage in read-heavy screens and remove stale cases that conflict with modern insets handling. | Read-path layouts avoid unnecessary `fitsSystemWindows` shims that would block future edge-to-edge work. |
| T22 | P1 | T20 | Classify legacy manifest compatibility flags and decide which ones are still justified for the recovery branch. | Manifest compatibility debt is documented item-by-item with keep/remove rationale. |
| T23 | P1 | T15 | Revisit cleartext policy and document whether `usesCleartextTraffic` can be narrowed without breaking supported flows. | Cleartext remains a deliberate compatibility choice or is tightened with explicit supporting evidence. |
| T24 | P1 | T01 | Eliminate the current AGP warning around `android.defaults.buildfeatures.buildconfig=true` if it is safe to do so. | The build warning is removed or a documented blocker explains why it remains. |
| T25 | P1 | T01 | Refresh plugin/library debt that is low-risk and independent from an SDK bump, especially SweetProperty/SweetDependency and lightweight AndroidX updates. | Low-risk version bumps build successfully and do not expand scope into a platform migration. |
| T26 | P1 | T03 | Expand pure JVM tests around `SessionHealth`, `NotificationFieldResolver`, and other guardrail logic that should not need a real account. | Core guardrail logic has deterministic unit coverage and passes without device/emulator access. |
| T27 | P1 | T26 | Audit background work entry points so read-only users or incomplete sessions never retain stale polling or stale sign work. | `BackgroundWorkScheduler` and `AccountUtil` remain the only scheduling/cleanup choke points. |
| T28 | P1 | T10,T27 | Further isolate posting, replying, and auto sign under experimental capability gates and guard text. | Experimental features cannot be mistaken for stable browse functionality. |
| T29 | P1 | T12,T28 | Update docs and in-app summaries so the recommended usage path is clearly "reading-first" rather than "full Tieba replacement." | README/docs/settings copy aligns with the actual supported feature set. |
| T30 | P1 | T04,T26 | Add a reproducible local verification script or checklist for the recovery branch. | A single documented command set covers build, browse smoke tests, and key unit tests. |
| T31 | P0 | T04,T18,T19,T26 | Re-run the full local validation baseline after the above stabilization tasks. | `assembleDebug` plus the targeted smoke/unit suite pass from a clean-enough local run. |
| T32 | P1 | T31 | Produce a refreshed functionality audit that states what is stable, what is behind guards, and what is intentionally not promised. | `docs/feature-status.md` and `docs/revival-audit.md` reflect the new evidence and do not overclaim. |
| T33 | P1 | T31 | Build a release-readiness checklist for Android 10 / 13 / latest-mainline Android browse behavior, even if device runs are deferred. | `STATUS.md` contains the pending manual matrix and the exact routes to verify on hardware later. |
| T34 | P2 | T22,T23,T31 | Prepare an isolated SDK-upgrade branch plan for `compileSdk` / `targetSdk` uplift after browse stability is locked. | A post-recovery branch plan exists and is explicitly separated from the main stabilization track. |
| T35 | P2 | T31 | Evaluate whether forum-level `图片 / 视频 / 推荐` surfaces should land in this branch or be deferred. | The decision is documented with scope, blockers, and expected user value. |
| T36 | P2 | T31 | Audit whether any remaining WebView-only paths can be slimmed down or replaced with cleaner browser/native routes. | WebView usage is reduced to deliberate, justified cases only. |
| T37 | P2 | T31 | Review legacy manifest and permission leftovers one more time after the main stabilization work settles. | Any further permission/manifest removals are backed by passing build/tests and updated docs. |
| T38 | P2 | T31,T32 | Package a changelog-style recovery summary for downstream maintainers. | A maintainer-facing summary explains the recovery baseline, known gaps, and next branch recommendations. |

## Immediate Execution Order

1. T00-T04
2. T05-T09
3. T10-T17
4. T18-T25
5. T26-T33
6. T34-T38

## Explicitly Deferred Unless Evidence Changes

- Stable posting workflow
- Stable reply workflow
- Real-account login/notification/sign validation
- Android 15/16 target SDK migration inside the current stabilization batch
