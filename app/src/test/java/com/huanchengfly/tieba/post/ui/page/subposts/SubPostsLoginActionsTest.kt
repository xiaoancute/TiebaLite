package com.huanchengfly.tieba.post.ui.page.subposts

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubPostsLoginActionsTest {
    @Test
    fun loggedOutSubPostActionsKeepCopyButDisableReplyAndReport() {
        val actions = resolveSubPostsItemActionAvailability(
            isLoggedIn = false,
            hideReply = false,
            hasCopyAction = true,
            canDelete = true,
        )

        assertFalse(actions.canTapItemToReply)
        assertTrue(actions.showCopyMenuItem)
        assertFalse(actions.showReplyMenuItem)
        assertFalse(actions.showReportMenuItem)
        assertFalse(actions.showDeleteMenuItem)
    }

    @Test
    fun loggedInSubPostActionsKeepReportWhenReplyPreferenceIsHidden() {
        val actions = resolveSubPostsItemActionAvailability(
            isLoggedIn = true,
            hideReply = true,
            hasCopyAction = true,
            canDelete = true,
        )

        assertFalse(actions.canTapItemToReply)
        assertTrue(actions.showCopyMenuItem)
        assertFalse(actions.showReplyMenuItem)
        assertTrue(actions.showReportMenuItem)
        assertTrue(actions.showDeleteMenuItem)
    }
}
