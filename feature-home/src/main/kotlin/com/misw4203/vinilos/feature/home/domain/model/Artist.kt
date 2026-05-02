package com.misw4203.vinilos.feature.home.domain.model

data class Artist(
    val id: Long,
    val name: String,
    val image: String? = null,
    val description: String? = null,
    val birthDate: String? = null,
)
