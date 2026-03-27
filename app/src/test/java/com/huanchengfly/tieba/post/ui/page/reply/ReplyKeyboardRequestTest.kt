package com.huanchengfly.tieba.post.ui.page.reply

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReplyKeyboardRequestTest {
    @Test
    fun requestReplyKeyboardDefersFocusAndKeyboardUntilPostedTaskRuns() {
        val events = mutableListOf<String>()
        var postedTask: (() -> Unit)? = null

        requestReplyKeyboard(
            postToEditor = { task -> postedTask = task },
            requestFocus = { events += "focus" },
            showKeyboard = { events += "keyboard" }
        )

        assertTrue(events.isEmpty())
        assertNotNull(postedTask)

        postedTask!!.invoke()

        assertEquals(listOf("focus", "keyboard"), events)
    }

    @Test
    fun requestReplyKeyboardDoesNothingWithoutEditor() {
        var invoked = false

        requestReplyKeyboard(
            postToEditor = null,
            requestFocus = { invoked = true },
            showKeyboard = { invoked = true }
        )

        assertFalse(invoked)
    }
}
