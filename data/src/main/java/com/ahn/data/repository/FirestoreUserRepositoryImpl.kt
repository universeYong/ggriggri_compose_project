package com.ahn.data.repository

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.User
import com.ahn.domain.repository.UserRepository
import com.ahn.data.datasource.UserDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class FirestoreUserRepositoryImpl(val targetDataSource: UserDataSource) : UserRepository {
    override suspend fun create(userInfo: User): Flow<DataResourceResult<Unit>> = flow {
        emit(DataResourceResult.Loading)
        emit(targetDataSource.create(userInfo))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun read(): Flow<DataResourceResult<List<User>>> = flow {
        emit(DataResourceResult.Loading)
        emit(targetDataSource.read())
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun update(userInfo: User): Flow<DataResourceResult<Unit>> = flow {
        emit(DataResourceResult.Loading)
        emit(targetDataSource.update(userInfo))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun delete(userId: String): Flow<DataResourceResult<Unit>> = flow {
        emit(DataResourceResult.Loading)
        emit(targetDataSource.delete(userId))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun getUserGroupDocumentId(userId: String):
            Flow<DataResourceResult<String?>> = flow {
        emit(DataResourceResult.Loading)
        emit(targetDataSource.getUserGroupDocumentId(userId))
    }

    override suspend fun updateUserGroupDocumentId(
        userId: String,
        groupDocumentId: String,
    ): Flow<DataResourceResult<Unit>> = flow{
        emit(DataResourceResult.Loading)
        emit(targetDataSource.updateUserGroupDocumentId(userId,groupDocumentId))
    }

}