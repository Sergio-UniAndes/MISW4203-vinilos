package com.misw4203.vinilos.feature.home.data.mapper

import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import com.misw4203.vinilos.feature.home.data.remote.dto.CommentDto
import com.misw4203.vinilos.feature.home.data.remote.dto.PerformerDto
import com.misw4203.vinilos.feature.home.data.remote.dto.TrackDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AlbumMapperTest {

    @Test
    fun mapsCompleteDto_toHomeItem() {
        val dto = AlbumDto(
            id = 7L,
            name = "Buscando America",
            artist = "Ruben Blades",
            recordLabel = "Elektra",
            genre = "Salsa",
            releaseDate = "1984-08-01",
            cover = "http://example.com/cover.png",
            description = "Politically charged salsa album",
            format = "Vinyl",
            tracks = listOf(TrackDto(id = 1L, name = "Decisiones", duration = "5:00")),
            performers = listOf(PerformerDto(id = 9L, name = "Ruben Blades")),
            comments = listOf(CommentDto(id = 2L, description = "Great", rating = 5)),
        )

        val item = dto.toHomeItem(0)

        assertEquals("7", item.id)
        assertEquals("Buscando America", item.title)
        assertEquals("Ruben Blades", item.artist)
        assertEquals("Salsa", item.genre)
        assertEquals(1984, item.year)
        assertEquals("Elektra", item.recordLabel)
        assertEquals("Vinyl", item.format)
        assertEquals("http://example.com/cover.png", item.coverUrl)
        assertEquals("Politically charged salsa album", item.description)
        assertEquals(1, item.tracks.size)
        assertEquals("Decisiones", item.tracks[0].name)
        assertEquals(1, item.performers.size)
        assertEquals(1, item.comments.size)
    }

    @Test
    fun missingId_fallsBackToIndexedPlaceholder() {
        val item = AlbumDto().toHomeItem(4)

        assertEquals("album-4", item.id)
    }

    @Test
    fun nullName_fallsBackToTitle_thenToUntitled() {
        val withTitle = AlbumDto(name = null, title = "From Title").toHomeItem(0)
        assertEquals("From Title", withTitle.title)

        val empty = AlbumDto(name = null, title = null).toHomeItem(0)
        assertEquals("Untitled Album", empty.title)

        val blank = AlbumDto(name = "  ", title = "  ").toHomeItem(0)
        assertEquals("Untitled Album", blank.title)
    }

    @Test
    fun artistResolution_fallsThroughPerformerThenLabel_thenUnknown() {
        val fromPerformer = AlbumDto(
            artist = null,
            performers = listOf(PerformerDto(name = "Performer Name")),
        ).toHomeItem(0)
        assertEquals("Performer Name", fromPerformer.artist)

        val fromRecordLabel = AlbumDto(
            artist = null,
            performers = emptyList(),
            recordLabel = "Some Label",
        ).toHomeItem(0)
        assertEquals("Some Label", fromRecordLabel.artist)

        val fromLabel = AlbumDto(
            artist = null,
            performers = emptyList(),
            recordLabel = null,
            label = "Fallback Label",
        ).toHomeItem(0)
        assertEquals("Fallback Label", fromLabel.artist)

        val unknown = AlbumDto().toHomeItem(0)
        assertEquals("Unknown Artist", unknown.artist)
    }

    @Test
    fun blankGenre_becomesUnknown() {
        assertEquals("Unknown", AlbumDto(genre = "  ").toHomeItem(0).genre)
        assertEquals("Unknown", AlbumDto(genre = null).toHomeItem(0).genre)
    }

    @Test
    fun yearIsExtractedFromReleaseDate_andFallsBackToZero() {
        assertEquals(1999, AlbumDto(releaseDate = "1999-12-31").toHomeItem(0).year)
        assertEquals(2024, AlbumDto(releaseDate = "Some text 2024 more").toHomeItem(0).year)
        assertEquals(0, AlbumDto(releaseDate = "no year here").toHomeItem(0).year)
        assertEquals(0, AlbumDto(releaseDate = null).toHomeItem(0).year)
    }

    @Test
    fun blankTrackName_isDropped_andMissingDurationGetsPlaceholder() {
        val dto = AlbumDto(
            tracks = listOf(
                TrackDto(id = 1L, name = "Real", duration = null),
                TrackDto(id = 2L, name = "  ", duration = "3:00"),
                TrackDto(id = null, name = "Other", duration = ""),
            ),
        )

        val tracks = dto.toHomeItem(0).tracks
        assertEquals(2, tracks.size)
        assertEquals("Real", tracks[0].name)
        assertEquals("--:--", tracks[0].duration)
        assertEquals(0L, tracks[1].id)
        assertEquals("--:--", tracks[1].duration)
    }

    @Test
    fun blankPerformerName_isDropped() {
        val dto = AlbumDto(
            performers = listOf(
                PerformerDto(name = "Keep Me"),
                PerformerDto(name = "  "),
                PerformerDto(name = null),
            ),
        )

        val performers = dto.toHomeItem(0).performers
        assertEquals(1, performers.size)
        assertEquals("Keep Me", performers[0].name)
    }

    @Test
    fun blankCommentDescription_isDropped_andRatingIsClamped() {
        val dto = AlbumDto(
            comments = listOf(
                CommentDto(description = "Loved it", rating = 9),
                CommentDto(description = "  ", rating = 4),
                CommentDto(description = "Hated it", rating = -3),
                CommentDto(description = "No rating", rating = null),
            ),
        )

        val comments = dto.toHomeItem(0).comments
        assertEquals(3, comments.size)
        assertEquals(5, comments[0].rating)
        assertEquals(0, comments[1].rating)
        assertEquals(0, comments[2].rating)
    }

    @Test
    fun emptyDto_producesEmptyCollectionsAndNullableFields() {
        val item = AlbumDto().toHomeItem(0)

        assertTrue(item.tracks.isEmpty())
        assertTrue(item.performers.isEmpty())
        assertTrue(item.comments.isEmpty())
        assertNull(item.coverUrl)
        assertNull(item.description)
        assertNull(item.format)
        assertNull(item.releaseDate)
    }

    @Test
    fun cover_fallsBackToImage_whenCoverIsMissing() {
        val item = AlbumDto(cover = null, image = "http://example.com/img.png").toHomeItem(0)
        assertEquals("http://example.com/img.png", item.coverUrl)
    }
}
