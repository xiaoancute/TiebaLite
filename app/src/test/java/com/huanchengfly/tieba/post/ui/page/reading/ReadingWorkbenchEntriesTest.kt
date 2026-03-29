package com.huanchengfly.tieba.post.ui.page.reading

import com.huanchengfly.tieba.post.models.ReadingTargetType
import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingWorkbenchEntriesTest {
    @Test
    fun `build saved reading entries sorts newest first`() {
        val result = buildSavedReadingEntries(
            listOf(
                SavedReadingEntryCandidate(
                    key = "forum-1",
                    type = ReadingTargetType.FORUM,
                    title = "数码吧",
                    subtitle = null,
                    timestamp = 100L,
                ),
                SavedReadingEntryCandidate(
                    key = "thread-1",
                    type = ReadingTargetType.THREAD,
                    title = "今天有什么新机",
                    subtitle = "数码吧",
                    timestamp = 300L,
                ),
                SavedReadingEntryCandidate(
                    key = "thread-2",
                    type = ReadingTargetType.THREAD,
                    title = "今晚发布会",
                    subtitle = "数码吧",
                    timestamp = 200L,
                ),
            )
        )

        assertEquals(listOf("thread-1", "thread-2", "forum-1"), result.map { it.key })
    }

    @Test
    fun `build saved reading entries drops blank titles and unsupported types`() {
        val result = buildSavedReadingEntries(
            listOf(
                SavedReadingEntryCandidate(
                    key = "thread-1",
                    type = ReadingTargetType.THREAD,
                    title = "   ",
                    subtitle = "数码吧",
                    timestamp = 300L,
                ),
                SavedReadingEntryCandidate(
                    key = "forum-1",
                    type = ReadingTargetType.FORUM,
                    title = "动漫吧",
                    subtitle = null,
                    timestamp = 200L,
                ),
                SavedReadingEntryCandidate(
                    key = "unknown-1",
                    type = 99,
                    title = "无效项",
                    subtitle = null,
                    timestamp = 100L,
                ),
            )
        )

        assertEquals(listOf("forum-1"), result.map { it.key })
    }
}
