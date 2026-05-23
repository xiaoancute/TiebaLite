package com.huanchengfly.tieba.post.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class BlockRepositoryTest {
    @Test
    fun normalizeForumName_trimsSuffixBa() {
        assertEquals("显卡", BlockRepository.normalizeForumName(" 显卡吧 "))
    }

    @Test
    fun normalizeForumName_keepsSingleBaInvalidForCaller() {
        assertEquals("", BlockRepository.normalizeForumName("吧"))
    }
}
