package com.huanchengfly.tieba.post.ui.page.forum

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForumPageStateTest {

    @Test
    fun `initial tab is applied after real forum tabs load`() {
        assertTrue(
            shouldApplyInitialForumTab(
                initialTabPositioned = false,
                navTabsLoaded = true,
                currentPage = 0,
                initialPage = 1
            )
        )
    }

    @Test
    fun `initial tab is not reapplied after returning from a thread`() {
        assertFalse(
            shouldApplyInitialForumTab(
                initialTabPositioned = true,
                navTabsLoaded = true,
                currentPage = 2,
                initialPage = 1
            )
        )
    }

    @Test
    fun `initial tab waits for real forum tabs`() {
        assertFalse(
            shouldApplyInitialForumTab(
                initialTabPositioned = false,
                navTabsLoaded = false,
                currentPage = 0,
                initialPage = 1
            )
        )
    }

    @Test
    fun `initial tab is not applied when pager already points at it`() {
        assertFalse(
            shouldApplyInitialForumTab(
                initialTabPositioned = false,
                navTabsLoaded = true,
                currentPage = 1,
                initialPage = 1
            )
        )
    }
}
