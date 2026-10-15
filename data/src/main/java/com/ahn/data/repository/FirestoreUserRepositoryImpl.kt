package com.ahn.data.repository

import android.util.Log
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.User
import com.ahn.domain.repository.UserRepository
import com.ahn.data.datasource.UserDataSource
import com.ahn.data.remote.dto.FCMTokenRequestDTO
import com.ahn.data.rest.ApiService
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUserRepositoryImpl @Inject constructor(
    val userDataSource: UserDataSource,
    private val apiService: ApiService
) : UserRepository {
    override suspend fun create(userInfo: User): Flow<DataResourceResult<Unit>> = flow {
        emit(DataResourceResult.Loading)
        emit(userDataSource.create(userInfo))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun read(): Flow<DataResourceResult<List<User>>> = flow {
        emit(DataResourceResult.Loading)
        emit(userDataSource.read())
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun update(userInfo: User): Flow<DataResourceResult<Unit>> = flow {
        emit(DataResourceResult.Loading)
        emit(userDataSource.update(userInfo))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun delete(userId: String): Flow<DataResourceResult<Unit>> = flow {
        emit(DataResourceResult.Loading)
        emit(userDataSource.delete(userId))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun getUserGroupDocumentId(userId: String):
            Flow<DataResourceResult<String?>> = flow {
        emit(DataResourceResult.Loading)
        emit(userDataSource.getUserGroupDocumentId(userId))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun updateUserGroupDocumentId(
        userId: String,
        groupDocumentId: String,
    ): Flow<DataResourceResult<Unit>> = flow{
        emit(DataResourceResult.Loading)
        emit(userDataSource.updateUserGroupDocumentId(userId,groupDocumentId))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun getUserById(userId: String): Flow<DataResourceResult<User?>> = flow{
        emit(DataResourceResult.Loading)
        val result = userDataSource.getUserById(userId)
        emit(result)
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun getUserByIdSync(userId: String): DataResourceResult<User?> {
        return userDataSource.getUserByIdSync(userId)
    }

    override suspend fun updateFcmToken(userId: String, token: String): Flow<DataResourceResult<Boolean>> = flow {
        emit(DataResourceResult.Loading)

        val userCollection = Firebase.firestore.collection("user_data")

        // 같은 기기에서 계정을 바꿔 로그인한 경우, 이 토큰이 이전 계정 문서에 남아 있으면
        // 이전 계정의 알림이 이 기기로 온다. 다른 사용자 문서에서 먼저 지운다.
        val previousOwners = userCollection
            .whereArrayContains("_userFcmToken", token)
            .get()
            .await()
        previousOwners.documents
            .filter { it.id != userId }
            .forEach { doc ->
                doc.reference.update("_userFcmToken", FieldValue.arrayRemove(token)).await()
            }

        // Functions는 user_data._userFcmToken을 읽기 때문에 Firestore 저장이 우선 보장되어야 한다.
        userCollection
            .document(userId)
            .set(
                mapOf("_userFcmToken" to FieldValue.arrayUnion(token)),
                SetOptions.merge()
            )
            .await()

        // 백엔드 동기화는 best-effort로 유지한다.
        val request = FCMTokenRequestDTO(userId, token)
        val response = runCatching { apiService.updateFCMToken(request) }.getOrNull()
        if (response?.isSuccessful != true) {
            Log.w("FirestoreUserRepository", "FCM token API sync failed, but Firestore update succeeded.")
        }

        emit(DataResourceResult.Success(true))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun removeFcmToken(userId: String, token: String): Flow<DataResourceResult<Boolean>> = flow {
        emit(DataResourceResult.Loading)
        Firebase.firestore.collection("user_data")
            .document(userId)
            .update("_userFcmToken", FieldValue.arrayRemove(token))
            .await()
        emit(DataResourceResult.Success(true))
    }.catch { emit(DataResourceResult.Failure(it)) }

}