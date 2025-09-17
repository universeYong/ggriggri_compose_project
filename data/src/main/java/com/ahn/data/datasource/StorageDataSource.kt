package com.ahn.data.datasource

import android.net.Uri
import com.ahn.domain.common.DataResourceResult

interface StorageDataSource {
    suspend fun uploadImage(imageUri: Uri, path: String): DataResourceResult<String>
    suspend fun getImageUrl(path: String): DataResourceResult<String>
}











