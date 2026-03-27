package com.huanchengfly.tieba.post.ui.common

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test

class TextContentRenderTest {
    @Test
    fun annotationStringAddsBilibiliJumpLinks() {
        val annotated = TextContentRender(AnnotatedString("BV1Q541167Qg")).toAnnotationString()

        assertEquals(
            listOf("https://www.bilibili.com/video/BV1Q541167Qg"),
            annotated.getStringAnnotations(tag = "url", start = 0, end = annotated.length)
                .map { it.item }
        )
    }
}
