package com.ahn.data.repository

import android.net.Uri
import com.ahn.data.datasource.StorageDataSource
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.repository.StorageRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FirebaseStorageRepositoryImpl @Inject constructor(
    private val storageDataSource: StorageDataSource
) : StorageRepository {
    // 플로우로 내뱉어서 서스펜드 노필요
    override suspend fun uploadImage(imageUri: String, path: String): Flow<DataResourceResult<String>> = flow {
        emit(DataResourceResult.Loading)
        // domain은 Uri를 모르므로 문자열로 받고 여기서 변환한다.
        val result = storageDataSource.uploadImage(Uri.parse(imageUri), path)
        emit(result)
    }

    override suspend fun getImageUrl(path: String): Flow<DataResourceResult<String>> = flow {
        emit(DataResourceResult.Loading)
        val result = storageDataSource.getImageUrl(path)
        emit(result)
    }
}