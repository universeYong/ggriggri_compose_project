package com.ahn.data.remote.firebase

import android.net.Uri
import com.ahn.data.datasource.StorageDataSource
import com.ahn.domain.common.DataResourceResult
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.google.firebase.storage.storageMetadata
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseStorageDataSourceImpl @Inject constructor(): StorageDataSource {

    private val storage = Firebase.storage

    override suspend fun uploadImage(imageUri: Uri, path: String): DataResourceResult<String> =
        runCatching {
            val fileName = "${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child("$path/$fileName")
            // 보안 규칙이 image/* 만 허용하므로 contentType을 명시한다(파일명이 .jpg라 image/jpeg).
            val metadata = storageMetadata { contentType = "image/jpeg" }
            val uploadTask = storageRef.putFile(imageUri, metadata).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            DataResourceResult.Success(downloadUrl.toString())
        }.getOrElse { DataResourceResult.Failure(it) }


    override suspend fun getImageUrl(path: String): DataResourceResult<String> = runCatching {
        val storageRef = storage.reference.child(path)
        val downloadUrl = storageRef.downloadUrl.await()
        DataResourceResult.Success(downloadUrl.toString())
    }.getOrElse {
        DataResourceResult.Failure(it)
    }
}
