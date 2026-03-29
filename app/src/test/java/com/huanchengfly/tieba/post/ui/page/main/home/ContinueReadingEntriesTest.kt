package com.huanchengfly.tieba.post.ui.page.main.home

import org.junit.Assert.assertEquals
import org.junit.Test

class ContinueReadingEntriesTest {
    @Test
    fun `buildContinueReadingEntries sorts by latest timestamp first`() {
        val result = buildContinueReadingEntries(
            listOf(
                ContinueReadingEntryCandidate(
                    threadId = 1L,
                    title = "older",
                    postId = 100L,
                    timestamp = 10L,
                ),
                ContinueReadingEntryCandidate(
                    threadId = 2L,
                    title = "newer",
                    postId = 200L,
                    timestamp = 20L,
                ),
            ),
            limit = 10,
        )

        assertEquals(listOf(2L, 1L), result.map { it.threadId })
    }

    @Test
    fun `buildContinueReadingEntries drops entries without usable progress`() {
        val result = buildContinueReadingEntries(
            listOf(
                ContinueReadingEntryCandidate(
                    threadId = 1L,
                    title = "valid",
                    postId = 100L,
                    timestamp = 10L,
                ),
                ContinueReadingEntryCandidate(
                    threadId = 2L,
                    title = "missing post id",
                    postId = 0L,
                    timestamp = 30L,
                ),
                ContinueReadingEntryCandidate(
                    threadId = 3L,
                    title = "",
                    postId = 300L,
                    timestamp = 40L,
                ),
            ),
            limit = 10,
        )

        assertEquals(listOf(1L), result.map { it.threadId })
    }

    @Test
    fun `buildContinueReadingEntries respects the requested limit`() {
        val result = buildContinueReadingEntries(
            listOf(
                ContinueReadingEntryCandidate(threadId = 1L, title = "1", postId = 1L, timestamp = 10L),
                ContinueReadingEntryCandidate(threadId = 2L, title = "2", postId = 2L, timestamp = 20L),
                ContinueReadingEntryCandidate(threadId = 3L, title = "3", postId = 3L, timestamp = 30L),
            ),
            limit = 2,
        )

        assertEquals(listOf(3L, 2L), result.map { it.threadId })
    }
}
