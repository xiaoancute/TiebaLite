package com.huanchengfly.tieba.post.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WaterPostBlockerTest {
    @Test
    fun `blocks common short water posts`() {
        assertTrue(WaterPostBlocker.isWaterPost("+3"))
        assertTrue(WaterPostBlocker.isWaterPost("经验+3"))
        assertTrue(WaterPostBlocker.isWaterPost("顶"))
        assertTrue(WaterPostBlocker.isWaterPost("dd"))
        assertTrue(WaterPostBlocker.isWaterPost("插眼"))
        assertTrue(WaterPostBlocker.isWaterPost("马克"))
        assertTrue(WaterPostBlocker.isWaterPost("cy"))
    }

    @Test
    fun `blocks repeated single character noise`() {
        assertTrue(WaterPostBlocker.isWaterPost("111111"))
        assertTrue(WaterPostBlocker.isWaterPost("哈哈哈哈哈哈"))
        assertTrue(WaterPostBlocker.isWaterPost("......"))
    }

    @Test
    fun `does not block meaningful text that contains water keywords`() {
        assertFalse(WaterPostBlocker.isWaterPost("这个帖子+3张图挺有用"))
        assertFalse(WaterPostBlocker.isWaterPost("楼主说的经验+3其实是游戏机制"))
        assertFalse(WaterPostBlocker.isWaterPost("插眼这个说法现在是不是很少用了"))
        assertFalse(WaterPostBlocker.isWaterPost("顶级理解，写得很清楚"))
    }

    @Test
    fun `does not block blank or normal short replies`() {
        assertFalse(WaterPostBlocker.isWaterPost(""))
        assertFalse(WaterPostBlocker.isWaterPost("谢谢"))
        assertFalse(WaterPostBlocker.isWaterPost("不懂"))
        assertFalse(WaterPostBlocker.isWaterPost("2024"))
        assertFalse(WaterPostBlocker.isWaterPost("可以试试"))
    }
}
