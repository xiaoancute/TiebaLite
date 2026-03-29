package com.huanchengfly.tieba.post.ui.page.thread

import org.junit.Assert.assertEquals
import org.junit.Test

class ThreadResumeStateResolverTest {
    @Test
    fun `explicit thread launch target wins over saved progress`() {
        val resolved = resolveThreadResumeState(
            initialPostId = 123L,
            initialSeeLz = false,
            rememberProgress = true,
            savedProgress = SavedThreadProgress(
                postId = 456L,
                seeLz = true,
            ),
        )

        assertEquals(
            ThreadResumeState(
                postId = 123L,
                seeLz = false,
                restoredFromProgress = false,
            ),
            resolved,
        )
    }

    @Test
    fun `saved progress is restored when launch target is empty`() {
        val resolved = resolveThreadResumeState(
            initialPostId = 0L,
            initialSeeLz = false,
            rememberProgress = true,
            savedProgress = SavedThreadProgress(
                postId = 456L,
                seeLz = true,
            ),
        )

        assertEquals(
            ThreadResumeState(
                postId = 456L,
                seeLz = true,
                restoredFromProgress = true,
            ),
            resolved,
        )
    }

    @Test
    fun `saved progress is ignored when remember progress is disabled`() {
        val resolved = resolveThreadResumeState(
            initialPostId = 0L,
            initialSeeLz = false,
            rememberProgress = false,
            savedProgress = SavedThreadProgress(
                postId = 456L,
                seeLz = true,
            ),
        )

        assertEquals(
            ThreadResumeState(
                postId = 0L,
                seeLz = false,
                restoredFromProgress = false,
            ),
            resolved,
        )
    }

    @Test
    fun `empty saved progress keeps original launch state`() {
        val resolved = resolveThreadResumeState(
            initialPostId = 0L,
            initialSeeLz = true,
            rememberProgress = true,
            savedProgress = null,
        )

        assertEquals(
            ThreadResumeState(
                postId = 0L,
                seeLz = true,
                restoredFromProgress = false,
            ),
            resolved,
        )
    }
}
