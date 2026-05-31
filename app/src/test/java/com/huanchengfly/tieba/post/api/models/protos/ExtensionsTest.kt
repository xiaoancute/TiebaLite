package com.huanchengfly.tieba.post.api.models.protos

import org.junit.Assert.assertEquals
import org.junit.Test

class ExtensionsTest {

    @Test
    fun `thread abstract keeps link text`() {
        val threadInfo = ThreadInfo(
            richAbstract = listOf(
                PbContent(type = 0, text = "正文  文本"),
                PbContent(type = 40, text = "链接  文本")
            )
        )

        assertEquals("正文 文本链接 文本", threadInfo.abstractText)
    }

    @Test
    fun `post list abstract keeps link text`() {
        val postInfo = PostInfoList(
            rich_abstract = listOf(
                PbContent(type = 0, text = "楼主  内容"),
                PbContent(type = 40, text = "跳转  链接")
            )
        )

        assertEquals("楼主 内容跳转 链接", postInfo.abstractText)
    }
}
