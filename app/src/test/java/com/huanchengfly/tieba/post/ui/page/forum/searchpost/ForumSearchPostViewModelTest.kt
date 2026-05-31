package com.huanchengfly.tieba.post.ui.page.forum.searchpost

import org.junit.Assert.assertEquals
import org.junit.Test

class ForumSearchPostViewModelTest {
    @Test
    fun `normalizeForumSearchKeyword trims valid keyword`() {
        assertEquals("原神", normalizeForumSearchKeyword("  原神  "))
    }

    @Test
    fun `normalizeForumSearchKeyword rejects punctuation only keyword`() {
        assertEquals("", normalizeForumSearchKeyword("  !!! ??? ...  "))
    }
}
