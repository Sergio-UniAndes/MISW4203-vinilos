package com.misw4203.vinilos.feature.home.data.mapper

import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import com.misw4203.vinilos.feature.home.domain.model.AlbumComment
import com.misw4203.vinilos.feature.home.domain.model.AlbumPerformer
import com.misw4203.vinilos.feature.home.domain.model.AlbumTrack
import com.misw4203.vinilos.feature.home.domain.model.HomeItem

fun AlbumDto.toHomeItem(index: Int): HomeItem {
    val resolvedTitle = (name ?: title).orEmpty().ifBlank { "Untitled Album" }
    val resolvedArtist = (artist ?: performers.firstOrNull()?.name ?: recordLabel ?: label)
        .orEmpty()
        .ifBlank { "Unknown Artist" }
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
        recordLabel = recordLabel ?: label,
        format = format,
        coverUrl = cover ?: image,
        description = description,
        releaseDate = releaseDate,
        tracks = tracks.mapNotNull { track ->
            val trackName = track.name.orEmpty().ifBlank { return@mapNotNull null }
            AlbumTrack(
                id = track.id ?: 0L,
                name = trackName,
                duration = track.duration.orEmpty().ifBlank { "--:--" },
            )
        },
        performers = performers.mapNotNull { performer ->
            val performerName = performer.name.orEmpty().ifBlank { return@mapNotNull null }
            AlbumPerformer(
                id = performer.id ?: 0L,
                name = performerName,
                image = performer.image,
                description = performer.description,
                birthDate = performer.birthDate,
            )
        },
        comments = comments.mapNotNull { comment ->
            val commentDescription = comment.description.orEmpty().ifBlank { return@mapNotNull null }
            AlbumComment(
                id = comment.id ?: 0L,
                description = commentDescription,
                rating = (comment.rating ?: 0).coerceIn(0, 5),
            )
        },
    )
}

private val YEAR_REGEX = Regex("\\d{4}")

