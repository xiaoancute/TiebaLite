package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.huanchengfly.tieba.post.ui.common.theme.compose.TiebaLiteTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LongClickMenuTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun longClickMenu_clickingMenuItem_doesNotTriggerHostClick() {
        var hostClickCount = 0
        var menuClickCount = 0

        composeRule.setContent {
            TiebaLiteTheme {
                LongClickMenu(
                    onClick = { hostClickCount++ },
                    menuContent = {
                        DropdownMenuItem(
                            onClick = { menuClickCount++ }
                        ) {
                            Text("Copy")
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .testTag("long-click-menu-host")
                    ) {
                        Text("Host")
                    }
                }
            }
        }

        composeRule.onNodeWithTag("long-click-menu-host", useUnmergedTree = true).performTouchInput {
            longClick(center)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Copy").performTouchInput {
            click(center)
        }

        composeRule.runOnIdle {
            assertEquals(1, menuClickCount)
            assertEquals(0, hostClickCount)
        }
    }
}
