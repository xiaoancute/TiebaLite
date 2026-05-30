package com.huanchengfly.tieba.post.ui.models.settings

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoClearImageCacheIntervalTest {

    @Test
    fun `off never clears`() {
        assertFalse(
            AutoClearImageCacheInterval.shouldClear(
                interval = AutoClearImageCacheInterval.OFF,
                lastClearTime = 0L,
                now = 10_000L
            )
        )
    }

    @Test
    fun `on launch clears when it has not run before`() {
        assertTrue(
            AutoClearImageCacheInterval.shouldClear(
                interval = AutoClearImageCacheInterval.ON_LAUNCH,
                lastClearTime = 0L,
                now = 10_000L
            )
        )
    }

    @Test
    fun `on launch ignores previous process clear time`() {
        assertTrue(
            AutoClearImageCacheInterval.shouldClear(
                interval = AutoClearImageCacheInterval.ON_LAUNCH,
                lastClearTime = 9_000L,
                now = 10_000L
            )
        )
    }

    @Test
    fun `daily clears after one day`() {
        assertFalse(
            AutoClearImageCacheInterval.shouldClear(
                interval = AutoClearImageCacheInterval.DAILY,
                lastClearTime = 1_000L,
                now = 1_000L + AutoClearImageCacheInterval.ONE_DAY_MILLIS - 1L
            )
        )

        assertTrue(
            AutoClearImageCacheInterval.shouldClear(
                interval = AutoClearImageCacheInterval.DAILY,
                lastClearTime = 1_000L,
                now = 1_000L + AutoClearImageCacheInterval.ONE_DAY_MILLIS
            )
        )
    }

    @Test
    fun `three days clears after three days`() {
        assertFalse(
            AutoClearImageCacheInterval.shouldClear(
                interval = AutoClearImageCacheInterval.THREE_DAYS,
                lastClearTime = 1_000L,
                now = 1_000L + AutoClearImageCacheInterval.THREE_DAYS_MILLIS - 1L
            )
        )

        assertTrue(
            AutoClearImageCacheInterval.shouldClear(
                interval = AutoClearImageCacheInterval.THREE_DAYS,
                lastClearTime = 1_000L,
                now = 1_000L + AutoClearImageCacheInterval.THREE_DAYS_MILLIS
            )
        )
    }
}
