package com.huanchengfly.tieba.post.ui.page.reading

import com.huanchengfly.tieba.post.models.ReadingTargetType

data class SavedReadingEntryCandidate(
    val key: String,
    val type: Int,
    val title: String,
    val subtitle: String?,
    val timestamp: Long,
)

fun buildSavedReadingEntries(
    entries: List<SavedReadingEntryCandidate>,
): List<SavedReadingEntryCandidate> {
    return entries
        .asSequence()
        .filter { it.title.isNotBlank() }
        .filter { it.type == ReadingTargetType.FORUM || it.type == ReadingTargetType.THREAD }
        .sortedByDescending { it.timestamp }
        .toList()
}
