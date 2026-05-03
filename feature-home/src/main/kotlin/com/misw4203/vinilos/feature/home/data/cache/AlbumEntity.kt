package com.misw4203.vinilos.feature.home.data.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: Long,
    val name: String?,
    val title: String?,
    val artist: String?,
    val recordLabel: String?,
    val label: String?,
    val format: String?,
    val cover: String?,
    val image: String?,
    val description: String?,
    val genre: String?,
    val releaseDate: String?,
    val cachedAtMillis: Long,
)
