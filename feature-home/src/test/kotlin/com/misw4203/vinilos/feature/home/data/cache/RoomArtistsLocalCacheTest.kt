package com.misw4203.vinilos.feature.home.data.cache

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.misw4203.vinilos.feature.home.data.remote.dto.MusicianDto
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
class RoomArtistsLocalCacheTest {

    private lateinit var database: VinilosDatabase
    private lateinit var dao: ArtistDao

    @Before
    fun setUp() {
        database = Room
            .inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                VinilosDatabase::class.java,
            )
            .allowMainThreadQueries()
            .build()
        dao = database.artistDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun read_returnsNull_whenEmpty() = runTest {
        val cache = RoomArtistsLocalCache(dao = dao, ttlMillis = 60_000L, now = { 0L })

        assertNull(cache.read())
    }

    @Test
    fun write_thenRead_returnsPersistedArtists() = runTest {
        val cache = RoomArtistsLocalCache(dao = dao, ttlMillis = 60_000L, now = { 1_000L })

        cache.write(
            listOf(
                MusicianDto(id = 100L, name = "Ruben Blades", description = "Cantante panameño"),
                MusicianDto(id = 101L, name = "Joan Manuel Serrat", description = "Cantautor español"),
            ),
        )

        val result = cache.read()
        assertNotNull(result)
        assertEquals(2, result!!.size)
        assertEquals("Ruben Blades", result[0].name)
        assertEquals("Joan Manuel Serrat", result[1].name)
    }

    @Test
    fun read_returnsNull_whenEntriesAreExpired() = runTest {
        var clock = 0L
        val cache = RoomArtistsLocalCache(dao = dao, ttlMillis = 1_000L, now = { clock })

        clock = 0L
        cache.write(listOf(MusicianDto(id = 1L, name = "Old")))

        clock = 5_000L
        assertNull(cache.read())
    }

    @Test
    fun readStale_returnsExpiredEntries_forOfflineFallback() = runTest {
        var clock = 0L
        val cache = RoomArtistsLocalCache(dao = dao, ttlMillis = 1_000L, now = { clock })

        cache.write(listOf(MusicianDto(id = 1L, name = "Old")))

        clock = 5_000L
        val stale = cache.readStale()
        assertNotNull(stale)
        assertEquals(1, stale!!.size)
        assertEquals("Old", stale[0].name)
    }

    @Test
    fun write_replacesPreviousEntries() = runTest {
        val cache = RoomArtistsLocalCache(dao = dao, ttlMillis = 60_000L, now = { 1L })

        cache.write(listOf(MusicianDto(id = 1L, name = "First")))
        cache.write(listOf(MusicianDto(id = 2L, name = "Second")))

        val result = cache.read()!!
        assertEquals(1, result.size)
        assertEquals(2L, result[0].id)
    }

    @Test
    fun write_skipsArtistsWithoutId() = runTest {
        val cache = RoomArtistsLocalCache(dao = dao, ttlMillis = 60_000L, now = { 1L })

        cache.write(
            listOf(
                MusicianDto(id = null, name = "No id"),
                MusicianDto(id = 7L, name = "Has id"),
            ),
        )

        val result = cache.read()!!
        assertEquals(1, result.size)
        assertEquals(7L, result[0].id)
    }
}
