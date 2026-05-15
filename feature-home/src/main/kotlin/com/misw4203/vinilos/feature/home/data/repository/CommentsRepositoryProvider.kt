package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.core.utils.config.BackendConfig
import com.misw4203.vinilos.feature.home.data.remote.CommentsService
import com.misw4203.vinilos.feature.home.data.remote.HttpCommentsService
import com.misw4203.vinilos.feature.home.domain.repository.CommentsRepository

fun provideCommentsRepository(
    baseUrl: String = BackendConfig.BASE_URL,
): CommentsRepository = RemoteCommentsRepository(service = provideCommentsService(baseUrl))

fun provideCommentsService(baseUrl: String = BackendConfig.BASE_URL): CommentsService =
    HttpCommentsService(baseUrl)
