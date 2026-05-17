package com.ahn.data.remote.firebase

import android.util.Log
import com.ahn.domain.common.DataResourceResult
import com.ahn.data.datasource.UserDataSource
import com.ahn.data.mapper.toDomainUser
import com.ahn.domain.model.User
import com.ahn.data.mapper.toDomainUserList
import com.ahn.data.mapper.toFirestoreUserDTO
import com.ahn.data.remote.dto.UserDTO
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class FirestoreUserDataSourceImpl @Inject constructor() : UserDataSource {
    override suspend fun delete(userId: String): DataResourceResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun create(user: User): DataResourceResult<Unit> = runCatching {
        Firebase.firestore.collection("user_data")
            .document(user.userId)
            .set(user.toFirestoreUserDTO(), SetOptions.merge())
            .await()
        DataResourceResult.Success(Unit)
    }.getOrElse {
        DataResourceResult.Failure(it)
    }

    override suspend fun update(user: User): DataResourceResult<Unit> = runCatching {
        Firebase.firestore.collection("user_data")
            .document(user.userId)
            .update(user.toFirestoreUserDTO())
            .await()
        DataResourceResult.Success(Unit)
    }.getOrElse {
        DataResourceResult.Failure(it)
    }

    override suspend fun read(): DataResourceResult<List<User>> = runCatching {
        val userSnapshot = Firebase.firestore.collection("user_data")
            .orderBy("_userId", Query.Direction.ASCENDING)
            .get()
            .await()
        val userDTOList = userSnapshot.toObjects(UserDTO::class.java)
        DataResourceResult.Success(userDTOList.toDomainUserList())
    }.getOrElse {
        DataResourceResult.Failure(it)
    }

    override suspend fun getUserById(userId: String): DataResourceResult<User?> = runCatching {
        Log.d("FirestoreDataSource", "Attempting to get user by ID: $userId")
        val documentSnapshot = Firebase.firestore.collection("user_data")
            .document(userId)
            .get()
            .await()
        if (documentSnapshot.exists()) {
            val userDTO = documentSnapshot.toObject(UserDTO::class.java)
            if (userDTO != null) {
                DataResourceResult.Success(userDTO.toDomainUser())
            } else {
                DataResourceResult.Failure(Exception())
            }
        } else {
            DataResourceResult.Success(null)
        }
    }.getOrElse {
        DataResourceResult.Failure(it)
    }

    override suspend fun getUserByIdSync(userId: String): DataResourceResult<User?> = runCatching {
        Log.d("FirestoreDataSource", "Attempting to get user by ID (sync): $userId")
        val documentSnapshot = Firebase.firestore.collection("user_data")
            .document(userId)
            .get()
            .await()
        if (documentSnapshot.exists()) {
            val userDTO = documentSnapshot.toObject(UserDTO::class.java)
            if (userDTO != null) {
                DataResourceResult.Success(userDTO.toDomainUser())
            } else {
                DataResourceResult.Failure(Exception())
            }
        } else {
            DataResourceResult.Success(null)
        }
    }.getOrElse {
        DataResourceResult.Failure(it)
    }

    override suspend fun getUserGroupDocumentId(userId: String): DataResourceResult<String?> =
        runCatching {
            val docSnapshot = Firebase.firestore.collection("user_data")
                .document(userId)
                .get()
                .await()
            val _docSnapshot = if (docSnapshot.exists()) {
                docSnapshot.getString("_userGroupDocumentId")
            } else null
            DataResourceResult.Success(_docSnapshot)
        }.getOrElse {
            DataResourceResult.Failure(it)
        }

    override suspend fun updateUserGroupDocumentId(
        userId: String,
        groupDocumentId: String,
    ): DataResourceResult<Unit> = runCatching{
        Firebase.firestore.collection("user_data")
            .document(userId)
            .set(mapOf("_userGroupDocumentId" to groupDocumentId), SetOptions.merge())
            .await()
        DataResourceResult.Success(Unit)
    }.getOrElse {
        DataResourceResult.Failure(it)
    }
}