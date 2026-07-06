# Thread Enhancement Panel Design

## Goal

Add a Zhihu++-style enhancement layer to the thread reading page: low-intrusion, local-first, user-controlled tools that make long threads easier to consume without changing posting, account, or network behavior.

The first version adds a thread enhancement panel with two practical tools:

- LZ timeline: list the loaded floors posted by the thread author and jump to them.
- In-thread search: search loaded post text in the current thread page and jump between matches.

## Non-Goals

- Do not add AI summary, cloud service calls, model downloads, or external APIs.
- Do not crawl every page of a thread in the background.
- Do not change the default thread layout or redesign the thread page.
- Do not modify posting, reply, login, or account synchronization behavior.
- Do not hide content automatically in the first version.

## UX

Add an entry point in the thread page top actions or overflow menu named "增强". Opening it shows a bottom sheet panel.

The panel contains:

1. LZ timeline
   - Shows loaded posts whose `author.id` matches `ThreadUiState.lz.id`.
   - Each row shows floor number and a short plain-text preview.
   - Tapping a row scrolls to that floor and closes the panel.
   - If no loaded LZ replies exist, show a quiet empty state instead of an error.

2. In-thread search
   - Provides a text input scoped to the posts already loaded in `ThreadUiState`.
   - Matches against `PostData.title` and `PostData.plainText`.
   - Shows match count and result rows with floor number and preview.
   - Tapping a result scrolls to that floor and closes the panel.
   - Empty query shows no results; no match shows an empty state.

The panel must be optional and non-blocking. If the user never opens it, the thread page should behave exactly as it does now.

## Architecture

Create a small pure model builder for the panel, separate from Compose rendering:

- `ThreadEnhancementPost`
  - `postId`
  - `floor`
  - `authorId`
  - `title`
  - `plainText`
  - `isLz`
  - `listKey`

- `ThreadEnhancementState`
  - `posts`
  - `lzPosts`
  - `search(query)`

The builder consumes `ThreadUiState` and mirrors the currently loaded thread content only:

- `firstPost`
- `data`
- `latestPosts`

It must deduplicate posts by `postId`, because `latestPosts` can overlap with loaded content.

Compose UI should live in a focused component such as `ThreadEnhancementPanel`. It should receive:

- the derived enhancement state
- current search query
- callbacks for query changes and post jump requests

## Jump Behavior

Jumping should use the same lazy-list keys/order as `ThreadContent`.

For normal loaded posts, the target key is the post id.
For the first floor, the target key is `Type.FirstPost.key`.
For latest-post sections, the target key is `"LatestPost_${post.id}"`.

Implementation should use a helper that maps an enhancement target to the current lazy-list item index. If a target is no longer present because the page reloaded, show a toast and do not crash.

## Error Handling

- Empty or loading thread state: disable the panel entry or show an empty panel.
- Query containing regex characters must be treated as literal text, not regex.
- Search should be case-insensitive for ASCII and direct substring matching for Chinese text.
- Blocked posts remain visible to the panel only as blocked floor placeholders if the existing thread list also shows them; the panel must not reveal hidden blocked content.

## Testing

Add unit tests for the pure model/search logic:

- LZ timeline includes only loaded author-matching posts.
- Search matches title and plain text.
- Search is literal and does not treat regex metacharacters specially.
- Duplicate posts are deduplicated by `postId`.
- Blocked posts do not leak their hidden content into search previews.

Manual verification should happen through GitHub Actions build output, not local Gradle compilation, unless explicitly allowed.

## Release Risk

Risk is low because the feature is opt-in and scoped to the thread page. The main risk is incorrect lazy-list jump indexing; keep that logic isolated and fail with a toast instead of throwing.
