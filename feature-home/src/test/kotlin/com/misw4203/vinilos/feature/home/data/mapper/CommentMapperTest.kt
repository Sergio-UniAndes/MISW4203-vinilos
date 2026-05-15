package com.misw4203.vinilos.feature.home.data.mapper

import com.misw4203.vinilos.feature.home.data.remote.dto.CommentDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CommentMapperTest {

    @Test
    fun toAlbumComment_mapsAllFields_whenAllProvided() {
        val dto = CommentDto(id = 7L, description = "Stunning record", rating = 4)

        val comment = dto.toAlbumComment()

        assertEquals(7L, comment?.id)
        assertEquals("Stunning record", comment?.description)
        assertEquals(4, comment?.rating)
    }

    @Test
    fun toAlbumComment_returnsNull_whenDescriptionIsNull() {
        val dto = CommentDto(id = 1L, description = null, rating = 5)
        assertNull(dto.toAlbumComment())
    }

    @Test
    fun toAlbumComment_returnsNull_whenDescriptionIsBlank() {
        val dto = CommentDto(id = 1L, description = "   ", rating = 5)
        assertNull(dto.toAlbumComment())
    }

    @Test
    fun toAlbumComment_defaultsId_whenMissing() {
        val dto = CommentDto(id = null, description = "Hello", rating = 3)
        assertEquals(0L, dto.toAlbumComment()?.id)
    }

    @Test
    fun toAlbumComment_clampsRatingBelowZero_toZero() {
        val dto = CommentDto(id = 1L, description = "x", rating = -2)
        assertEquals(0, dto.toAlbumComment()?.rating)
    }

    @Test
    fun toAlbumComment_clampsRatingAboveFive_toFive() {
        val dto = CommentDto(id = 1L, description = "x", rating = 9)
        assertEquals(5, dto.toAlbumComment()?.rating)
    }

    @Test
    fun toAlbumComment_defaultsRatingToZero_whenNull() {
        val dto = CommentDto(id = 1L, description = "x", rating = null)
        assertEquals(0, dto.toAlbumComment()?.rating)
    }

    @Test
    fun toAlbumComments_filtersOutBlanks() {
        val list = listOf(
            CommentDto(id = 1L, description = "ok", rating = 3),
            CommentDto(id = 2L, description = null, rating = 5),
            CommentDto(id = 3L, description = "", rating = 4),
            CommentDto(id = 4L, description = "great", rating = 4),
        )

        val mapped = list.toAlbumComments()

        assertEquals(2, mapped.size)
        assertEquals("ok", mapped[0].description)
        assertEquals("great", mapped[1].description)
    }
}
