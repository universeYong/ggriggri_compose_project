package com.ahn.data.remote.firebase

import android.util.Log
import com.ahn.data.datasource.RequestDataSource
import com.ahn.data.mapper.toDomainRequest
import com.ahn.data.mapper.toFirestoreRequestDTO
import com.ahn.data.remote.dto.RequestDTO
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Request
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRequestDataSourceImpl @Inject constructor(): RequestDataSource {

    private val db = Firebase.firestore

    override suspend fun delete(requestId: String): DataResourceResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun create(request: Request): DataResourceResult<Unit> = runCatching {
        Log.d("FirestoreRequestDataSource", "create() 호출됨 - Request: $request")
        val requestDTO = request.toFirestoreRequestDTO()
        Log.d("FirestoreRequestDataSource", "RequestDTO: $requestDTO")
        
        val documentReference = db.collection("request_data")
            .add(requestDTO)
            .await()
        
        Log.d("FirestoreRequestDataSource", "Firestore에 문서 추가 성공 - Document ID: ${documentReference.id}")
        DataResourceResult.Success(Unit)
    }.getOrElse { 
        Log.e("FirestoreRequestDataSource", "Firestore에 문서 추가 실패", it)
        DataResourceResult.Failure(it) 
    }

    override suspend fun update(request: Request): DataResourceResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun read(groupId: String): Flow<DataResourceResult<List<Request>>> = callbackFlow {
        Log.d("FirestoreRequestDataSource", "그룹 ID로 요청 조회: $groupId")
        
        // 먼저 일회성 쿼리로 현재 데이터를 가져옴
        try {
            // 디버깅을 위해 모든 데이터를 먼저 확인
            val allSnapshot = db.collection("request_data")
                .get()
                .await()
            
            Log.d("FirestoreRequestDataSource", "전체 request_data 컬렉션 문서 수: ${allSnapshot.size()}")
            allSnapshot.documents.forEach { doc ->
                Log.d("FirestoreRequestDataSource", "전체 문서: ${doc.id} - ${doc.data}")
            }
            
            // 그룹 ID로 필터링 (인덱스 없이도 작동하도록 단순화)
            val initialSnapshot = db.collection("request_data")
                .get()
                .await()
            
            val currentTime = System.currentTimeMillis()
            val initialDTOList = initialSnapshot.toObjects(RequestDTO::class.java)
            
            Log.d("FirestoreRequestDataSource", "그룹 ID($groupId)로 필터링된 결과: ${initialDTOList.size}개")
            initialDTOList.forEach { dto ->
                Log.d("FirestoreRequestDataSource", "초기 DTO: hasAnswer=${dto._hasAnswer}, answerDeadline=${dto._answerDeadline}, groupId=${dto._requestGroupDocumentID}")
            }
            
            val initialRequestList = initialSnapshot.documents
                .mapNotNull { document ->
                    val dto = document.toObject(RequestDTO::class.java)
                    dto?.toDomainRequest(document.id)
                }
                .filter { 
                    it.requestGroupDocumentID == groupId &&
                    it.hasAnswer == false && 
                    it.answerDeadline > currentTime 
                }
                .sortedByDescending { it.requestTime }
            
            Log.d("FirestoreRequestDataSource", "초기 요청 목록: ${initialRequestList.size}개")
            trySend(DataResourceResult.Success(initialRequestList))
            
        } catch (e: Exception) {
            Log.e("FirestoreRequestDataSource", "초기 쿼리 오류", e)
            trySend(DataResourceResult.Failure(e))
        }
        
        // 그 다음 실시간 리스너 시작 (인덱스 없이도 작동하도록 단순화)
        val listener = db.collection("request_data")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreRequestDataSource", "실시간 리스너 오류", error)
                    trySend(DataResourceResult.Failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    try {
                        val currentTime = System.currentTimeMillis()
                        val requestDTOList = snapshot.toObjects(RequestDTO::class.java)
                        
                        Log.d("FirestoreRequestDataSource", "실시간 리스너 - Firestore에서 받은 원본 데이터: ${requestDTOList.size}개")
                        requestDTOList.forEach { dto ->
                            Log.d("FirestoreRequestDataSource", "실시간 DTO: hasAnswer=${dto._hasAnswer}, answerDeadline=${dto._answerDeadline}, groupId=${dto._requestGroupDocumentID}")
                        }
                        
                        val requestList = snapshot.documents
                            .mapNotNull { document ->
                                val dto = document.toObject(RequestDTO::class.java)
                                dto?.toDomainRequest(document.id)
                            }
                            .filter { 
                                it.requestGroupDocumentID == groupId &&
                                it.hasAnswer == false && 
                                it.answerDeadline > currentTime 
                            } // 클라이언트에서 필터링
                            .sortedByDescending { it.requestTime } // 클라이언트에서 정렬
                        
                        Log.d("FirestoreRequestDataSource", "실시간 업데이트: ${requestList.size}개 요청 (전체: ${requestDTOList.size}개)")
                        Log.d("FirestoreRequestDataSource", "현재 시간: $currentTime")
                        requestList.forEach { request ->
                            Log.d("FirestoreRequestDataSource", "필터링된 요청: ${request.requestMessage}, 마감: ${request.answerDeadline}, 답변 가능: ${request.isAnswerable()}")
                        }
                        trySend(DataResourceResult.Success(requestList))
                    } catch (e: Exception) {
                        Log.e("FirestoreRequestDataSource", "데이터 변환 오류", e)
                        trySend(DataResourceResult.Failure(e))
                    }
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun readAllRequests(groupId: String): Flow<DataResourceResult<List<Request>>> = callbackFlow {
        Log.d("FirestoreRequestDataSource", "Reading ALL requests for group: $groupId")
        
        // 실시간 리스너만 사용하여 중복 데이터 전송 방지
        val listener = db.collection("request_data")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreRequestDataSource", "모든 요청 실시간 리스너 오류", error)
                    trySend(DataResourceResult.Failure(error))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val dtoList = snapshot.toObjects(RequestDTO::class.java)
                    Log.d("FirestoreRequestDataSource", "실시간 업데이트 - 모든 요청: ${dtoList.size}개")
                    
                    val requestList = snapshot.documents
                        .mapNotNull { document ->
                            val dto = document.toObject(RequestDTO::class.java)
                            dto?.toDomainRequest(document.id)
                        }
                        .filter { it.requestGroupDocumentID == groupId } // 그룹 ID만 필터링
                        .sortedByDescending { it.requestTime } // 최신순 정렬
                    
                    Log.d("FirestoreRequestDataSource", "실시간 업데이트된 모든 요청 목록: ${requestList.size}개")
                    trySend(DataResourceResult.Success(requestList))
                }
            }
        
        awaitClose { listener.remove() }
    }
}