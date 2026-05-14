package com.misw4203.vinilos.feature.home.data.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AlbumEntity::class, ArtistEntity::class, CollectorEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class VinilosDatabase : RoomDatabase() {

    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun collectorDao(): CollectorDao

    companion object {
        private const val DATABASE_NAME = "vinilos.db"

        @Volatile
        private var instance: VinilosDatabase? = null

        fun get(context: Context): VinilosDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room
                    .databaseBuilder(
                        context.applicationContext,
                        VinilosDatabase::class.java,
                        DATABASE_NAME,
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
