package com.misw4203.vinilos.feature.home.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.misw4203.vinilos.feature.home.domain.model.Artist

// Use AndroidJUnit4 to run instrumented Compose tests. Espresso is available in dependencies for interop.
@RunWith(AndroidJUnit4::class)
class ArtistsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private class FakeArtistsRepository(private val artists: List<Artist>) : com.misw4203.vinilos.feature.home.domain.repository.ArtistsRepository {
        override fun observeArtists(): Flow<List<Artist>> = flowOf(artists)
        override fun observeArtist(id: Long): Flow<Artist?> = flowOf(artists.find { it.id == id })
    }

    @Test
    fun artistsScreen_displaysArtistsAndSpotlight() {
        val sample = listOf(
            Artist(id = 1L, name = "The Midnight Ink", image = null, description = "Artist of the month"),
            Artist(id = 2L, name = "Julian Vance", image = null, description = "12 Records • Neo-Soul"),
        )

        val fakeRepo = FakeArtistsRepository(sample)
        val useCase = com.misw4203.vinilos.feature.home.domain.usecase.ObserveArtistsUseCase(fakeRepo)
        val viewModel = ArtistsViewModel(observeArtistsUseCase = useCase)

        composeRule.setContent {
            MaterialTheme {
                ArtistsScreen(viewModel = viewModel)
            }
        }

        // Title
        composeRule.onNodeWithText("Artists").assertIsDisplayed()

        // Spotlight artist (first)
        composeRule.onNodeWithText("The Midnight Ink").assertIsDisplayed()

        // List item
        composeRule.onNodeWithText("Julian Vance").assertIsDisplayed()
    }
}

