package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.core.utils.config.BackendConfig
import com.misw4203.vinilos.feature.home.data.remote.HomeService
import com.misw4203.vinilos.feature.home.data.remote.HttpHomeService
import com.misw4203.vinilos.feature.home.domain.repository.HomeRepository

fun provideHomeRepository(baseUrl: String = BackendConfig.BASE_URL): HomeRepository {
    return RemoteHomeRepository(provideHomeService(baseUrl))
}

fun provideHomeService(baseUrl: String = BackendConfig.BASE_URL): HomeService {
    return HttpHomeService(baseUrl)
}
