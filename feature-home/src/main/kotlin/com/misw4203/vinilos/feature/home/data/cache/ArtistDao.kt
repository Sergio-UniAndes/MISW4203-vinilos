package com.misw4203.vinilos.feature.home.data.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ArtistDao {

    @Query("SELECT * FROM artists ORDER BY id ASC")
    suspend fun getAll(): List<ArtistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ArtistEntity>)

    @Query("DELETE FROM artists")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(entities: List<ArtistEntity>) {
        deleteAll()
        insertAll(entities)
    }
}
