package com.misw4203.vinilos.feature.home.data.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface CollectorDao {
    @Query("SELECT * FROM collectors ORDER BY id ASC")
    suspend fun getAll(): List<CollectorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(collectors: List<CollectorEntity>)

    @Query("DELETE FROM collectors")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(collectors: List<CollectorEntity>) {
        deleteAll()
        insertAll(collectors)
    }
}
