package com.misw4203.vinilos.app

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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end coverage of HU01 (role selection → home) driven via Espresso for the
 * system-owned interactions (idling synchronization) and Compose finders for in-app
 * nodes — the standard Compose+Espresso interop pattern.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AuthHomeFlowEspressoTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bootstrap_landsOnAuthScreen() {
        Espresso.onIdle()
        waitForText("Select Your Profile")

        composeRule.onNodeWithText("Select Your Profile").assertIsDisplayed()
        composeRule.onNodeWithText("Visitor").assertIsDisplayed()
        composeRule.onNodeWithText("Collector").assertIsDisplayed()
    }

    @Test
    fun visitorRole_navigatesToHome_andShowsVisitorCopy() {
        waitForText("Select Your Profile")

        composeRule.onNodeWithText("Visitor").performClick()
        composeRule.waitForIdle()
        continueFromAuthIfNeeded()

        Espresso.onIdle()
        waitForText("Vinilos")
        composeRule.onNodeWithText("Vinilos").assertIsDisplayed()

        // Role-dependent copy renders after the session flow emits — wait for it.
        waitForText("Visitor mode · Browse the editorial catalog")
        composeRule.onAllNodesWithText("Visitor mode · Browse the editorial catalog")
            .onFirst().assertIsDisplayed()
    }

    @Test
    fun collectorRole_navigatesToHome_andShowsCollectorCopy() {
        waitForText("Select Your Profile")

        composeRule.onNodeWithText("Collector").performClick()
        composeRule.waitForIdle()
        continueFromAuthIfNeeded()

        Espresso.onIdle()
        waitForText("Vinilos")

        waitForText("Collector mode · Full management enabled")
        composeRule.onAllNodesWithText("Collector mode · Full management enabled")
            .onFirst().assertIsDisplayed()
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
