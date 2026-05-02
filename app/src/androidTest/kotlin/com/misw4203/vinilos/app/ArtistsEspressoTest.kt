package com.misw4203.vinilos.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end smoke test for the Artists tab.
 * Uses Espresso for synchronization and Compose matchers for the in-app assertions.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ArtistsEspressoTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun visitor_canOpenArtistsTab_andSeeArtistsScreenHeader() {
        navigateToHomeAsVisitor()

        Espresso.onIdle()
        composeRule.onNodeWithText("Artists").performClick()
        composeRule.waitForIdle()
        Espresso.onIdle()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Find your favorite curator...").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("Artists").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Artists").assertIsDisplayed()
        composeRule.onNodeWithText("Find your favorite curator...").assertIsDisplayed()
    }

    private fun navigateToHomeAsVisitor() {
        waitForText("Select Your Profile")
        composeRule.onNodeWithText("Visitor").performClick()
        composeRule.waitForIdle()
        continueFromAuthIfNeeded()
        Espresso.onIdle()
        waitForText("Vinilos")
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
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

