package com.ahn.data.remote.firebase

import android.provider.ContactsContract
import android.util.Log
import com.ahn.data.datasource.GroupDataSource
import com.ahn.data.mapper.toDomainGroup
import com.ahn.data.mapper.toFirestoreGroupDTO
import com.ahn.data.remote.dto.GroupDTO
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Group
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
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

    override suspend fun getGroupByCode(groupCode: String): DataResourceResult<Group?> = runCatching{
        val querySnapShot = Firebase.firestore.collection("group_data")
            .whereEqualTo("_groupCode", groupCode)
            .limit(1)
            .get()
            .await()
        if (querySnapShot.isEmpty) {
            DataResourceResult.Success(null)
        } else {
            val document = querySnapShot.documents.first()
            val groupDTO = document.toObject(GroupDTO::class.java)

            if (groupDTO != null) {
                val domainGroup = groupDTO.toDomainGroup(document.id)
                DataResourceResult.Success(domainGroup)
            } else {
                Log.w("FirestoreGroupDS", "Document ${document.id} data could not be converted to GroupDTO")
                DataResourceResult.Failure(Exception("Failed to convert document to GroupDTO"))
            }
        }
    }.getOrElse(){
        Log.e("FirestoreGroupDS", "Error in getGroupByCode for code: $groupCode", it)
        DataResourceResult.Failure(it)
    }

    override suspend fun addUserToGroup(
        groupId: String,
        userId: String,
    ): DataResourceResult<Unit> = runCatching{
        val groupRef = Firebase.firestore.collection("group_data").document(groupId)
        groupRef.update("_groupUserDocumentID", FieldValue.arrayUnion(userId)).await()
        DataResourceResult.Success(Unit)
    }.getOrElse {
        Log.e("FirestoreGroupDS", "Error in addUserToGroup for group: $groupId, user: $userId", it)
        DataResourceResult.Failure(it)
    }

    override suspend fun isGroupCodeExist(groupCode: String): DataResourceResult<Boolean> = runCatching{
        val querySnapshot = Firebase.firestore.collection("group_data")
            .whereEqualTo("_groupCode", groupCode)
            .limit(1)
            .get()
            .await()
        DataResourceResult.Success(!querySnapshot.isEmpty)
    }.getOrElse {
        DataResourceResult.Failure(it)
    }

    // 홈화면에서 그룹원 목록 가져오기
    override suspend fun getGroupMembers(groupId: String): DataResourceResult<List<String>> = runCatching{
        val documentSnapshot = Firebase.firestore
            .collection("group_data")
            .document(groupId)
            .get()
            .await()
        if (documentSnapshot.exists()) {
            @Suppress("UNCHECKED_CAST")
            val userDocumentIds = documentSnapshot.get("_groupUserDocumentID") as? List<String>

            if (userDocumentIds != null) {
                DataResourceResult.Success(userDocumentIds)
            } else {
                DataResourceResult.Failure(Exception("User document IDs not found in the document"))
            }
        } else {
            DataResourceResult.Failure(Exception("Group not found with ID: $groupId"))
        }
    }.getOrElse {
        DataResourceResult.Failure(it)
    }
}