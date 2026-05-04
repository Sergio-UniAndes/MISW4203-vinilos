package com.misw4203.vinilos.feature.home.data.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey val id: Long,
    val name: String?,
    val image: String?,
    val description: String?,
    val birthDate: String?,
    val cachedAtMillis: Long,
)
