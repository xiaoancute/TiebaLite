package com.huanchengfly.tieba.post.ui.page.thread

import org.junit.Assert.assertEquals
import org.junit.Test

class PollVoteUtilsTest {
    @Test
    fun `formatPollOptionIds sorts and filters option ids`() {
        assertEquals("0,2,8", formatPollOptionIds(setOf(8, -1, 2, 0)))
    }

    @Test
    fun `parsePollOptionIds ignores malformed values`() {
        assertEquals(setOf(0, 2, 8), parsePollOptionIds(" 2, bad, 8, 0 "))
    }
}
