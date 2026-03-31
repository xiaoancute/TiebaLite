package com.huanchengfly.tieba.post.ui.page.subposts

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.junit.Assert.assertFalse
import org.junit.Test

class SubPostsCopyBehaviorTest {
    @Test
    fun copyMenuDoesNotRouteThroughCopyDialogPage() {
        val source = String(
            Files.readAllBytes(
                subPostsPagePath()
            ),
            StandardCharsets.UTF_8
        )

        assertFalse(
            "sub posts copy should go straight to the clipboard",
            source.contains("CopyTextDialogPageDestination(")
        )
    }

    private fun subPostsPagePath(): Path {
        val candidates = listOf(
            Paths.get("src/main/java/com/huanchengfly/tieba/post/ui/page/subposts/SubPostsPage.kt"),
            Paths.get("app/src/main/java/com/huanchengfly/tieba/post/ui/page/subposts/SubPostsPage.kt"),
        )

        return candidates.firstOrNull(Files::exists)
            ?: throw AssertionError("could not locate SubPostsPage.kt from test working directory")
    }
}
