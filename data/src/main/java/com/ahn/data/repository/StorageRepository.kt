package com.ahn.data.repository

import android.net.Uri
import com.ahn.domain.common.DataResourceResult
import kotlinx.coroutines.flow.Flow

interface StorageRepository {
    suspend fun uploadImage(imageUri: Uri, path: String): Flow<DataResourceResult<String>>
    suspend fun getImageUrl(path: String): Flow<DataResourceResult<String>>
}