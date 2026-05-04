package com.misw4203.vinilos.feature.home.data.mapper

import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import com.misw4203.vinilos.feature.home.data.remote.dto.MusicianDto
import com.misw4203.vinilos.feature.home.domain.model.Artist
import com.misw4203.vinilos.feature.home.domain.model.ArtistAlbum

fun MusicianDto.toArtist(): Artist {
    return Artist(
        id = this.id ?: 0L,
        name = this.name ?: "Unknown",
        image = this.image,
        description = this.description,
        birthDate = this.birthDate,
        albums = albums.mapNotNull { it.toArtistAlbumOrNull() },
    )
}

private fun AlbumDto.toArtistAlbumOrNull(): ArtistAlbum? {
    val albumId = id ?: return null
    val albumName = (name ?: title).orEmpty().ifBlank { return null }
    return ArtistAlbum(
        id = albumId,
        name = albumName,
        cover = cover ?: image,
        genre = genre,
        releaseDate = releaseDate,
    )
}
