package com.misw4203.vinilos.feature.home.data.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface AlbumDao {

    @Query("SELECT * FROM albums ORDER BY id ASC")
    suspend fun getAll(): List<AlbumEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<AlbumEntity>)

    @Query("DELETE FROM albums")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(entities: List<AlbumEntity>) {
        deleteAll()
        insertAll(entities)
    }
}
