package com.huanchengfly.tieba.post.utils

import com.huanchengfly.tieba.post.workers.OKSignWork
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OKSignNotificationPolicyTest {
    @Test
    fun progressNotificationRemainsFixedAndOngoing() {
        val spec = buildOKSignNotificationSpec(OKSignNotificationKind.Progress)

        assertEquals(OKSignWork.NOTIFICATION_ID, spec.notificationId)
        assertTrue(spec.ongoing)
        assertFalse(spec.autoCancel)
    }

    @Test
    fun failureNotificationsUseUniqueDismissibleIds() {
        val completion = buildOKSignNotificationSpec(OKSignNotificationKind.Completion)
        val failure = buildOKSignNotificationSpec(
            kind = OKSignNotificationKind.Failure,
            uniqueSeed = 42L,
        )

        assertEquals(OKSignWork.NOTIFICATION_ID, completion.notificationId)
        assertEquals(42, failure.notificationId)
        assertNotEquals(completion.notificationId, failure.notificationId)
        assertFalse(failure.ongoing)
        assertTrue(failure.autoCancel)
    }
}
