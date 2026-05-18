package com.misw4203.vinilos.app

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.misw4203.vinilos.core.utils.permissions.DefaultPermissionsPolicy
import com.misw4203.vinilos.core.utils.permissions.PermissionsPolicy
import com.misw4203.vinilos.core.utils.repository.InMemorySessionRepository
import com.misw4203.vinilos.core.utils.repository.SessionRepository
import com.misw4203.vinilos.core.utils.usecase.ClearSessionUseCase
import com.misw4203.vinilos.core.utils.usecase.ObserveSessionUseCase
import com.misw4203.vinilos.feature.auth.domain.SelectRoleUseCase
import com.misw4203.vinilos.feature.auth.ui.AuthViewModel
import com.misw4203.vinilos.feature.home.data.repository.provideArtistsRepository
import com.misw4203.vinilos.feature.home.data.repository.provideCollectorsRepository
import com.misw4203.vinilos.feature.home.data.repository.provideCommentsRepository
import com.misw4203.vinilos.feature.home.data.repository.provideHomeRepository
import com.misw4203.vinilos.feature.home.domain.usecase.AddTrackUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveAlbumDetailUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveArtistDetailUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveArtistsUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveCollectorsUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveCommentsUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveHomeItemsUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.CreateAlbumUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.PostCommentUseCase
import com.misw4203.vinilos.feature.home.ui.AlbumDetailViewModel
import com.misw4203.vinilos.feature.home.ui.ArtistDetailViewModel
import com.misw4203.vinilos.feature.home.ui.ArtistsViewModel
import com.misw4203.vinilos.feature.home.ui.CollectorsViewModel
import com.misw4203.vinilos.feature.home.ui.HomeViewModel

class AppContainer(context: Context) {

    private val appContext: Context = context.applicationContext

    private val permissionsPolicy: PermissionsPolicy = DefaultPermissionsPolicy()
    private val sessionRepository: SessionRepository = InMemorySessionRepository(
        permissionsPolicy = permissionsPolicy,
    )
    private val homeRepository = provideHomeRepository(context = appContext)
    private val artistsRepository = provideArtistsRepository(context = appContext)
    private val collectorsRepository = provideCollectorsRepository(context = appContext)
    private val commentsRepository = provideCommentsRepository()

    private val observeSessionUseCase = ObserveSessionUseCase(sessionRepository)
    private val clearSessionUseCase = ClearSessionUseCase(sessionRepository)
    private val selectRoleUseCase = SelectRoleUseCase(sessionRepository)
    private val observeHomeItemsUseCase = ObserveHomeItemsUseCase(homeRepository)
    private val observeAlbumDetailUseCase = ObserveAlbumDetailUseCase(homeRepository)
    private val createAlbumUseCase = CreateAlbumUseCase(homeRepository)
    private val addTrackUseCase = AddTrackUseCase(homeRepository)
    private val uploadCoverUseCase = com.misw4203.vinilos.feature.home.domain.usecase.UploadCoverUseCase(homeRepository)
    private val observeArtistsUseCase = ObserveArtistsUseCase(artistsRepository)
    private val observeArtistDetailUseCase = ObserveArtistDetailUseCase(artistsRepository)
    private val observeCollectorsUseCase = ObserveCollectorsUseCase(collectorsRepository)
    private val observeCommentsUseCase = ObserveCommentsUseCase(commentsRepository)
    private val postCommentUseCase = PostCommentUseCase(commentsRepository)

    fun bootstrapViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        BootstrapViewModel(observeSessionUseCase)
    }

    fun authViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        AuthViewModel(selectRoleUseCase)
    }

    fun homeViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        HomeViewModel(
            observeSessionUseCase = observeSessionUseCase,
            observeHomeItemsUseCase = observeHomeItemsUseCase,
            clearSessionUseCase = clearSessionUseCase,
        )
    }

    fun albumDetailViewModelFactory(albumId: String): ViewModelProvider.Factory = viewModelFactory {
        AlbumDetailViewModel(
            albumId = albumId,
            observeAlbumDetailUseCase = observeAlbumDetailUseCase,
            observeSessionUseCase = observeSessionUseCase,
            observeCommentsUseCase = observeCommentsUseCase,
            observeCollectorsUseCase = observeCollectorsUseCase,
            addTrackUseCase = addTrackUseCase,
            postCommentUseCase = postCommentUseCase,
        )
    }

    fun createAlbumViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        com.misw4203.vinilos.feature.home.ui.CreateAlbumViewModel(
            createAlbumUseCase = createAlbumUseCase,
            uploadCoverUseCase = uploadCoverUseCase,
        )
    }

    fun artistsViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        ArtistsViewModel(observeArtistsUseCase = observeArtistsUseCase)
    }

    fun artistDetailViewModelFactory(artistId: Long): ViewModelProvider.Factory = viewModelFactory {
        ArtistDetailViewModel(
            artistId = artistId,
            observeArtistDetailUseCase = observeArtistDetailUseCase,
        )
    }

    fun collectorsViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        CollectorsViewModel(observeCollectorsUseCase = observeCollectorsUseCase)
    }
}

inline fun <reified T : ViewModel> viewModelFactory(
    crossinline creator: () -> T,
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = creator() as VM
    }
}
