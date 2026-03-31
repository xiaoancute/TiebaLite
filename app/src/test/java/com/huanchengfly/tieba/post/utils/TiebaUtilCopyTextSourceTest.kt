package com.huanchengfly.tieba.post.utils

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.junit.Assert.assertFalse
import org.junit.Test

class TiebaUtilCopyTextSourceTest {
    @Test
    fun copyTextDoesNotGateToastBySdkVersion() {
        val source = String(
            Files.readAllBytes(tiebaUtilPath()),
            StandardCharsets.UTF_8
        )

        assertFalse(
            "copyText should show the copy toast on every supported Android version",
            source.contains("Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2")
        )
    }

    private fun tiebaUtilPath(): Path {
        val candidates = listOf(
            Paths.get("src/main/java/com/huanchengfly/tieba/post/utils/TiebaUtil.kt"),
            Paths.get("app/src/main/java/com/huanchengfly/tieba/post/utils/TiebaUtil.kt"),
        )

        return candidates.firstOrNull(Files::exists)
            ?: throw AssertionError("could not locate TiebaUtil.kt from test working directory")
    }
}
