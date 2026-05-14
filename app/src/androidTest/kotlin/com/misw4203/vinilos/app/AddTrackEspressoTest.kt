package com.misw4203.vinilos.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.misw4203.vinilos.feature.home.ui.ADD_TRACK_CONFIRM_TAG
import com.misw4203.vinilos.feature.home.ui.ADD_TRACK_FAB_TAG
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end smoke tests for HU08 (Add Track to Album).
 *
 * - Verifies that the "Add Track" FAB is visible for Collector but not for Visitor.
 * - Verifies that the AddTrack dialog opens when the FAB is clicked.
 *
 * Backend availability is not guaranteed; data-dependent steps are guarded with
 * [Assume] so the suite stays green in offline environments.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AddTrackEspressoTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun visitor_doesNotSeeAddTrackFab_onAlbumDetail() {
        navigateToAlbumDetailAs(role = "Visitor") ?: return

        // The FAB must not be visible for Visitor.
        val fabNodes = composeRule.onAllNodesWithTag(ADD_TRACK_FAB_TAG).fetchSemanticsNodes()
        assertTrue("Visitor must not see Add Track FAB", fabNodes.isEmpty())
    }

    @Test
    fun collector_seesAddTrackFab_andCanOpenDialog() {
        navigateToAlbumDetailAs(role = "Collector") ?: return

        // FAB is visible for Collector.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(ADD_TRACK_FAB_TAG).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(ADD_TRACK_FAB_TAG).assertIsDisplayed()

        // Opening the dialog.
        composeRule.onNodeWithTag(ADD_TRACK_FAB_TAG).performClick()
        composeRule.waitForIdle()
        Espresso.onIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Add Track to Album").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Add Track to Album").assertIsDisplayed()
        composeRule.onNodeWithText("Search for a track...").assertIsDisplayed()
        composeRule.onNodeWithTag(ADD_TRACK_CONFIRM_TAG).assertIsDisplayed()
    }

    @Test
    fun collector_canDismissAddTrackDialog() {
        navigateToAlbumDetailAs(role = "Collector") ?: return

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(ADD_TRACK_FAB_TAG).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(ADD_TRACK_FAB_TAG).performClick()
        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Add Track to Album").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.waitForIdle()
        Espresso.onIdle()

        // Dialog is dismissed — the title should be gone.
        val dialogNodes = composeRule.onAllNodesWithText("Add Track to Album").fetchSemanticsNodes()
        assertTrue("Dialog must be dismissed after Cancel", dialogNodes.isEmpty())
    }

    // -- helpers --------------------------------------------------------------

    /**
     * Navigates to auth as [role], lands on the catalog, and opens the first album.
     * Returns null (and skips via [Assume]) if no albums are available.
     */
    private fun navigateToAlbumDetailAs(role: String): Unit? {
        waitForText("Select Your Profile")
        composeRule.onNodeWithText(role).performClick()
        composeRule.waitForIdle()
        continueFromAuthIfNeeded()
        Espresso.onIdle()
        waitForText("Vinilos")

        // Navigate into an album — requires backend to have at least one album.
        val featuredAvailable = waitForCondition(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("FEATURED RELEASE").fetchSemanticsNodes().isNotEmpty()
        }
        Assume.assumeTrue(
            "Album catalog not reachable; HU08 Add Track E2E skipped.",
            featuredAvailable,
        )

        composeRule.onNodeWithText("FEATURED RELEASE").performClick()
        composeRule.waitForIdle()
        Espresso.onIdle()

        val detailAvailable = waitForCondition(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("About this release").fetchSemanticsNodes().isNotEmpty()
        }
        Assume.assumeTrue(
            "Album detail not reachable; HU08 Add Track E2E skipped.",
            detailAvailable,
        )

        return Unit
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForCondition(timeoutMillis: Long, condition: () -> Boolean): Boolean =
        runCatching { composeRule.waitUntil(timeoutMillis = timeoutMillis, condition = condition) }
            .isSuccess

    private fun continueFromAuthIfNeeded() {
        val getStartedNodes = composeRule.onAllNodesWithText("Get Started").fetchSemanticsNodes()
        if (getStartedNodes.isNotEmpty()) {
            composeRule.onNodeWithText("Get Started").performClick()
            composeRule.waitForIdle()
        }
    }

    private fun assertTrue(message: String, condition: Boolean) {
        if (!condition) throw AssertionError(message)
    }
}
