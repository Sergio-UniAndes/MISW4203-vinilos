package com.misw4203.vinilos.feature.home.data.mapper

import com.misw4203.vinilos.feature.home.data.remote.dto.PerformerDto
import com.misw4203.vinilos.feature.home.domain.model.Artist

fun PerformerDto.toArtist(): Artist {
    return Artist(
        id = this.id ?: 0L,
        name = this.name ?: "Unknown",
        image = this.image,
        description = this.description,
        birthDate = this.birthDate,
    )
}

