package com.huanchengfly.tieba.post.repository

import androidx.core.util.Predicate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun isBlocked_blocksWaterPostWhenEnabled() {
        assertTrue(
            BlockRepository.isBlocked(
                emptyList(),
                emptyList(),
                true,
                "+3"
            )
        )
    }

    @Test
    fun isBlocked_doesNotBlockWaterPostWhenDisabled() {
        assertFalse(
            BlockRepository.isBlocked(
                emptyList(),
                emptyList(),
                false,
                "+3"
            )
        )
    }

    @Test
    fun isBlocked_whitelistOverridesBuiltInWaterPostRule() {
        assertFalse(
            BlockRepository.isBlocked(
                emptyList(),
                listOf(Predicate { it == "+3" }),
                true,
                "+3"
            )
        )
    }
}
