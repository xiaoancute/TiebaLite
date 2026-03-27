# TiebaLite App Language Switch Design

## Goal

Add an in-app language switch for TiebaLite with four options:

- Follow system
- Simplified Chinese
- Traditional Chinese
- English

The switch should live in the existing settings UI, apply immediately, and persist across app restarts.

## Context

The app already declares supported locales in [app/src/main/res/xml/locales_config.xml](/home/x/My_Dev/TiebaLite-4.0-dev/.worktrees/language-switch/app/src/main/res/xml/locales_config.xml), but there is no in-app entry to change them. Users currently have to rely on system app-language settings, which are not available or obvious on every Android version the app supports.

`CustomSettingsPage` already hosts appearance-related preferences such as dark mode and app icon, so language belongs there semantically and can reuse the existing `ListPref` pattern.

## User Experience

### Entry

Add a new list preference to `CustomSettingsPage` near the existing appearance preferences.

### Options

Store stable values internally:

- `system`
- `zh-Hans`
- `zh-Hant`
- `en`

Show user-facing labels:

- Follow system
- 简体中文
- 繁體中文
- English

### Behavior

- Selecting any option applies the language immediately.
- `Follow system` clears app-specific locale override and falls back to device language.
- The current selection is shown as the preference summary.
- The selected language is restored when the app starts again.

## Technical Design

### Preference Storage

Add a dedicated preference key in `AppPreferencesUtils`:

- Property name: `appLanguage`
- Key: `app_language`
- Default value: `system`

### Locale Application

Create a focused helper responsible for mapping stored values to `LocaleListCompat` and applying them through `AppCompatDelegate.setApplicationLocales(...)`.

Responsibilities:

- Convert `system` to `LocaleListCompat.getEmptyLocaleList()`
- Convert `zh-Hans`, `zh-Hant`, and `en` to `LocaleListCompat.forLanguageTags(...)`
- Expose a small API for:
  - applying a stored preference
  - applying a newly selected preference
  - returning the normalized current preference value for UI/tests if needed

### Startup Restore

Call the helper from `App.onCreate()` before most UI is shown so the chosen language is restored consistently on startup.

### Settings Integration

In `CustomSettingsPage`:

- Add a `ListPref` backed by `app_language`
- Use `useSelectedAsSummary = true`
- Hook `onValueChange` to the locale helper so the app language updates immediately
- Keep the implementation local and consistent with existing settings patterns

## Testing Strategy

### JVM Tests

Add helper-level tests that verify:

- `system` maps to an empty locale list
- `zh-Hans` maps to simplified Chinese
- `zh-Hant` maps to traditional Chinese
- `en` maps to English
- unknown or blank values normalize safely to `system`

### UI/Settings Regression

Add a lightweight settings test that validates:

- the four language entries are exposed in the settings layer
- the default stored value is `system`

### Verification

Run:

- focused JVM tests for the new locale helper and related settings coverage
- `assembleDebug`

## Constraints

- Do not add a separate language page
- Do not require app restart
- Do not change the supported locale list in `locales_config.xml`
- Do not change unrelated settings structure outside the minimal insertion needed for this preference

## Risks And Mitigations

- Some UI may still depend on cached `Context` objects:
  apply via `AppCompatDelegate` and restore at app startup to stay within AndroidX-supported behavior.

- Locale tags can drift between storage and API usage:
  centralize all mapping logic in one helper instead of scattering string checks across the app.

## Implementation Boundary

This change covers only app language selection and persistence. It does not include translation content changes, extra locales, region-specific formatting settings, or any migration of existing string resources.
