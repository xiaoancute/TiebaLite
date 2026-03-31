package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.huanchengfly.tieba.post.ui.common.theme.compose.TiebaLiteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class ChipTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun chip_withoutOnClick_hasNoClickAction() {
        composeRule.setContent {
            TiebaLiteTheme {
                Chip(
                    text = "Static chip",
                    modifier = androidx.compose.ui.Modifier.testTag("chip"),
                )
            }
        }

        composeRule.waitForIdle()

        val semantics = composeRule.onNodeWithTag("chip").fetchSemanticsNode().config
        assertFalse(semantics.contains(SemanticsActions.OnClick))
    }

    @Test
    fun chip_withOnClick_keepsClickAction_andInvokesCallback() {
        var clicked = 0

        composeRule.setContent {
            TiebaLiteTheme {
                Chip(
                    text = "Clickable chip",
                    modifier = androidx.compose.ui.Modifier.testTag("chip"),
                    onClick = { clicked++ },
                )
            }
        }

        composeRule.waitForIdle()

        val semantics = composeRule.onNodeWithTag("chip").fetchSemanticsNode().config
        assertTrue(semantics.contains(SemanticsActions.OnClick))

        composeRule.onNodeWithTag("chip").performClick()
        composeRule.runOnIdle {
            assertEquals(1, clicked)
        }
    }
}
