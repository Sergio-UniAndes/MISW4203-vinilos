package com.misw4203.vinilos.feature.home.data.cache

import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import com.misw4203.vinilos.feature.home.data.remote.dto.CollectorDto
import com.misw4203.vinilos.feature.home.data.remote.dto.MusicianDto

internal fun AlbumDto.toEntity(cachedAtMillis: Long): AlbumEntity? {
    val rowId = id ?: return null
    return AlbumEntity(
        id = rowId,
        name = name,
        title = title,
        artist = artist,
        recordLabel = recordLabel,
        label = label,
        format = format,
        cover = cover,
        image = image,
        description = description,
        genre = genre,
        releaseDate = releaseDate,
        cachedAtMillis = cachedAtMillis,
    )
}

internal fun AlbumEntity.toDto(): AlbumDto = AlbumDto(
    id = id,
    name = name,
    title = title,
    artist = artist,
    recordLabel = recordLabel,
    label = label,
    format = format,
    cover = cover,
    image = image,
    description = description,
    genre = genre,
    releaseDate = releaseDate,
)

internal fun MusicianDto.toEntity(cachedAtMillis: Long): ArtistEntity? {
    val rowId = id ?: return null
    return ArtistEntity(
        id = rowId,
        name = name,
        image = image,
        description = description,
        birthDate = birthDate,
        cachedAtMillis = cachedAtMillis,
    )
}

internal fun ArtistEntity.toDto(): MusicianDto = MusicianDto(
    id = id,
    name = name,
    image = image,
    description = description,
    birthDate = birthDate,
)

internal fun CollectorDto.toEntity(cachedAtMillis: Long): CollectorEntity? {
    val rowId = id ?: return null
    return CollectorEntity(
        id = rowId,
        name = name,
        telephone = telephone,
        email = email,
        albumCount = albumCount,
        cachedAtMillis = cachedAtMillis,
    )
}

internal fun CollectorEntity.toDto(): CollectorDto = CollectorDto(
    id = id,
    name = name,
    telephone = telephone,
    email = email,
    albumCount = albumCount,
)
