package com.ahn.ggriggri.testutil

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.User
import com.ahn.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** 메모리 Map에 사용자를 두는 UserRepository. 기본은 Loading 다음 Success를 흘린다. */
class FakeUserRepository : UserRepository {
    val users = mutableMapOf<String, User>()

    var updateGroupIdResult: DataResourceResult<Unit> = DataResourceResult.Success(Unit)

    override suspend fun updateUserGroupDocumentId(
        userId: String,
        groupDocumentId: String,
    ): Flow<DataResourceResult<Unit>> {
        if (updateGroupIdResult is DataResourceResult.Success) {
            users[userId]?.let { users[userId] = it.copy(userGroupDocumentId = groupDocumentId) }
        }
        return flowOf(DataResourceResult.Loading, updateGroupIdResult)
    }

    override suspend fun getUserById(userId: String): Flow<DataResourceResult<User?>> =
        flowOf(DataResourceResult.Loading, DataResourceResult.Success(users[userId]))

    /** getUserByIdSync로 조회된 userId 기록(중복 조회 검증용). */
    val getUserByIdSyncCalls = mutableListOf<String>()

    override suspend fun getUserByIdSync(userId: String): DataResourceResult<User?> {
        getUserByIdSyncCalls += userId
        return DataResourceResult.Success(users[userId])
    }

    override suspend fun create(userInfo: User): Flow<DataResourceResult<Unit>> = notUsed()
    override suspend fun read(): Flow<DataResourceResult<List<User>>> = notUsed()
    override suspend fun update(userInfo: User): Flow<DataResourceResult<Unit>> = notUsed()
    override suspend fun delete(userId: String): Flow<DataResourceResult<Unit>> = notUsed()
    override suspend fun getUserGroupDocumentId(userId: String): Flow<DataResourceResult<String?>> = notUsed()
    override suspend fun updateFcmToken(userId: String, token: String): Flow<DataResourceResult<Boolean>> = notUsed()
    override suspend fun removeFcmToken(userId: String, token: String): Flow<DataResourceResult<Boolean>> = notUsed()

    private fun notUsed(): Nothing = throw UnsupportedOperationException("이 테스트에서는 쓰지 않음")
}
