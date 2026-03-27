package com.huanchengfly.tieba.post.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClipBoardLinkDetectorTest {
    @Test
    fun ignoresForumLinksWithEmptyQueryValues() {
        assertNull(parseTiebaClipboardLink("https://tieba.baidu.com/f?kw="))
        assertNull(parseTiebaClipboardLink("https://tieba.baidu.com/f?word="))
        assertNull(parseTiebaClipboardLink("https://tieba.baidu.com/mo/q/m?kz="))
    }

    @Test
    fun keepsValidForumAndThreadLinksResolvable() {
        val forumLink = parseTiebaClipboardLink("https://tieba.baidu.com/f?kw=%E5%8E%9F%E7%A5%9E")
        val threadLink = parseTiebaClipboardLink("https://tieba.baidu.com/mo/q/m?kz=10547704116")

        assertEquals("原神", (forumLink as ClipBoardForumLink).forumName)
        assertEquals("10547704116", (threadLink as ClipBoardThreadLink).threadId)
    }
}
