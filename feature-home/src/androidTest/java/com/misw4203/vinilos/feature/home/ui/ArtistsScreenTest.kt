package com.misw4203.vinilos.feature.home.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.misw4203.vinilos.feature.home.domain.model.Artist
import com.misw4203.vinilos.feature.home.domain.repository.ArtistsRepository
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveArtistsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArtistsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private class FakeArtistsRepository(private val artists: List<Artist>) : ArtistsRepository {
        override fun observeArtists(): Flow<List<Artist>> = flowOf(artists)
        override fun observeArtist(id: Long): Flow<Artist?> = flowOf(artists.find { it.id == id })
    }

    private fun screenWith(artists: List<Artist>) {
        val viewModel = ArtistsViewModel(
            observeArtistsUseCase = ObserveArtistsUseCase(FakeArtistsRepository(artists)),
        )
        composeRule.setContent {
            MaterialTheme {
                ArtistsScreen(viewModel = viewModel)
            }
        }
    }

    @Test
    fun artistsScreen_displaysArtistsAndSpotlight() {
        screenWith(
            listOf(
                Artist(id = 1L, name = "The Midnight Ink", description = "Artist of the month"),
                Artist(id = 2L, name = "Julian Vance", description = "12 Records · Neo-Soul"),
            ),
        )

        composeRule.onNodeWithText("Artists").assertIsDisplayed()
        composeRule.onNodeWithText("The Midnight Ink").assertIsDisplayed()
        composeRule.onNodeWithText("Julian Vance").assertIsDisplayed()
    }

    @Test
    fun artistsScreen_filtersList_whenUserTypesInSearch() {
        screenWith(
            listOf(
                Artist(id = 1L, name = "Ruben Blades"),
                Artist(id = 2L, name = "Joan Manuel Serrat"),
                Artist(id = 3L, name = "Lila Downs"),
            ),
        )

        composeRule.onNodeWithText("Ruben Blades").assertIsDisplayed()
        composeRule.onNodeWithText("Joan Manuel Serrat").assertIsDisplayed()

        composeRule.onNodeWithTag(SEARCH_FIELD_TAG).performTextInput("ruben")
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Ruben Blades").assertIsDisplayed()
        composeRule.onNodeWithText("Joan Manuel Serrat").assertDoesNotExist()
        composeRule.onNodeWithText("Lila Downs").assertDoesNotExist()
    }

    @Test
    fun artistsScreen_showsEmptyMessage_whenNoArtistsAvailable() {
        screenWith(emptyList())

        composeRule.onNodeWithText("No artists in the catalog yet.").assertIsDisplayed()
    }

    @Test
    fun artistsScreen_showsNoMatches_whenQueryHasNoResults() {
        screenWith(listOf(Artist(id = 1L, name = "Ruben Blades")))

        composeRule.onNodeWithTag(SEARCH_FIELD_TAG).performTextInput("zzz")
        composeRule.waitForIdle()

        composeRule.onNodeWithText("No matches for \"zzz\".").assertIsDisplayed()
    }
}
