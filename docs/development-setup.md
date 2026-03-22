# Android Development Setup

This project is an Android app module and cannot build with only `platform-tools`.

## Required Local SDK Components

At minimum, the current Gradle config expects:

- Java 17
- Android SDK Platform 34
- Android SDK Build-Tools 34.0.0
- Android platform-tools
- Accepted Android SDK licenses

The app currently declares these requirements in:

- [app/build.gradle.kts](/home/x/My_Dev/TiebaLite-4.0-dev/app/build.gradle.kts#L37)

## Current P0 Finding

On 2026-03-21, the local machine at `/usr/lib/android-sdk` only contained `platform-tools`.

Missing items observed:

- `platforms/android-34`
- `build-tools/34.0.0`
- `licenses/`

Because of that, Gradle fails before Java/Kotlin compilation begins.

## Observed Build Failure

`./gradlew :app:compileDebugJavaWithJavac --stacktrace`

Key failure:

```text
Failed to install the following Android SDK packages as some licences have not been accepted.
platforms;android-34 Android SDK Platform 34
build-tools;34.0.0 Android SDK Build-Tools 34
```

## Observed Sandbox Limitation

In the current restricted execution environment, Gradle may also fail before task execution with:

```text
Could not determine a usable wildcard IP for this machine.
Caused by: java.net.SocketException: Operation not permitted (Socket creation failed)
```

This is an environment restriction around socket creation and network-interface inspection, not a confirmed project-source failure.

## Quick Checklist

1. Install Java 17.
2. Install Android SDK Platform 34.
3. Install Android Build-Tools 34.0.0.
4. Accept Android SDK licenses.
5. Export `ANDROID_HOME` and `ANDROID_SDK_ROOT`.
6. Run `scripts/check-android-env.sh`.
7. Run `./gradlew assembleDebug`.

## Debian/Ubuntu Package Hint

If you manage Android SDK components through distro packages instead of Android Studio, this host currently needs:

```text
google-android-licenses
google-android-cmdline-tools-13.0-installer
google-android-platform-34-installer
google-android-build-tools-34.0.0-installer
```

Typical install command:

```bash
sudo apt-get update
sudo apt-get install -y \
  google-android-licenses \
  google-android-cmdline-tools-13.0-installer \
  google-android-platform-34-installer \
  google-android-build-tools-34.0.0-installer
```

## Troubleshooting Notes

- If Gradle fails before compilation, verify SDK contents first.
- If Gradle fails with wildcard IP or socket-creation errors, retry outside the restricted sandbox or on a normal local machine.
- If the SDK is present but Gradle still fails, inspect AGP/Kotlin/KSP/Wire version compatibility next.
- If login-related flows fail at runtime, inspect WebView cookie capture in:
  - [app/src/main/java/com/huanchengfly/tieba/post/ui/page/login/LoginPage.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/ui/page/login/LoginPage.kt#L72)
  - [app/src/main/java/com/huanchengfly/tieba/post/ui/page/login/LoginPage.kt](/home/x/My_Dev/TiebaLite-4.0-dev/app/src/main/java/com/huanchengfly/tieba/post/ui/page/login/LoginPage.kt#L255)

## Next P0 Actions

- Re-run the build after SDK installation.
- Record the first source-level failure, if any.
- Add more unit tests around pure parsing and repository helpers.
