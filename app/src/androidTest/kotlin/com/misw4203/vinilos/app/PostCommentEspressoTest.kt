package com.misw4203.vinilos.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.misw4203.vinilos.feature.home.ui.COMMENTS_SECTION_TAG
import com.misw4203.vinilos.feature.home.ui.COMMENT_INPUT_TAG
import com.misw4203.vinilos.feature.home.ui.COMMENT_POST_TAG
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end smoke tests for HU09 (Comment an Album).
 *
 * - Verifies that the comment composer is hidden for Visitors and visible for Collectors.
 * - Verifies that the composer renders the input + post button.
 *
 * Backend availability is not guaranteed; data-dependent steps are guarded with
 * [Assume] so the suite stays green in offline environments.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class PostCommentEspressoTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun visitor_doesNotSeeCommentComposer_onAlbumDetail() {
        navigateToAlbumDetailAs(role = "Visitor") ?: return

        // Comment input must not appear for Visitor.
        val inputNodes = composeRule.onAllNodesWithTag(COMMENT_INPUT_TAG).fetchSemanticsNodes()
        assertTrue("Visitor must not see comment input", inputNodes.isEmpty())

        val postNodes = composeRule.onAllNodesWithTag(COMMENT_POST_TAG).fetchSemanticsNodes()
        assertTrue("Visitor must not see Post Comment button", postNodes.isEmpty())
    }

    @Test
    fun collector_seesCommentComposer_andCommentsSection() {
        navigateToAlbumDetailAs(role = "Collector") ?: return

        // The comments section renders below the fold on this layout — assert the
        // semantics tree contains it rather than asserting visual visibility.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(COMMENTS_SECTION_TAG).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag(COMMENTS_SECTION_TAG).assertExists()
        composeRule.onNodeWithTag(COMMENT_INPUT_TAG).assertExists()
        composeRule.onNodeWithTag(COMMENT_POST_TAG).assertExists()
        composeRule.onNodeWithText("Comments").assertExists()
        composeRule.onNodeWithText("ADD A COLLECTOR'S NOTE").assertExists()
    }

    // -- helpers --------------------------------------------------------------

    private fun navigateToAlbumDetailAs(role: String): Unit? {
        waitForText("Select Your Profile")
        composeRule.onNodeWithText(role).performClick()
        composeRule.waitForIdle()
        continueFromAuthIfNeeded()
        Espresso.onIdle()
        waitForText("Vinilos")

        val featuredAvailable = waitForCondition(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("FEATURED RELEASE").fetchSemanticsNodes().isNotEmpty()
        }
        Assume.assumeTrue(
            "Album catalog not reachable; HU09 Post Comment E2E skipped.",
            featuredAvailable,
        )

        composeRule.onNodeWithText("FEATURED RELEASE").performClick()
        composeRule.waitForIdle()
        Espresso.onIdle()

        val detailAvailable = waitForCondition(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("About this release").fetchSemanticsNodes().isNotEmpty()
        }
        Assume.assumeTrue(
            "Album detail not reachable; HU09 Post Comment E2E skipped.",
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
