package com.ahn.ggriggri.testutil

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Group
import com.ahn.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** 각 결과를 테스트에서 지정할 수 있는 GroupRepository. 기본은 Loading 다음 Success를 흘린다. */
class FakeGroupRepository : GroupRepository {
    val groups = mutableMapOf<String, Group>()

    var updateResult: DataResourceResult<Unit> = DataResourceResult.Success(Unit)
    var removeUserResult: DataResourceResult<Unit> = DataResourceResult.Success(Unit)

    val updateCalls = mutableListOf<Group>()
    val removeUserCalls = mutableListOf<Pair<String, String>>()

    override suspend fun getGroupById(groupId: String): Flow<DataResourceResult<Group?>> =
        flowOf(DataResourceResult.Loading, DataResourceResult.Success(groups[groupId]))

    override suspend fun update(groupInfo: Group): Flow<DataResourceResult<Unit>> {
        updateCalls += groupInfo
        return flowOf(DataResourceResult.Loading, updateResult)
    }

    override suspend fun removeUserFromGroup(groupId: String, userId: String): Flow<DataResourceResult<Unit>> {
        removeUserCalls += groupId to userId
        return flowOf(DataResourceResult.Loading, removeUserResult)
    }

    override suspend fun create(groupInfo: Group): Flow<DataResourceResult<String>> = notUsed()
    override suspend fun read(): Flow<DataResourceResult<List<Group>>> = notUsed()
    override suspend fun delete(groupId: String): Flow<DataResourceResult<Unit>> = notUsed()
    override suspend fun getGroupByCode(groupCode: String): Flow<DataResourceResult<Group?>> = notUsed()
    override suspend fun addUserToGroup(groupId: String, userId: String): Flow<DataResourceResult<Unit>> = notUsed()
    override suspend fun isGroupCodeExist(groupCode: String): Flow<DataResourceResult<Boolean>> = notUsed()
    override suspend fun getGroupMembers(groupId: String): Flow<DataResourceResult<List<String>>> = notUsed()

    private fun notUsed(): Nothing = throw UnsupportedOperationException("이 테스트에서는 쓰지 않음")
}
