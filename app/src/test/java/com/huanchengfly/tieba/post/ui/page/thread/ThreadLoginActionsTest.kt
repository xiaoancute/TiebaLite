package com.huanchengfly.tieba.post.ui.page.thread

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThreadLoginActionsTest {
    @Test
    fun loggedOutPostActionsOnlyKeepCopy() {
        val actions = resolveThreadPostActionAvailability(
            isLoggedIn = false,
            hideReply = false,
            hasCopyAction = true,
            hasFavoriteAction = true,
            canDelete = true,
        )

        assertFalse(actions.canTapCardToReply)
        assertFalse(actions.showReplyMenuItem)
        assertTrue(actions.showCopyMenuItem)
        assertFalse(actions.showReportMenuItem)
        assertFalse(actions.showFavoriteMenuItem)
        assertFalse(actions.showDeleteMenuItem)
    }

    @Test
    fun loggedInPostActionsExposeReplyReportFavoriteAndDelete() {
        val actions = resolveThreadPostActionAvailability(
            isLoggedIn = true,
            hideReply = false,
            hasCopyAction = true,
            hasFavoriteAction = true,
            canDelete = true,
        )

        assertTrue(actions.canTapCardToReply)
        assertTrue(actions.showReplyMenuItem)
        assertTrue(actions.showCopyMenuItem)
        assertTrue(actions.showReportMenuItem)
        assertTrue(actions.showFavoriteMenuItem)
        assertTrue(actions.showDeleteMenuItem)
    }

    @Test
    fun loggedOutThreadMenuHidesCollectReportAndDelete() {
        val actions = resolveThreadMenuActionAvailability(
            isLoggedIn = false,
            canDelete = true,
        )

        assertFalse(actions.showCollectToggle)
        assertFalse(actions.showReportItem)
        assertFalse(actions.showDeleteItem)
    }

    @Test
    fun loggedInSubPostPreviewKeepsReportWhenReplyPreferenceIsHidden() {
        val actions = resolveThreadSubPostActionAvailability(
            isLoggedIn = true,
            hideReply = true,
            hasCopyAction = true,
        )

        assertTrue(actions.showCopyMenuItem)
        assertFalse(actions.showReplyMenuItem)
        assertTrue(actions.showReportMenuItem)
    }
}
