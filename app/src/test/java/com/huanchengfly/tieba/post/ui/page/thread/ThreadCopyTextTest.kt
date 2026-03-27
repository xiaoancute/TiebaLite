package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.post.api.models.protos.PbContent
import com.huanchengfly.tieba.post.api.models.protos.Post
import org.junit.Assert.assertEquals
import org.junit.Test

class ThreadCopyTextTest {
    @Test
    fun firstFloorCopyTextIncludesVisibleTitle() {
        val post = Post(
            floor = 1,
            title = "测试标题",
            content = listOf(PbContent(type = 0, text = "正文内容"))
        )

        assertEquals("测试标题\n正文内容", buildPostCopyText(post))
    }

    @Test
    fun replyCopyTextDoesNotIncludeTitle() {
        val post = Post(
            floor = 2,
            title = "测试标题",
            content = listOf(PbContent(type = 0, text = "回复内容"))
        )

        assertEquals("回复内容", buildPostCopyText(post))
    }

    @Test
    fun hiddenTitleDoesNotLeakIntoCopyText() {
        val post = Post(
            floor = 1,
            title = "隐藏标题",
            is_ntitle = 1,
            content = listOf(PbContent(type = 0, text = "正文内容"))
        )

        assertEquals("正文内容", buildPostCopyText(post))
    }
}
