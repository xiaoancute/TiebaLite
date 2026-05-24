# Modern Android System Integration Design

Date: 2026-05-24

## Goal

Make TiebaLite feel closer to current Android system behavior without changing its visible UI design.

This spec intentionally excludes layout, color, spacing, card, toolbar, tab, bottom navigation, and edge-to-edge visual redesign work. The scope is system integration and navigation behavior only.

## Current State

- The project already targets modern Android reasonably well:
  - `compileSdk = 36`
  - `targetSdk = 35`
  - Material 3 dependencies
  - Android 12 splash screen
  - dynamic themed launcher icons
  - app shortcuts
  - notification permission handling
  - a one-click sign-in quick settings tile
- The application enables predictive back at the application level, but `MainActivityV2` explicitly sets `android:enableOnBackInvokedCallback="false"`.
- The app already accepts:
  - `tblite://...`
  - `com.baidu.tieba://unidispatch`
  - `http(s)://tieba.baidu.com/...`
- Compose navigation already has typed destinations for search, forum, thread, notifications, history, and favorites.

## Non-Goals

- Do not implement edge-to-edge visual changes.
- Do not redesign cards, top bars, tab bars, bottom navigation, dialogs, or color themes.
- Do not introduce new landing pages or explanatory UI.
- Do not change the app name, icon, signing, release process, or versioning as part of this work.
- Do not run local Gradle builds; verification should use GitHub Actions when build/test evidence is needed.

## Proposed Scope

### 1. Predictive Back Compatibility

Enable modern Android predictive back behavior for `MainActivityV2` while preserving existing in-app back semantics.

Implementation intent:

- Remove or replace the activity-level opt-out that disables predictive back for `MainActivityV2`.
- Audit custom back handlers in core flows:
  - main page
  - forum page
  - thread page
  - photo viewer
  - WebView page
  - reply bottom sheet
  - modal/bottom-sheet navigation
- Keep existing business behavior unchanged:
  - Back from thread returns to the previous list.
  - Back from dialog/sheet closes the overlay first.
  - Back from root exits the app.
  - WebView back navigates browser history before leaving the page.

Expected user-visible result:

- On Android versions that support predictive back, system back gestures should preview navigation consistently.
- On older Android versions, behavior should remain unchanged.

### 2. Deep Link Routing Improvements

Improve system/open-link entry points without adding new UI.

Supported inputs:

- `tblite://search?keyword=<text>` opens the search page with the keyword prefilled/submitted according to existing search behavior.
- `https://tieba.baidu.com/p/<threadId>` opens the thread page.
- `http://tieba.baidu.com/p/<threadId>` opens the thread page.
- `https://tieba.baidu.com/f?kw=<forumName>` opens the forum page.
- `http://tieba.baidu.com/f?kw=<forumName>` opens the forum page.
- Existing `tblite://search`, `tblite://forum`, `tblite://history`, `tblite://notifications`, and `tblite://favorite` links continue to work.

Parsing rules:

- Prefer structured `Uri` parsing over string slicing.
- Decode query parameters using platform URI APIs.
- Ignore unsupported or malformed URLs by falling back to existing app launch behavior instead of crashing.
- Do not navigate to a destination unless the minimum required fields are present and valid.

Expected user-visible result:

- Opening common Tieba links from Android system surfaces lands in the relevant TiebaLite screen instead of a generic entry point.
- Malformed links do not crash the app.

### 3. System Share Ingress

Allow Android Sharesheet text/link input to enter TiebaLite and route using the same parser as deep links.

Supported inputs:

- Shared plain text containing a Tieba thread URL.
- Shared plain text containing a Tieba forum URL.
- Shared plain text with no recognized Tieba URL opens search with the shared text as the keyword.

Implementation intent:

- Add `ACTION_SEND` handling for `text/plain` to `MainActivityV2`.
- Extract `Intent.EXTRA_TEXT`.
- Reuse the deep link parser so share handling and link handling do not diverge.
- Keep failure behavior quiet and predictable: if the text cannot be parsed as a Tieba URL, use it as a search keyword.

Expected user-visible result:

- Sharing a Tieba link to TiebaLite opens the relevant content.
- Sharing normal text to TiebaLite opens search.

## Architecture

Add a small routing/parser layer near navigation code, for example:

- `SystemIntentRoute`
  - sealed result describing route targets: search, forum, thread, notifications, favorite, history, none
- `SystemIntentRouter`
  - converts `Intent` or `Uri` into `SystemIntentRoute`
  - contains no Compose code
  - has unit tests

Navigation code should consume `SystemIntentRoute` and convert it into existing `Destination` instances.

This keeps parsing testable and prevents route parsing logic from spreading across `MainActivityV2`, `RootNavGraph`, and individual pages.

## Error Handling

- Invalid thread IDs, missing forum names, empty search keywords, and unknown schemes should not throw.
- Unsupported URLs should return `SystemIntentRoute.None`.
- `ACTION_SEND` text with no recognized URL should become a search route if non-blank.
- Parser exceptions should be avoided by using safe conversions such as `toLongOrNull()`.

## Testing

Add JVM unit tests for parser behavior:

- `tblite://search?keyword=abc`
- `https://tieba.baidu.com/p/123456`
- `https://tieba.baidu.com/f?kw=android`
- malformed thread IDs
- unsupported hosts
- plain shared text
- shared text containing a Tieba URL among surrounding text

Manual/GitHub Actions verification:

- Push branch and use GitHub Actions for unit tests/build.
- If an APK is needed, install the GitHub Actions artifact on emulator.
- Validate adb intent examples:
  - `am start -a android.intent.action.VIEW -d "https://tieba.baidu.com/p/123456"`
  - `am start -a android.intent.action.VIEW -d "https://tieba.baidu.com/f?kw=android"`
  - `am start -a android.intent.action.VIEW -d "tblite://search?keyword=android"`
  - `am start -a android.intent.action.SEND -t text/plain --es android.intent.extra.TEXT "https://tieba.baidu.com/p/123456"`

## Release Notes

Suggested release note:

> Improved Android system integration: better predictive back compatibility, direct opening of common Tieba links, and support for sharing text or Tieba links into TiebaLite.

