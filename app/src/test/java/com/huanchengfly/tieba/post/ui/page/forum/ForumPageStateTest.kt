package com.huanchengfly.tieba.post.ui.page.forum

import com.huanchengfly.tieba.post.ui.models.forum.NavTab
import org.junit.Assert.assertEquals
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

    @Test
    fun `tab bar stays empty while forum tabs are still loading`() {
        assertEquals(emptyList<NavTab>(), forumTabBarNavTabs(null))
    }

    @Test
    fun `tab bar falls back only after loaded tabs are empty`() {
        assertEquals(listOf(NavTab.Fallback), forumTabBarNavTabs(emptyList()))
    }
}
