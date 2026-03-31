package com.huanchengfly.tieba.post.ui.widgets.compose

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LongClickMenuSourceTest {
    @Test
    fun menuStateSupportsConsumingTheNextHostClickAfterDismiss() {
        val source = readSource(
            "app/src/main/java/com/huanchengfly/tieba/post/ui/widgets/compose/Menu.kt"
        )

        assertTrue(
            "LongClickMenu should track and consume the next host click after dismissing the menu",
            source.contains("consumeNextClick")
        )
    }

    @Test
    fun subPostMenusDoNotDismissByDirectlyMutatingExpandedState() {
        val subPostsPageSource = readSource(
            "app/src/main/java/com/huanchengfly/tieba/post/ui/page/subposts/SubPostsPage.kt"
        )
        val threadPageSource = readSource(
            "app/src/main/java/com/huanchengfly/tieba/post/ui/page/thread/ThreadPage.kt"
        )

        assertFalse(
            "sub posts page should use a consuming dismiss helper instead of mutating menuState.expanded directly",
            subPostsPageSource.contains("menuState.expanded = false")
        )
        assertFalse(
            "thread page should use a consuming dismiss helper instead of mutating menuState.expanded directly",
            threadPageSource.contains("menuState.expanded = false")
        )
    }

    private fun readSource(path: String): String {
        return String(
            Files.readAllBytes(resolvePath(path)),
            StandardCharsets.UTF_8
        )
    }

    private fun resolvePath(path: String): Path {
        val candidates = listOf(
            Paths.get(path.removePrefix("app/")),
            Paths.get(path),
        )

        return candidates.firstOrNull(Files::exists)
            ?: throw AssertionError("could not locate $path from test working directory")
    }
}
