package com.huanchengfly.tieba.post.utils

import com.huanchengfly.tieba.post.api.models.MessageListBean
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationFieldResolverTest {
    private val agreeItem = MessageListBean.MessageInfoBean(postId = "agree")
    private val atItem = MessageListBean.MessageInfoBean(postId = "at")
    private val replyItem = MessageListBean.MessageInfoBean(postId = "reply")

    @Test
    fun `prefer agree list when primary field exists`() {
        val selection = NotificationFieldResolver.selectAgreeList(
            primaryAgreeList = listOf(agreeItem),
            legacyAtList = listOf(atItem),
            legacyReplyList = listOf(replyItem)
        )

        assertEquals(listOf(agreeItem), selection.data)
        assertEquals(NotificationFieldSource.PrimaryAgree, selection.source)
        assertFalse(selection.usedCompatibilityFallback)
    }

    @Test
    fun `do not fall back when agree list is present but empty`() {
        val selection = NotificationFieldResolver.selectAgreeList(
            primaryAgreeList = emptyList(),
            legacyAtList = listOf(atItem),
            legacyReplyList = listOf(replyItem)
        )

        assertTrue(selection.data.isEmpty())
        assertEquals(NotificationFieldSource.PrimaryAgree, selection.source)
        assertFalse(selection.usedCompatibilityFallback)
    }

    @Test
    fun `fall back to at list only when primary list is missing`() {
        val selection = NotificationFieldResolver.selectAgreeList(
            primaryAgreeList = null,
            legacyAtList = listOf(atItem),
            legacyReplyList = listOf(replyItem)
        )

        assertEquals(listOf(atItem), selection.data)
        assertEquals(NotificationFieldSource.LegacyAtList, selection.source)
        assertTrue(selection.usedCompatibilityFallback)
    }

    @Test
    fun `do not fall back to fans when agree count is zero`() {
        val selection = NotificationFieldResolver.selectAgreeUnread(
            primaryAgreeCount = "0",
            legacyFansCount = "6"
        )

        assertEquals(0, selection.count)
        assertEquals(NotificationFieldSource.PrimaryAgree, selection.source)
        assertFalse(selection.usedCompatibilityFallback)
    }

    @Test
    fun `fall back to fans only when agree count is missing`() {
        val selection = NotificationFieldResolver.selectAgreeUnread(
            primaryAgreeCount = null,
            legacyFansCount = "6"
        )

        assertEquals(6, selection.count)
        assertEquals(NotificationFieldSource.LegacyFans, selection.source)
        assertTrue(selection.usedCompatibilityFallback)
    }
}
