# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TiebaLite is an unofficial Baidu Tieba (贴吧) Android client, currently in a recovery/revival phase focused on stable public browsing. The app is Kotlin-first with Jetpack Compose UI.

**Current recovery goal:** reading-first Android 10+ client. Public browsing is mainline; account/posting features are guarded or experimental.

## Build & Environment

```bash
# Required environment
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/home/x/Android/Sdk
export ANDROID_SDK_ROOT=/home/x/Android/Sdk

# Debug build
./gradlew :app:assembleDebug --stacktrace

# Release build (requires keystore.properties)
./gradlew assembleRelease

# Run all unit tests
./gradlew :app:testDebugUnitTest --console=plain

# Run a single test class
./gradlew :app:testDebugUnitTest --tests 'com.huanchengfly.tieba.post.ForumPageFixtureTest' --console=plain
```

Requirements: Java 17, Android SDK Platform 34, Build-Tools 34.0.0. Run `scripts/check-android-env.sh` to verify.

## Architecture

### MVI Pattern (Intent → PartialChange → State → UI)

Every feature page follows this pattern via `BaseViewModel<Intent, PartialChange, State, Event>` in `app/.../arch/`:

1. **UiIntent** — user action dispatched via `viewModel.send(intent)`
2. **PartialChangeProducer** — transforms intent flow into `PartialChange` flow (where async work happens)
3. **PartialChange.reduce(oldState)** — pure function producing new state
4. **UiState** — immutable state observed by Compose UI via `viewModel.uiState`
5. **UiEvent** — one-shot side effects (toasts, navigation) emitted from `dispatchEvent()`

To add a new page: define its `UiState`, `UiIntent`, `PartialChange`, create a `ViewModel` extending `BaseViewModel`, and wire a `PartialChangeProducer`.

### Navigation

- **Compose Destinations** (v1.10.0) generates type-safe navigation from `@Destination` annotations
- Main nav host in `MainActivityV2.kt` with bottom sheet support
- KSP generates destinations into `build/generated/ksp/`
- Deep links: `com.baidu.tieba://unidispatch/frs` (forums), `/pb` (threads)

### API Layer

- **Retrofit + OkHttp** for HTTP, with custom interceptors for Tieba API signing
- **Wire Protocol Buffers** (4.9.3) — 677+ `.proto` files in `app/src/main/protos/`
- API facade: `TiebaApi.kt` / `RetrofitTiebaApi.kt`
- Repository pattern: `repository/` package (e.g., `FrsPageRepository`, `PbPageRepository`)

### Dependency Management

- **SweetDependency** (1.0.4) — dependencies declared in `gradle/sweet-dependency/sweet-dependency-config.yaml`, referenced via `autowire()` in build scripts
- **SweetProperty** (1.0.5) — reads `application.properties` and `keystore.properties` as typed properties accessible via `property.*`
- **Hilt** for DI with `kapt` (not KSP)

### Key Packages

```
com.huanchengfly.tieba.post/
├── arch/           # BaseViewModel, UiState/Intent/Event/PartialChange contracts
├── api/            # Retrofit services, interceptors, protobuf request helpers
├── repository/     # Data access layer (forum, thread, post, personalized)
├── models/         # API response beans and database models (LitePal ORM)
├── ui/
│   ├── page/       # Screen destinations (forum/, thread/, main/, search/, settings/, ...)
│   ├── widgets/compose/  # Reusable Compose components
│   └── common/theme/     # Material You dynamic theming system
├── utils/          # AccountUtil, ClientUtils, ThemeUtil, DataStore helpers
├── services/       # Notification helper, OKSign tile service
└── workers/        # WorkManager tasks (notification polling, auto sign)
```

### Theming

Custom theme system with Material You dynamic colors (Android 12+), day/night mode, translucent theme support, and custom color picker. Theme colors resolve through attribute lookup in `App.kt`'s theme delegate.

## Testing

Tests live in `app/src/test/` (JVM unit tests) and `app/src/androidTest/` (instrumented). Current test suite focuses on public browse regression:

- **Fixture tests** (`ForumPageFixtureTest`, `ThreadPageFixtureTest`, `HotTopicFixtureTest`) — offline JSON snapshot parsing
- **Failure-mode tests** (`PublicBrowseFailureModeTest`) — graceful degradation when API fields drift
- **Link routing tests** (`LinkRoutingTest`) — in-app vs browser URL dispatch
- **Live smoke tests** (`PublicBrowseLiveSmokeTest`) — requires network, validates real API responses

Pure JVM tests avoid `android.*` dependencies. Where Android APIs leaked into parsers (e.g., `TextUtils`), they were replaced with Kotlin equivalents.

## Revival Capability Model

Features are tiered by confidence level:

| Tier | Examples | Gating |
|------|----------|--------|
| **Stable** | Forum browse, thread read, search, hot topics | None |
| **Core account** | Login, notifications, manual sign | `SessionHealth` validation |
| **Experimental** | Reply, post, auto sign | Mandatory risk warning dialog |
| **Not promised** | Full posting restoration, SDK 35+ migration | Deferred |

`SessionHealth` is the single source of truth for session validity. Account-capability checks gate guarded features. See `docs/feature-status.md` for the full entry-point audit.

## Conventions

- Single `app` module (no multi-module). All source under `app/src/main/java/com/huanchengfly/tieba/post/`
- Compose compiler extension version pinned at 1.5.8 with stability config in `compose_stability_configuration.txt`
- ProGuard/R8 enabled for release builds; rules in `app/proguard-rules.pro`
- CI via GitHub Actions (`.github/workflows/build.yml`): builds release APK, uploads to Telegram + App Center
