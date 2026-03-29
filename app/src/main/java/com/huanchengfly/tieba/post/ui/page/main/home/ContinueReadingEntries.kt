package com.huanchengfly.tieba.post.ui.page.main.home

data class ContinueReadingEntryCandidate(
    val threadId: Long,
    val title: String,
    val postId: Long,
    val timestamp: Long,
)

fun buildContinueReadingEntries(
    entries: List<ContinueReadingEntryCandidate>,
    limit: Int,
): List<ContinueReadingEntryCandidate> {
    return entries
        .asSequence()
        .filter { it.postId != 0L && it.title.isNotBlank() }
        .sortedByDescending { it.timestamp }
        .take(limit)
        .toList()
}
