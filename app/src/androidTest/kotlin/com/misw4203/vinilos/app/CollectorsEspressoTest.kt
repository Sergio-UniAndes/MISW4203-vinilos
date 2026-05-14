package com.misw4203.vinilos.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end smoke test for the Collectors tab (HU05).
 * Uses Espresso for synchronization and Compose matchers for in-app assertions.
 * Covers both roles — the screen is read-only for both Visitor and Collector.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CollectorsEspressoTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun visitor_canOpenCollectorsTab_andSeeCollectorsScreenHeader() {
        navigateToHomeAs(role = "Visitor")

        Espresso.onIdle()
        composeRule.onNodeWithText("Collectors").performClick()
        composeRule.waitForIdle()
        Espresso.onIdle()

        // "Search collectors..." is unique to the Collectors screen.
        // Waiting for it avoids a race with the bare "Collectors" label that
        // already exists in the bottom-nav bar before the screen renders.
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Search collectors...").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Search collectors...").assertIsDisplayed()
        // "Collectors" appears both as screen title and bottom-nav label; onAllNodes avoids ambiguity.
        composeRule.onAllNodesWithText("Collectors").onFirst().assertIsDisplayed()
    }

    @Test
    fun collector_canOpenCollectorsTab_andSeeCollectorsScreenHeader() {
        navigateToHomeAs(role = "Collector")

        Espresso.onIdle()
        composeRule.onNodeWithText("Collectors").performClick()
        composeRule.waitForIdle()
        Espresso.onIdle()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Search collectors...").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Search collectors...").assertIsDisplayed()
        composeRule.onAllNodesWithText("Collectors").onFirst().assertIsDisplayed()
    }

    // -- helpers --------------------------------------------------------------

    private fun navigateToHomeAs(role: String) {
        waitForText("Select Your Profile")
        composeRule.onNodeWithText(role).performClick()
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
