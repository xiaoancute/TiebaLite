package com.huanchengfly.tieba.post.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test

class BilibiliUtilTest {
    @Test
    fun annotateVideoNumbersAddsExpectedUrls() {
        val annotated = BilibiliUtil.annotateVideoNumbers(
            AnnotatedString("看看 BV1Q541167Qg av12345 cv54321 au6789")
        )

        assertEquals(
            listOf(
                "https://www.bilibili.com/video/BV1Q541167Qg",
                "https://www.bilibili.com/video/av12345",
                "https://www.bilibili.com/read/cv54321",
                "https://www.bilibili.com/audio/au6789",
            ),
            annotated.getStringAnnotations(tag = "url", start = 0, end = annotated.length)
                .map { it.item }
        )
    }

    @Test
    fun annotateVideoNumbersSkipsExistingUrlRanges() {
        val source = buildAnnotatedString {
            pushStringAnnotation(
                tag = "url",
                annotation = "https://www.bilibili.com/video/BV1Q541167Qg"
            )
            append("https://www.bilibili.com/video/BV1Q541167Qg")
            pop()
        }

        val annotated = BilibiliUtil.annotateVideoNumbers(source)

        assertEquals(
            listOf("https://www.bilibili.com/video/BV1Q541167Qg"),
            annotated.getStringAnnotations(tag = "url", start = 0, end = annotated.length)
                .map { it.item }
        )
    }
}
