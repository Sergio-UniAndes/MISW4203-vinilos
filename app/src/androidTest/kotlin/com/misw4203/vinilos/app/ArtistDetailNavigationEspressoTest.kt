package com.misw4203.vinilos.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
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
 * End-to-end coverage of HU04 (artist detail navigation). Uses Espresso for the
 * system back press and idling synchronization, and Compose finders for in-app
 * nodes. The data-dependent test is skipped via [Assume] when no artist is
 * available (e.g. backend unreachable).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ArtistDetailNavigationEspressoTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun visitor_canOpenArtistDetail_andReturnViaSystemBack() {
        navigateToHomeAsVisitor()

        // Switch to the Artists tab.
        composeRule.onNodeWithText("Artists").performClick()
        composeRule.waitForIdle()

        val artistAvailable = waitForCondition(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("SPOTLIGHT").fetchSemanticsNodes().isNotEmpty()
        }
        Assume.assumeTrue(
            "Artist catalog not reachable; HU04 detail-navigation E2E skipped.",
            artistAvailable,
        )

        // The featured artist's name is rendered alongside the SPOTLIGHT badge.
        // Tapping the SPOTLIGHT label propagates the click on the parent card.
        composeRule.onNodeWithText("SPOTLIGHT").performClick()
        composeRule.waitForIdle()
        Espresso.onIdle()

        // Detail screen renders FEATURED ARTIST on success or "Artist not found"
        // if the per-id endpoint rejects the request — both prove navigation worked.
        composeRule.waitUntil(timeoutMillis = 10_000) {
            val onDetail = composeRule.onAllNodesWithText("FEATURED ARTIST")
                .fetchSemanticsNodes().isNotEmpty()
            val onFallback = composeRule.onAllNodesWithText("Artist not found")
                .fetchSemanticsNodes().isNotEmpty()
            onDetail || onFallback
        }
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()

        // Dispatch the system back press through the Activity instead of
        // Espresso.pressBackUnconditionally(). The latter blocks waiting for
        // window focus on the AVD, which can stay false under Compose
        // navigation transitions even with animations disabled.
        composeRule.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()

        // Back from detail returns to home (the home top bar's title is "Vinilos").
        waitForText("Vinilos")
        composeRule.onNodeWithText("Vinilos").assertIsDisplayed()
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

    private fun waitForCondition(timeoutMillis: Long, condition: () -> Boolean): Boolean {
        return runCatching { composeRule.waitUntil(timeoutMillis = timeoutMillis, condition = condition) }
            .isSuccess
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
