package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.feature.home.data.cache.CollectorsLocalCache
import com.misw4203.vinilos.feature.home.data.remote.CollectorsService
import com.misw4203.vinilos.feature.home.data.remote.dto.CollectorDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteCollectorsRepositoryTest {

    // -- observeCollectors ----------------------------------------------------

    @Test
    fun observeCollectors_mapsServiceDtosToCollectors() = runTest {
        val service = FakeCollectorsService(
            collectors = listOf(
                CollectorDto(
                    id = 1L,
                    name = "Jaime Andrés Monsalve",
                    telephone = "3102178976",
                    email = "j.monsalve@gmail.com",
                    albumCount = 3,
                ),
                CollectorDto(id = null, name = null),
            ),
        )

        val collectors = RemoteCollectorsRepository(service).observeCollectors().first()

        assertEquals(2, collectors.size)
        assertEquals(1L, collectors[0].id)
        assertEquals("Jaime Andrés Monsalve", collectors[0].name)
        assertEquals("3102178976", collectors[0].telephone)
        assertEquals("j.monsalve@gmail.com", collectors[0].email)
        assertEquals(3, collectors[0].albumCount)
        assertEquals(0L, collectors[1].id)
        assertEquals("Unknown Collector", collectors[1].name)
    }

    @Test
    fun observeCollectors_returnsEmpty_whenServiceReturnsEmpty() = runTest {
        val repository = RemoteCollectorsRepository(FakeCollectorsService(collectors = emptyList()))

        assertTrue(repository.observeCollectors().first().isEmpty())
    }

    @Test
    fun observeCollectors_emitsCachedCollectors_beforeNetwork() = runTest {
        val cached = CollectorDto(id = 99L, name = "From Cache", telephone = "0", email = "c@c.com")
        val network = CollectorDto(id = 1L, name = "From Network", telephone = "1", email = "n@n.com")
        val cache = FakeCollectorsLocalCache(initial = listOf(cached))
        val service = FakeCollectorsService(collectors = listOf(network))
        val repository = RemoteCollectorsRepository(service = service, localCache = cache)

        val emissions = repository.observeCollectors().toList()

        assertEquals(2, emissions.size)
        assertEquals("From Cache", emissions[0].single().name)
        assertEquals("From Network", emissions[1].single().name)
    }

    @Test
    fun observeCollectors_persistsNetworkResponse_intoLocalCache() = runTest {
        val cache = FakeCollectorsLocalCache(initial = null)
        val service = FakeCollectorsService(
            collectors = listOf(CollectorDto(id = 1L, name = "Fresh", telephone = "1", email = "f@f.com")),
        )
        val repository = RemoteCollectorsRepository(service = service, localCache = cache)

        repository.observeCollectors().toList()

        val persisted = cache.snapshot()
        assertEquals(1, persisted.size)
        assertEquals("Fresh", persisted[0].name)
    }

    @Test
    fun observeCollectors_doesNotEmitDuplicate_whenNetworkMatchesLocalCache() = runTest {
        val dto = CollectorDto(id = 1L, name = "Same", telephone = "1", email = "s@s.com", albumCount = 2)
        val cache = FakeCollectorsLocalCache(initial = listOf(dto))
        val service = FakeCollectorsService(collectors = listOf(dto))
        val repository = RemoteCollectorsRepository(service = service, localCache = cache)

        val emissions = repository.observeCollectors().toList()

        assertEquals(1, emissions.size)
        assertEquals("Same", emissions[0].single().name)
    }

    @Test
    fun observeCollectors_fallsBackToStaleCache_whenNetworkFails() = runTest {
        val staleDto = CollectorDto(id = 7L, name = "Stale Collector", telephone = "0", email = "s@s.com")
        val cache = FakeCollectorsLocalCache(initial = null, stale = listOf(staleDto))
        val service = FakeCollectorsService(throwOnGet = true)
        val repository = RemoteCollectorsRepository(service = service, localCache = cache)

        val emissions = repository.observeCollectors().toList()

        assertEquals(1, emissions.size)
        assertEquals("Stale Collector", emissions[0].single().name)
    }

    @Test
    fun observeCollectors_emitsEmpty_whenNetworkFailsAndCacheIsEmpty() = runTest {
        val cache = FakeCollectorsLocalCache(initial = null, stale = null)
        val service = FakeCollectorsService(throwOnGet = true)
        val repository = RemoteCollectorsRepository(service = service, localCache = cache)

        val emissions = repository.observeCollectors().toList()

        assertEquals(1, emissions.size)
        assertTrue(emissions[0].isEmpty())
    }

    @Test
    fun observeCollectors_keepsCache_whenServiceSilentlyReturnsEmpty() = runTest {
        val cachedDto = CollectorDto(id = 5L, name = "Cached", telephone = "0", email = "c@c.com")
        val cache = FakeCollectorsLocalCache(initial = listOf(cachedDto))
        val service = FakeCollectorsService(collectors = emptyList())
        val repository = RemoteCollectorsRepository(service = service, localCache = cache)

        val emissions = repository.observeCollectors().toList()

        assertEquals(1, emissions.size)
        assertEquals("Cached", emissions[0].single().name)
    }

    // -- Fakes ----------------------------------------------------------------

    private class FakeCollectorsService(
        private val collectors: List<CollectorDto> = emptyList(),
        private val throwOnGet: Boolean = false,
    ) : CollectorsService {
        override suspend fun getCollectors(): List<CollectorDto> {
            if (throwOnGet) throw java.io.IOException("offline")
            return collectors
        }
    }

    private class FakeCollectorsLocalCache(
        initial: List<CollectorDto>?,
        private val stale: List<CollectorDto>? = initial,
    ) : CollectorsLocalCache {
        private var stored: List<CollectorDto>? = initial

        override suspend fun read(): List<CollectorDto>? = stored
        override suspend fun readStale(): List<CollectorDto>? = stored ?: stale
        override suspend fun write(collectors: List<CollectorDto>) {
            stored = collectors
        }

        fun snapshot(): List<CollectorDto> = stored.orEmpty()
    }
}
