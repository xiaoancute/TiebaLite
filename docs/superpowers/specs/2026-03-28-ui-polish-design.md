# TiebaLite UI Polish Design

## Scope

This design covers a small Android UI polish pass based on emulator QA findings from March 28, 2026. The goal is to improve phone-width usability without changing feature logic or introducing new UI frameworks.

## Problems

1. The last item on the Personalization page can sit inside the bottom gesture area on first load.
2. The Home unauthenticated empty state feels vertically loose on phone screens.
3. The top informational summaries on Settings read as a dense text wall on phone width.

## Chosen Approach

### Preference screen safe area

Update the shared `PrefsScreen` component to apply bottom list content padding, merged with navigation bar insets. This fixes the clipped last row at the component level so existing preference pages benefit without page-specific hacks.

### Home empty state tightening

Keep the current empty-state structure, but reduce the visual weight of the illustration on compact screens so the title, message, and actions read as one cluster instead of feeling split by excess empty space.

### Settings readability

Keep the current info blocks and behavior, but shorten the summary copy in all supported locales so the page remains informative without turning the first screen into a dense announcement wall.

## Non-Goals

- No navigation or feature changes.
- No new Compose UI test harness.
- No broad redesign of settings or home page layouts.
- No dependency upgrades or framework migrations.

## Validation

- `assembleDebug`
- Emulator smoke verification on:
  - Home logged-out state
  - Settings top summary area
  - Personalization page last item visibility

## Notes

- This design was already approved inline in the session before implementation.
- The repo currently has no established Compose UI test setup, so verification stays with build + emulator regression.
