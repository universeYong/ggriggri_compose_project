package com.ahn.data.datasource

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Group

interface GroupDataSource {
    suspend fun delete(groupId: String): DataResourceResult<Unit>
    suspend fun create(group: Group): DataResourceResult<String>
    suspend fun update(group: Group): DataResourceResult<Unit>
    suspend fun read(): DataResourceResult<List<Group>>
}