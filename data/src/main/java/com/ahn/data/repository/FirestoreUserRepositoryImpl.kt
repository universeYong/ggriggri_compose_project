package com.ahn.data.repository

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.User
import com.ahn.domain.repository.UserRepository
import com.ahn.data.datasource.UserDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FirestoreUserRepositoryImpl @Inject constructor(val userDataSource: UserDataSource) : UserRepository {
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
    }

    override suspend fun updateUserGroupDocumentId(
        userId: String,
        groupDocumentId: String,
    ): Flow<DataResourceResult<Unit>> = flow{
        emit(DataResourceResult.Loading)
        emit(userDataSource.updateUserGroupDocumentId(userId,groupDocumentId))
    }

    override suspend fun getUserById(userId: String): Flow<DataResourceResult<User?>> = flow{
        emit(DataResourceResult.Loading)
        val result = userDataSource.getUserById(userId)
        emit(result)
    }.catch { exception ->
        emit(DataResourceResult.Failure(exception))
    }
}