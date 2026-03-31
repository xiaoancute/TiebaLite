package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.VideoInfo
import com.huanchengfly.tieba.post.arch.wrapImmutable
import com.huanchengfly.tieba.post.ui.common.theme.compose.TiebaLiteTheme
import com.huanchengfly.tieba.post.ui.page.ProvideNavigator
import com.huanchengfly.tieba.post.utils.appPreferences
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.Route
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class FeedCardTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val noOpNavigator = object : DestinationsNavigator {
        override fun navigate(
            direction: Direction,
            onlyIfResumed: Boolean,
            builder: NavOptionsBuilder.() -> Unit
        ) = Unit

        override fun navigate(
            route: String,
            onlyIfResumed: Boolean,
            builder: NavOptionsBuilder.() -> Unit
        ) = Unit

        override fun navigate(
            direction: Direction,
            onlyIfResumed: Boolean,
            navOptions: NavOptions?,
            navigatorExtras: Navigator.Extras?
        ) = Unit

        override fun navigate(
            route: String,
            onlyIfResumed: Boolean,
            navOptions: NavOptions?,
            navigatorExtras: Navigator.Extras?
        ) = Unit

        override fun navigateUp(): Boolean = false

        override fun popBackStack(): Boolean = false

        override fun popBackStack(
            route: Route,
            inclusive: Boolean,
            saveState: Boolean
        ): Boolean = false

        override fun popBackStack(
            route: String,
            inclusive: Boolean,
            saveState: Boolean
        ): Boolean = false

        override fun clearBackStack(route: Route): Boolean = false

        override fun clearBackStack(route: String): Boolean = false
    }

    @Test
    fun feedCard_hiddenVideoPlaceholder_keepsParentCardClick() {
        val preferences = composeRule.activity.appPreferences
        val originalHideMedia = preferences.hideMedia
        preferences.hideMedia = true

        try {
            var clicked = 0
            val videoLabel = composeRule.activity.getString(com.huanchengfly.tieba.post.R.string.desc_video)

            composeRule.setContent {
                TiebaLiteTheme {
                    ProvideNavigator(noOpNavigator) {
                        FeedCard(
                            item = ThreadInfo(
                                title = "带视频的帖子",
                                author = User(name = "tester"),
                                lastTimeInt = ((System.currentTimeMillis() / 1000) - 120).toInt(),
                                videoInfo = VideoInfo(
                                    videoUrl = "https://example.com/video.mp4",
                                    thumbnailUrl = "https://example.com/thumb.jpg",
                                    thumbnailWidth = 100,
                                    thumbnailHeight = 100,
                                ),
                            ).wrapImmutable(),
                            onClick = { clicked++ },
                            onAgree = {},
                        )
                    }
                }
            }

            composeRule.onNodeWithText(videoLabel).performTouchInput {
                click(center)
            }

            composeRule.runOnIdle {
                assertEquals(1, clicked)
            }
        } finally {
            preferences.hideMedia = originalHideMedia
        }
    }
}
