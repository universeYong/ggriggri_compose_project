package com.ahn.data.repository

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.User
import com.ahn.domain.repository.UserRepository
import com.ahn.data.datasource.UserDataSource
import com.ahn.data.remote.dto.FCMTokenRequestDTO
import com.ahn.data.rest.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
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

            val request = FCMTokenRequestDTO(userId, token)
            val response = apiService.updateFCMToken(request)

            if (response.isSuccessful) {
                emit(DataResourceResult.Success(true))
            } else {
                emit(DataResourceResult.Failure(Exception("Failed to update FCM token")))
            }
        }.catch { emit(DataResourceResult.Failure(it)) }

}