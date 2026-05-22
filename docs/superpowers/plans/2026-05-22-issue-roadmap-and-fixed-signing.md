# Issue Roadmap And Fixed Signing Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Keep APK signatures stable across GitHub Actions builds, then prioritize issue-driven features from `zzc10086/TiebaLite` and `0ranko0P/TiebaLite`.

**Architecture:** GitHub Actions should sign CI and release APKs with the same persistent repository secrets instead of generating throwaway keys. Product work should be split into small, independently releasable fixes, starting with high-value, low-risk user pain points.

**Tech Stack:** Android Gradle Plugin, GitHub Actions, `r0adkll/sign-android-release`, Compose, existing TiebaLite repositories and settings infrastructure.

---

## Immediate Signing Work

- [x] Replace the manual workflow-dispatch temporary keystore with repository secrets in `.github/workflows/build.yml`.
- [x] Allow `.github/workflows/release.yml` to run in `xiaoancute/TiebaLite`, not only upstream `0ranko0P/TiebaLite`.
- [x] Create persistent GitHub secrets in `xiaoancute/TiebaLite`: `KEYSTORE`, `KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`.
- [ ] Push workflow changes to `main`.
- [ ] Run a GitHub Actions manual build and verify the output APK is signed with the persistent key.

Note: Users who installed a previous temporary-key CI APK must uninstall once when switching to the new fixed signing key. Builds after that should update in place.

## Issue-Driven Feature Order

### Task 1: Search Default Sort Setting

Source: `zzc10086/TiebaLite#101`

Why first: low risk and high daily-use value. The app already supports search thread sort types; this adds a persisted default.

Files to inspect:
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/search/thread/SearchThreadViewModel.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/search/thread/SearchThreadPage.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/models/settings/HabitSettings.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/repository/user/DataStoreSettingsRepository.kt`
- `app/src/main/res/values/strings.xml`

Acceptance:
- User can choose default search-post/thread sorting in settings.
- Search page starts with that default unless the user changes sorting inside the current search session.

### Task 2: Notification Permission Prompt Suppression

Source: `zzc10086/TiebaLite#75`, `zzc10086/TiebaLite#97`

Why second: small change with large annoyance reduction.

Files to inspect:
- `app/src/main/java/com/huanchengfly/tieba/post/MainActivityV2.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/models/settings/PrivacySettings.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/settings/PrivacySettingsPage.kt`

Acceptance:
- If the user declines notification permission, the app does not repeatedly prompt on every launch.
- A settings toggle can re-enable notification permission prompting or open system notification settings.

### Task 3: Search Deep Link Keyword

Source: `zzc10086/TiebaLite#47`

Why third: small, contained, easy to verify.

Files to inspect:
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/search/SearchPage.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/search/SearchViewModel.kt`

Acceptance:
- `tblite://search?keyword=原神` opens search with `原神` prefilled.
- Existing `tblite://search` behavior still works.

### Task 4: Restore Forum Scroll Position After Returning From Thread

Source: `zzc10086/TiebaLite#98`, `zzc10086/TiebaLite#59`

Why fourth: visible bug, but requires more careful navigation/list-state work.

Files to inspect:
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/ForumPage.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/threadlist/ForumThreadListPage.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/threadlist/ForumThreadListViewModel.kt`

Acceptance:
- Opening a thread from a forum and pressing back returns to the previous forum tab and scroll position.
- State remains isolated by forum name and selected nav tab.

### Task 5: Poll Thread Read Support

Source: `0ranko0P/TiebaLite#14`, `zzc10086/TiebaLite#99`

Why later: high value but higher protocol uncertainty.

Files to inspect:
- `app/src/main/protos/Post.proto`
- `app/src/main/protos/OriginThreadInfo.proto`
- `app/src/main/java/com/huanchengfly/tieba/post/api/models/ThreadContentBean.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/thread/ThreadPage.kt`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/common/PbContentRender.kt`

Acceptance:
- Poll threads no longer look broken or empty.
- If poll details are available only when logged in, the UI handles logged-out state gracefully.

## Lower Priority Backlog

- Preload next page toggle: `0ranko0P/TiebaLite#15`.
- Accessibility follow-up for TalkBack list paging: `zzc10086/TiebaLite#64`.
- Auto cache cleanup option: `zzc10086/TiebaLite#73`.
- UI density/visual-area refinements: `0ranko0P/TiebaLite#13`, `zzc10086/TiebaLite#52`.
