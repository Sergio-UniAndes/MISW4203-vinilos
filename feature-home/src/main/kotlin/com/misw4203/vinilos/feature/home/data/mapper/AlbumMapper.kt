package com.misw4203.vinilos.feature.home.data.mapper

import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import com.misw4203.vinilos.feature.home.domain.model.HomeItem

fun AlbumDto.toHomeItem(index: Int): HomeItem {
    val resolvedTitle = (name ?: title).orEmpty().ifBlank { "Untitled Album" }
    val resolvedArtist = (artist ?: recordLabel).orEmpty().ifBlank { "Unknown Artist" }
    val resolvedGenre = genre.orEmpty().ifBlank { "Unknown" }
    val resolvedYear = releaseDate
        ?.let { YEAR_REGEX.find(it)?.value }
        ?.toIntOrNull()
        ?: 0

    return HomeItem(
        id = id?.toString() ?: "album-$index",
        title = resolvedTitle,
        artist = resolvedArtist,
        year = resolvedYear,
        genre = resolvedGenre,
    )
}

private val YEAR_REGEX = Regex("\\d{4}")

