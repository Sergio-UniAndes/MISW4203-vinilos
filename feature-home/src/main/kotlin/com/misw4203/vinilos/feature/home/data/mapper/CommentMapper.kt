package com.misw4203.vinilos.feature.home.data.mapper

import com.misw4203.vinilos.feature.home.data.remote.dto.CommentDto
import com.misw4203.vinilos.feature.home.domain.model.AlbumComment

fun CommentDto.toAlbumComment(): AlbumComment? {
    val description = description?.takeIf { it.isNotBlank() } ?: return null
    return AlbumComment(
        id = id ?: 0L,
        description = description,
        rating = (rating ?: 0).coerceIn(0, 5),
    )
}

fun List<CommentDto>.toAlbumComments(): List<AlbumComment> = mapNotNull { it.toAlbumComment() }
