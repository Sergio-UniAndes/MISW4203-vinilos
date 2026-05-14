package com.misw4203.vinilos.feature.home.domain.model

data class Collector(
    val id: Long,
    val name: String,
    val telephone: String,
    val email: String,
    val albumCount: Int = 0,
)
