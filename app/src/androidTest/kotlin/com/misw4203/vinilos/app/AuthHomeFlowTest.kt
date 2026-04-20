package com.misw4203.vinilos.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AuthHomeFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun authScreen_displaysMainOptions() {
        waitForText("Select Your Profile")

        composeRule.onNodeWithText("Visitor").assertIsDisplayed()
        composeRule.onNodeWithText("Collector").assertIsDisplayed()
        composeRule.onNodeWithText("Get Started").assertIsDisplayed()
    }

    @Test
    fun selectingVisitor_navigatesToHome() {
        waitForText("Select Your Profile")

        composeRule.onNodeWithText("Visitor").performClick()
        composeRule.waitForIdle()
        continueFromAuthIfNeeded()

        waitForText("Vinilos")
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun homeBackButton_returnsToAuth() {
        waitForText("Select Your Profile")

        composeRule.onNodeWithText("Visitor").performClick()
        composeRule.waitForIdle()
        waitForText("Vinilos")

        waitForBackButton()
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.waitForIdle()

        waitForText("Select Your Profile")
        composeRule.onNodeWithText("Get Started").assertIsDisplayed()
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForBackButton() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithContentDescription("Back").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun continueFromAuthIfNeeded() {
        val getStartedNodes = composeRule.onAllNodesWithText("Get Started").fetchSemanticsNodes()
        if (getStartedNodes.isNotEmpty()) {
            composeRule.onNodeWithText("Get Started").performClick()
            composeRule.waitForIdle()
        }
    }
}

