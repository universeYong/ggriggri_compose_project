package com.ahn.domain.repository

import com.ahn.domain.common.DataResourceResult
import kotlinx.coroutines.flow.Flow

interface StorageRepository {
    // domain은 순수 JVM 모듈이라 android.net.Uri를 쓸 수 없다.
    // 호출 측은 Uri.toString()을 넘기고, data 구현체에서 Uri로 되돌린다.
    suspend fun uploadImage(imageUri: String, path: String): Flow<DataResourceResult<String>>
    suspend fun getImageUrl(path: String): Flow<DataResourceResult<String>>
}
