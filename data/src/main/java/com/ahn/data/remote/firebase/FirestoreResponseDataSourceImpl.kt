package com.ahn.data.remote.firebase

import android.util.Log
import com.ahn.data.datasource.ResponseDataSource
import com.ahn.data.mapper.toDomainResponse
import com.ahn.data.mapper.toFirestoreResponseDTO
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Response
import com.google.firebase.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class FirestoreResponseDataSourceImpl @Inject constructor() : ResponseDataSource {

    override suspend fun create(requestDocumentId: String, response: Response): DataResourceResult<Response> = runCatching {
        Log.d("FirestoreResponseDS", "create 시작 - requestDocumentId: $requestDocumentId, response: $response")

        val docRef = Firebase.firestore
            .collection("request_data")
            .document(requestDocumentId)
            .collection("response_data")
            .document()

        val responseWithId = response.copy(responseId = docRef.id)
        val responseDataWithId = responseWithId.toFirestoreResponseDTO()

        docRef.set(responseDataWithId, SetOptions.merge()).await()

        Log.d("FirestoreResponseDS", "Response 생성 성공 - ID: ${docRef.id}")
        DataResourceResult.Success(responseWithId)
    }.getOrElse {
        Log.e("FirestoreResponseDS", "Response 생성 실패", it)
        DataResourceResult.Failure(it)
    }

    override suspend fun read(requestDocumentId: String): DataResourceResult<List<Response>> = runCatching {
        Log.d("FirestoreResponseDS", "read 시작 - requestDocumentId: $requestDocumentId")
        
        val querySnapshot = Firebase.firestore
            .collection("request_data")
            .document(requestDocumentId)
            .collection("response_data")
            .orderBy("_responseCreatedTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        
        val responses = querySnapshot.documents.mapNotNull { document ->
            try {
                val responseDTO = document.toObject(com.ahn.data.remote.dto.ResponseDTO::class.java)
                responseDTO?.toDomainResponse(document.id)
            } catch (e: Exception) {
                Log.w("FirestoreResponseDS", "문서 변환 실패: ${document.id}", e)
                null
            }
        }
        
        Log.d("FirestoreResponseDS", "Response 목록 조회 성공 - ${responses.size}개")
        DataResourceResult.Success(responses)
    }.getOrElse {
        Log.e("FirestoreResponseDS", "Response 목록 조회 실패", it)
        DataResourceResult.Failure(it)
    }

    override suspend fun update(requestDocumentId: String, response: Response): DataResourceResult<Response> = runCatching {
        Log.d("FirestoreResponseDS", "update 시작 - requestDocumentId: $requestDocumentId, responseId: ${response.responseId}")
        
        if (response.responseId.isNullOrEmpty()) {
            throw IllegalArgumentException("Response ID가 필요합니다.")
        }
        
        val responseData = response.toFirestoreResponseDTO()
        Firebase.firestore
            .collection("request_data")
            .document(requestDocumentId)
            .collection("response_data")
            .document(response.responseId)
            .set(responseData, SetOptions.merge())
            .await()
        
        Log.d("FirestoreResponseDS", "Response 업데이트 성공 - ID: ${response.responseId}")
        DataResourceResult.Success(response)
    }.getOrElse {
        Log.e("FirestoreResponseDS", "Response 업데이트 실패", it)
        DataResourceResult.Failure(it)
    }

    override suspend fun delete(requestDocumentId: String, responseId: String): DataResourceResult<Unit> = runCatching {
        Log.d("FirestoreResponseDS", "delete 시작 - requestDocumentId: $requestDocumentId, responseId: $responseId")
        
        Firebase.firestore
            .collection("request_data")
            .document(requestDocumentId)
            .collection("response_data")
            .document(responseId)
            .delete()
            .await()
        
        Log.d("FirestoreResponseDS", "Response 삭제 성공 - ID: $responseId")
        DataResourceResult.Success(Unit)
    }.getOrElse {
        Log.e("FirestoreResponseDS", "Response 삭제 실패", it)
        DataResourceResult.Failure(it)
    }

    override suspend fun getResponseById(requestDocumentId: String, responseId: String): DataResourceResult<Response?> = runCatching {
        Log.d("FirestoreResponseDS", "getResponseById 시작 - requestDocumentId: $requestDocumentId, responseId: $responseId")
        
        val documentSnapshot = Firebase.firestore
            .collection("request_data")
            .document(requestDocumentId)
            .collection("response_data")
            .document(responseId)
            .get()
            .await()
        
        if (documentSnapshot.exists()) {
            val responseDTO = documentSnapshot.toObject(com.ahn.data.remote.dto.ResponseDTO::class.java)
            val response = responseDTO?.toDomainResponse(documentSnapshot.id)
            Log.d("FirestoreResponseDS", "Response 조회 성공 - ID: $responseId")
            DataResourceResult.Success(response)
        } else {
            Log.w("FirestoreResponseDS", "Response 문서가 존재하지 않음 - ID: $responseId")
            DataResourceResult.Success(null)
        }
    }.getOrElse {
        Log.e("FirestoreResponseDS", "Response 조회 실패", it)
        DataResourceResult.Failure(it)
    }

}
