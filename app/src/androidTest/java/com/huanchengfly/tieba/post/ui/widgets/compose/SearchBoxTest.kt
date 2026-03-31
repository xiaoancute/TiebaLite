package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.TiebaLiteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class SearchBoxTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun searchBox_clickSearch_clearsFocusAfterSubmit() {
        var submittedKeyword = ""

        composeRule.setContent {
            TiebaLiteTheme {
                SearchBox(
                    keyword = "lol",
                    onKeywordChange = {},
                    onKeywordSubmit = { submittedKeyword = it },
                )
            }
        }

        val clearDescription = composeRule.activity.getString(R.string.button_clear)
        val searchDescription = composeRule.activity.getString(R.string.button_search)

        assertTrue(
            composeRule.onAllNodesWithContentDescription(clearDescription)
                .fetchSemanticsNodes().isEmpty()
        )

        composeRule.onNodeWithText("lol").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(clearDescription).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(searchDescription).performClick()

        composeRule.runOnIdle {
            assertEquals("lol", submittedKeyword)
        }
        assertTrue(
            composeRule.onAllNodesWithContentDescription(clearDescription)
                .fetchSemanticsNodes().isEmpty()
        )
    }
}
