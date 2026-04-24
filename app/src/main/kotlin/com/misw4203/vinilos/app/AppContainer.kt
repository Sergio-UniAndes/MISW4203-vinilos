package com.misw4203.vinilos.app

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
import com.misw4203.vinilos.feature.home.data.repository.provideHomeRepository
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveAlbumDetailUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveHomeItemsUseCase
import com.misw4203.vinilos.feature.home.ui.AlbumDetailViewModel
import com.misw4203.vinilos.feature.home.ui.HomeViewModel

class AppContainer {

    private val permissionsPolicy: PermissionsPolicy = DefaultPermissionsPolicy()
    private val sessionRepository: SessionRepository = InMemorySessionRepository(
        permissionsPolicy = permissionsPolicy,
    )
    private val homeRepository = provideHomeRepository()

    private val observeSessionUseCase = ObserveSessionUseCase(sessionRepository)
    private val clearSessionUseCase = ClearSessionUseCase(sessionRepository)
    private val selectRoleUseCase = SelectRoleUseCase(sessionRepository)
    private val observeHomeItemsUseCase = ObserveHomeItemsUseCase(homeRepository)
    private val observeAlbumDetailUseCase = ObserveAlbumDetailUseCase(homeRepository)

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
        )
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
