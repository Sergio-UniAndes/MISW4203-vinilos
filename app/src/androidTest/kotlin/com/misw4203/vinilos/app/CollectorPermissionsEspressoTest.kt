package com.misw4203.vinilos.app

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end coverage of role-based UI affordances. The Collector role unlocks Edit
 * and Delete pills on every album tile; the Visitor role hides them. Both assertions
 * are skipped via [Assume] when the catalog cannot be reached, so the suite stays
 * green in environments without the local backend.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CollectorPermissionsEspressoTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun collectorRole_revealsEditAndDeletePills_onAlbumTiles() {
        navigateToHomeAs("Collector")
        Assume.assumeTrue(
            "Album catalog not reachable; collector pills E2E skipped.",
            waitForCatalog(),
        )

        composeRule.onAllNodesWithText("Edit").onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("Delete").onFirst().assertIsDisplayed()
    }

    @Test
    fun visitorRole_doesNotRenderEditOrDeletePills() {
        navigateToHomeAs("Visitor")
        Assume.assumeTrue(
            "Album catalog not reachable; visitor pills E2E skipped.",
            waitForCatalog(),
        )

        composeRule.onAllNodesWithText("Edit").assertCountEquals(0)
        composeRule.onAllNodesWithText("Delete").assertCountEquals(0)
    }

    private fun navigateToHomeAs(role: String) {
        waitForText("Select Your Profile")
        composeRule.onNodeWithText(role).performClick()
        composeRule.waitForIdle()
        continueFromAuthIfNeeded()
        Espresso.onIdle()
        waitForText("Vinilos")
    }

    private fun waitForCatalog(): Boolean {
        return runCatching {
            composeRule.waitUntil(timeoutMillis = 10_000) {
                composeRule.onAllNodesWithText("FEATURED RELEASE").fetchSemanticsNodes().isNotEmpty()
            }
        }.isSuccess
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
        composeRule.onAllNodesWithContentDescription("Back").fetchSemanticsNodes()
    }
}

