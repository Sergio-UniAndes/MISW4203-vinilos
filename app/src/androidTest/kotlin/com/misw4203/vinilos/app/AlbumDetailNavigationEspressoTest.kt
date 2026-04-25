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
 * End-to-end coverage of HU02 (album detail navigation). Uses Espresso for the system
 * back press and idling synchronization, and Compose finders for in-app nodes. Backend
 * availability is not guaranteed in every environment, so the data-dependent test is
 * skipped via [Assume] when the catalog is empty.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AlbumDetailNavigationEspressoTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun home_rendersTopBar_evenWhenCatalogIsEmpty() {
        navigateToHomeAsVisitor()

        composeRule.onNodeWithText("Vinilos").assertIsDisplayed()
        // The "TOTAL" suffix is present whether the catalog is empty or populated.
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("TOTAL", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun visitor_canOpenFeaturedAlbumDetail_andReturnViaSystemBack() {
        navigateToHomeAsVisitor()

        // Wait briefly for the catalog to load. If the backend is unreachable, the
        // featured release card never renders and the test is skipped — this keeps
        // the suite green in environments without the local API.
        val featuredAvailable = waitForCondition(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("FEATURED RELEASE").fetchSemanticsNodes().isNotEmpty()
        }
        Assume.assumeTrue(
            "Album catalog not reachable; HU02 detail-navigation E2E skipped.",
            featuredAvailable,
        )

        // The card carries the clickable modifier; clicking the inner label propagates.
        composeRule.onNodeWithText("FEATURED RELEASE").performClick()
        composeRule.waitForIdle()
        Espresso.onIdle()

        // Detail screen renders either the section header (success) or the fallback
        // copy if the per-id endpoint rejects the request — both prove navigation worked.
        composeRule.waitUntil(timeoutMillis = 10_000) {
            val onDetail = composeRule.onAllNodesWithText("About this release")
                .fetchSemanticsNodes().isNotEmpty()
            val onFallback = composeRule.onAllNodesWithText("Album not found")
                .fetchSemanticsNodes().isNotEmpty()
            onDetail || onFallback
        }
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()

        Espresso.pressBackUnconditionally()
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
