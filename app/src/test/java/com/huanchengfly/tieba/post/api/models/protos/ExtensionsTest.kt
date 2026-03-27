package com.huanchengfly.tieba.post.api.models.protos

import com.huanchengfly.tieba.post.ui.common.TextContentRender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtensionsTest {
    @Test
    fun threadAbstractTextKeepsType40Text() {
        val thread = ThreadInfo(
            richAbstract = listOf(
                PbContent(type = 40, text = "关键词链接", link = "https://example.com")
            )
        )

        assertEquals("关键词链接", thread.abstractText)
    }

    @Test
    fun pbContentPlainTextKeepsType35Text() {
        assertEquals(
            "广告文案",
            listOf(PbContent(type = 35, text = "广告文案")).plainText
        )
    }

    @Test
    fun pbContentType40RendersAsPlainText() {
        val render = listOf(
            PbContent(type = 40, text = "关键词链接", link = "https://example.com")
        ).renders.single() as TextContentRender

        assertEquals("关键词链接", render.toString())
        val annotationString = render.toAnnotationString()
        assertTrue(
            annotationString.getStringAnnotations(
                tag = "url",
                start = 0,
                end = annotationString.length
            ).isEmpty()
        )
    }
}
