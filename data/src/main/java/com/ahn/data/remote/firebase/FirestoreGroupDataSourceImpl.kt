package com.ahn.data.remote.firebase

import com.ahn.data.datasource.GroupDataSource
import com.ahn.data.mapper.toFirestoreGroupDTO
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Group
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class FirestoreGroupDataSourceImpl: GroupDataSource {
    override suspend fun delete(groupId: String): DataResourceResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun create(group: Group): DataResourceResult<String> = runCatching{
        val decRef = Firebase.firestore.collection("group_data")
            .add(group.toFirestoreGroupDTO())
            .await()
        DataResourceResult.Success(decRef.id)
    }.getOrElse {
        DataResourceResult.Failure(it)
    }

    override suspend fun update(group: Group): DataResourceResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun read(): DataResourceResult<List<Group>> {
        TODO("Not yet implemented")
    }
}