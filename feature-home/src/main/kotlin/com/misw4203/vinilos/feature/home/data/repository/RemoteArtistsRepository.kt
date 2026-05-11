package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.feature.home.data.cache.ArtistsLocalCache
import com.misw4203.vinilos.feature.home.data.cache.NoopArtistsLocalCache
import com.misw4203.vinilos.feature.home.data.mapper.toArtist
import com.misw4203.vinilos.feature.home.data.remote.ArtistsService
import com.misw4203.vinilos.feature.home.data.remote.dto.MusicianDto
import com.misw4203.vinilos.feature.home.domain.model.Artist
import com.misw4203.vinilos.feature.home.domain.repository.ArtistsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class RemoteArtistsRepository(
    private val service: ArtistsService,
    private val localCache: ArtistsLocalCache = NoopArtistsLocalCache(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ArtistsRepository {

    private val artistsCache = MutableStateFlow<List<Artist>?>(null)

    override fun observeArtists(): Flow<List<Artist>> = flow {
        var hasEmitted = false
        artistsCache.value?.let {
            emit(it)
            hasEmitted = true
        }

        if (artistsCache.value == null) {
            localCache.read()?.let { dtos ->
                val artists = dtos.toArtists()
                artistsCache.value = artists
                emit(artists)
                hasEmitted = true
            }
        }

        val fresh = try {
            service.getMusicians()
        } catch (_: Exception) {
            if (!hasEmitted) {
                val stale = localCache.readStale()?.toArtists().orEmpty()
                artistsCache.value = stale
                emit(stale)
            }
            return@flow
        }
        val freshArtists = fresh.toArtists()
        if (fresh.isNotEmpty()) {
            localCache.write(fresh)
        }
        // HttpArtistsService swallows transport failures and returns []; keep cache instead of blanking.
        if (freshArtists.isEmpty() && artistsCache.value?.isNotEmpty() == true) {
            return@flow
        }
        if (freshArtists != artistsCache.value) {
            artistsCache.value = freshArtists
            emit(freshArtists)
        }
    }.flowOn(ioDispatcher)

    override fun observeArtist(id: Long): Flow<Artist?> = flow {
        emit(service.getMusician(id)?.toArtist())
    }.flowOn(ioDispatcher)

    private fun List<MusicianDto>.toArtists(): List<Artist> = map { it.toArtist() }
}
