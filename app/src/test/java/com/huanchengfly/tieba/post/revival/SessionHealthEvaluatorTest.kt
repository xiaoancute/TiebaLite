package com.huanchengfly.tieba.post.revival

import com.huanchengfly.tieba.post.models.database.Account
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionHealthEvaluatorTest {
    @Test
    fun `fromAccount marks missing identity fields as account incomplete`() {
        val health = SessionHealthEvaluator.fromAccount(
            Account(
                uid = "",
                name = "",
                bduss = "bduss",
                tbs = "tbs",
                portrait = "",
                sToken = "stoken",
                cookie = "BDUSS=bduss; STOKEN=stoken",
            )
        )

        assertEquals(SessionHealthStatus.AccountIncomplete, health.status)
        assertTrue(health.missingAccountFields.contains("uid"))
        assertTrue(health.missingAccountFields.contains("name"))
        assertTrue(health.missingAccountFields.contains("portrait"))
    }
}
