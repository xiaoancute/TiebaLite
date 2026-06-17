package com.huanchengfly.tieba.post.ui.common

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withAnnotation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PbContentTextTapTest {

    @Test
    fun `tap on url annotation opens url before default click`() {
        val text = buildAnnotatedString {
            append("before ")
            withAnnotation(PbContentRender.TAG_URL, "https://example.com") {
                append("link")
            }
        }

        assertEquals(
            PbContentTextTapTarget.Url("https://example.com"),
            resolvePbContentTextTapTarget(text, position = 8, hasDefaultClick = true)
        )
    }

    @Test
    fun `tap on user annotation opens user before default click`() {
        val text = buildAnnotatedString {
            append("hi ")
            withAnnotation(PbContentRender.TAG_USER, "12345") {
                append("@user")
            }
        }

        assertEquals(
            PbContentTextTapTarget.User(12345L),
            resolvePbContentTextTapTarget(text, position = 4, hasDefaultClick = true)
        )
    }

    @Test
    fun `tap on plain text uses default click when provided`() {
        assertEquals(
            PbContentTextTapTarget.Default,
            resolvePbContentTextTapTarget(
                text = AnnotatedString("plain text"),
                position = 3,
                hasDefaultClick = true
            )
        )
    }

    @Test
    fun `tap on plain text does nothing without default click`() {
        assertNull(
            resolvePbContentTextTapTarget(
                text = AnnotatedString("plain text"),
                position = 3,
                hasDefaultClick = false
            )
        )
    }
}
