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
    fun `blocks expanded short marker water posts`() {
        assertTrue(WaterPostBlocker.isWaterPost("蹲蹲"))
        assertTrue(WaterPostBlocker.isWaterPost("坐等"))
        assertTrue(WaterPostBlocker.isWaterPost("前排"))
        assertTrue(WaterPostBlocker.isWaterPost("留爪"))
        assertTrue(WaterPostBlocker.isWaterPost("m"))
        assertTrue(WaterPostBlocker.isWaterPost("mark一下"))
        assertTrue(WaterPostBlocker.isWaterPost("顶顶顶"))
        assertTrue(WaterPostBlocker.isWaterPost("看看看"))
        assertTrue(WaterPostBlocker.isWaterPost("插个眼"))
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
        assertFalse(WaterPostBlocker.isWaterPost("前排提醒一下这个设置会误伤"))
        assertFalse(WaterPostBlocker.isWaterPost("收藏了很多有用资料"))
        assertFalse(WaterPostBlocker.isWaterPost("这个代码码住了吗"))
        assertFalse(WaterPostBlocker.isWaterPost("m 系列芯片挺强"))
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
