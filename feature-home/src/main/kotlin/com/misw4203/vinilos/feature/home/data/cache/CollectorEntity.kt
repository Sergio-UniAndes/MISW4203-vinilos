package com.misw4203.vinilos.feature.home.data.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collectors")
data class CollectorEntity(
    @PrimaryKey val id: Long,
    val name: String?,
    val telephone: String?,
    val email: String?,
    val albumCount: Int,
    val cachedAtMillis: Long,
)
