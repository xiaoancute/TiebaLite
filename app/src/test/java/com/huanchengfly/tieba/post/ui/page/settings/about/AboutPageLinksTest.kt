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

    @Test
    fun previewVersionParserTurnsRecoveryChannelIntoDisplayParts() {
        assertEquals(
            PreviewVersion(baseVersion = "4.0.0", previewVersion = "14"),
            parsePreviewVersion("4.0.0-recovery.14")
        )
    }

    @Test
    fun previewVersionParserLeavesStableVersionUntouched() {
        assertEquals(null, parsePreviewVersion("4.0.0"))
    }
}
