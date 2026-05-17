package com.ahn.data.datasource

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Group

interface GroupDataSource {
    suspend fun delete(groupId: String): DataResourceResult<Unit>
    suspend fun create(group: Group): DataResourceResult<String>
    suspend fun update(group: Group): DataResourceResult<Unit>
    suspend fun read(): DataResourceResult<List<Group>>
    suspend fun getGroupByCode(groupCode: String): DataResourceResult<Group?>
    suspend fun getGroupById(groupId: String): DataResourceResult<Group?>
    suspend fun addUserToGroup(groupId: String, userId: String): DataResourceResult<Unit>
    suspend fun removeUserFromGroup(groupId: String, userId: String): DataResourceResult<Unit>
    // 그룹코드 중복 체크
    suspend fun isGroupCodeExist(groupCode: String): DataResourceResult<Boolean>
    suspend fun getGroupMembers(groupId: String): DataResourceResult<List<String>>
}