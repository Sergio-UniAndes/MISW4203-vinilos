package com.misw4203.vinilos.feature.home.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.misw4203.vinilos.feature.home.domain.model.Artist
import com.misw4203.vinilos.feature.home.domain.model.ArtistAlbum
import com.misw4203.vinilos.feature.home.domain.repository.ArtistsRepository
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveArtistDetailUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArtistDetailScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private class FakeRepository(private val byId: Map<Long, Artist>) : ArtistsRepository {
        override fun observeArtists(): Flow<List<Artist>> = flowOf(byId.values.toList())
        override fun observeArtist(id: Long): Flow<Artist?> = flowOf(byId[id])
    }

    private fun screenWith(
        artist: Artist?,
        onAlbumClick: (Long) -> Unit = {},
        onBack: () -> Unit = {},
    ) {
        val byId = if (artist != null) mapOf(artist.id to artist) else emptyMap()
        val viewModel = ArtistDetailViewModel(
            artistId = artist?.id ?: 0L,
            observeArtistDetailUseCase = ObserveArtistDetailUseCase(FakeRepository(byId)),
        )
        composeRule.setContent {
            MaterialTheme {
                ArtistDetailScreen(
                    viewModel = viewModel,
                    onBack = onBack,
                    onAlbumClick = onAlbumClick,
                )
            }
        }
    }

    @Test
    fun artistDetail_rendersHeroAndNarrative() {
        screenWith(
            Artist(
                id = 1L,
                name = "Ruben Blades",
                description = "Cantante panameño.",
                birthDate = "1948-07-16",
                albums = listOf(
                    ArtistAlbum(id = 10L, name = "Buscando America", genre = "Salsa"),
                ),
            ),
        )

        composeRule.onNodeWithText("FEATURED ARTIST").assertIsDisplayed()
        composeRule.onNodeWithText("Ruben Blades").assertIsDisplayed()
        // Narrative + discography live below the fold on tall screens; assert
        // they were rendered into the semantics tree (existence, not visibility).
        composeRule.onNodeWithText("Cantante panameño.").assertExists()
        composeRule.onNodeWithText("DISCOGRAPHY").assertExists()
        composeRule.onNodeWithText("Buscando America").assertExists()
    }

    @Test
    fun artistDetail_showsEmptyDiscographyMessage_whenNoAlbums() {
        screenWith(
            Artist(
                id = 1L,
                name = "Solo Artist",
                albums = emptyList(),
            ),
        )

        composeRule.onNodeWithText("No releases on file yet.").assertExists()
    }

    @Test
    fun artistDetail_showsNotFound_whenArtistIsNull() {
        screenWith(artist = null)

        composeRule.onNodeWithText("Artist not found").assertIsDisplayed()
    }

}
