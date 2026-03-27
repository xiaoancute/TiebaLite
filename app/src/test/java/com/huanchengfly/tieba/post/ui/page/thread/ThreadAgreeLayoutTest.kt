package com.huanchengfly.tieba.post.ui.page.thread

import androidx.compose.ui.Alignment
import org.junit.Assert.assertEquals
import org.junit.Test

class ThreadAgreeLayoutTest {
    @Test
    fun bottomBarAgreeContentUsesCenteredVerticalAlignment() {
        assertEquals(Alignment.CenterVertically, bottomBarAgreeVerticalAlignment())
    }
}
