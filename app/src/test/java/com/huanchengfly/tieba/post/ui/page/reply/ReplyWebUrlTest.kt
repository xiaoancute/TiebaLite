package com.huanchengfly.tieba.post.ui.page.reply

import org.junit.Assert.assertEquals
import org.junit.Test

class ReplyWebUrlTest {
    @Test
    fun `buildTiebaWebReplyUrl opens thread when post id is missing`() {
        assertEquals(
            "https://tieba.baidu.com/p/123456",
            buildTiebaWebReplyUrl(threadId = 123456L, postId = null)
        )
    }

    @Test
    fun `buildTiebaWebReplyUrl anchors to post when post id is positive`() {
        assertEquals(
            "https://tieba.baidu.com/p/123456?pid=98765",
            buildTiebaWebReplyUrl(threadId = 123456L, postId = 98765L)
        )
    }

    @Test
    fun `buildTiebaWebReplyUrl ignores zero post id`() {
        assertEquals(
            "https://tieba.baidu.com/p/123456",
            buildTiebaWebReplyUrl(threadId = 123456L, postId = 0L)
        )
    }
}
