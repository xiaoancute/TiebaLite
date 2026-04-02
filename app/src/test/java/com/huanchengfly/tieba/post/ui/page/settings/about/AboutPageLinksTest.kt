package com.huanchengfly.tieba.post.ui.page.settings.about

import com.huanchengfly.tieba.post.arch.GlobalEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class AboutPageLinksTest {
    @Test
    fun sourceCodeUrlPointsToMaintainedRepository() {
        assertEquals("https://github.com/xiaoancute/TiebaLite", ABOUT_SOURCE_CODE_URL)
    }

    @Test
    fun manualUpdateEventRequestsInteractiveCheck() {
        assertEquals(GlobalEvent.CheckAppUpdate(manual = true), buildManualCheckAppUpdateEvent())
    }
}
