package com.misw4203.vinilos.feature.home.data.cache

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomAlbumsLocalCacheTest {

    private lateinit var database: VinilosDatabase
    private lateinit var dao: AlbumDao

    @Before
    fun setUp() {
        database = Room
            .inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                VinilosDatabase::class.java,
            )
            .allowMainThreadQueries()
            .build()
        dao = database.albumDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun read_returnsNull_whenEmpty() = runTest {
        val cache = RoomAlbumsLocalCache(dao = dao, ttlMillis = 60_000L, now = { 0L })

        assertNull(cache.read())
    }

    @Test
    fun write_thenRead_returnsPersistedAlbums() = runTest {
        val cache = RoomAlbumsLocalCache(dao = dao, ttlMillis = 60_000L, now = { 1_000L })

        cache.write(
            listOf(
                AlbumDto(id = 1L, name = "Buscando America", artist = "Ruben Blades", genre = "Salsa"),
                AlbumDto(id = 2L, name = "Poeta del Pueblo", artist = "Hector Lavoe", genre = "Salsa"),
            ),
        )

        val result = cache.read()
        assertNotNull(result)
        assertEquals(2, result!!.size)
        assertEquals(1L, result[0].id)
        assertEquals("Buscando America", result[0].name)
        assertEquals("Hector Lavoe", result[1].artist)
    }

    @Test
    fun read_returnsNull_whenEntriesAreExpired() = runTest {
        var clock = 0L
        val cache = RoomAlbumsLocalCache(dao = dao, ttlMillis = 1_000L, now = { clock })

        clock = 0L
        cache.write(listOf(AlbumDto(id = 9L, name = "Old", genre = "Rock")))

        clock = 5_000L
        assertNull(cache.read())
    }

    @Test
    fun write_replacesPreviousEntries() = runTest {
        val cache = RoomAlbumsLocalCache(dao = dao, ttlMillis = 60_000L, now = { 1L })

        cache.write(listOf(AlbumDto(id = 1L, name = "First", genre = "Jazz")))
        cache.write(listOf(AlbumDto(id = 2L, name = "Second", genre = "Rock")))

        val result = cache.read()!!
        assertEquals(1, result.size)
        assertEquals(2L, result[0].id)
        assertEquals("Second", result[0].name)
    }

    @Test
    fun write_skipsAlbumsWithoutId() = runTest {
        val cache = RoomAlbumsLocalCache(dao = dao, ttlMillis = 60_000L, now = { 1L })

        cache.write(
            listOf(
                AlbumDto(id = null, name = "No id"),
                AlbumDto(id = 7L, name = "Has id", genre = "Folk"),
            ),
        )

        val result = cache.read()!!
        assertEquals(1, result.size)
        assertEquals(7L, result[0].id)
    }

    @Test
    fun write_emptyList_isNoop() = runTest {
        val cache = RoomAlbumsLocalCache(dao = dao, ttlMillis = 60_000L, now = { 1L })
        cache.write(listOf(AlbumDto(id = 1L, name = "First", genre = "Jazz")))

        cache.write(emptyList())

        val result = cache.read()!!
        assertEquals(1, result.size)
        assertEquals("First", result[0].name)
    }
}
