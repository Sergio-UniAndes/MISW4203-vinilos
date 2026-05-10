package com.misw4203.vinilos.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end coverage for HU07 (create album navigation and screen rendering).
 * Uses Espresso for system synchronization and Compose finders for in-app UI nodes.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CreateAlbumNavigationEspressoTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun collectorCanOpenCreateAlbumScreen_andSeeTheForm() {
        waitForText("Select Your Profile")
        composeRule.onNodeWithText("Collector").performClick()
        composeRule.waitForIdle()
        continueFromAuthIfNeeded()
        Espresso.onIdle()

        waitForText("Vinilos")
        waitForText("Collector mode · Full management enabled")

        composeRule.onNodeWithText("+ Add Album").assertIsDisplayed()
        composeRule.onNodeWithText("+ Add Album").performClick()
        composeRule.waitForIdle()
        Espresso.onIdle()

        waitForText("Create Album")
        composeRule.onAllNodesWithText("Create Album").assertCountEquals(2)
        composeRule.onNodeWithText("Album name").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Release date").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Choose a cover image").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Description").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Choose a genre from the options below").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Choose a record label from the options below").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Choose album cover image").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Open date picker").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun visitorDoesNotSeeCreateAlbumButton() {
        waitForText("Select Your Profile")
        composeRule.onNodeWithText("Visitor").performClick()
        composeRule.waitForIdle()
        continueFromAuthIfNeeded()
        Espresso.onIdle()

        waitForText("Vinilos")
        waitForText("Visitor mode · Browse the editorial catalog")

        composeRule.onAllNodesWithText("+ Add Album").assertCountEquals(0)
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
