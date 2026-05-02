package com.misw4203.vinilos.feature.home.domain.usecase

import android.content.ContentResolver
import com.misw4203.vinilos.feature.home.domain.repository.HomeRepository

class UploadCoverUseCase(
    private val repository: HomeRepository,
) {
    suspend operator fun invoke(contentResolver: ContentResolver, uriString: String): String? =
        repository.uploadCover(contentResolver, uriString)
}


