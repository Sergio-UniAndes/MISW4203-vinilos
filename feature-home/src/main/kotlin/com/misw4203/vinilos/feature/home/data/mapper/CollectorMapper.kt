package com.misw4203.vinilos.feature.home.data.mapper

import com.misw4203.vinilos.feature.home.data.remote.dto.CollectorDto
import com.misw4203.vinilos.feature.home.domain.model.Collector

fun CollectorDto.toCollector(): Collector = Collector(
    id = id ?: 0L,
    name = name.orEmpty().ifBlank { "Unknown Collector" },
    telephone = telephone.orEmpty(),
    email = email.orEmpty(),
    albumCount = albumCount,
)
