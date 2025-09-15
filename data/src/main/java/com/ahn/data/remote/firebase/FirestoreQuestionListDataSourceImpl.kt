package com.ahn.data.remote.firebase

import android.util.Log
import com.ahn.data.datasource.QuestionListDataSource
import com.ahn.data.mapper.toDomainQuestionList
import com.ahn.data.remote.dto.QuestionListDTO
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.QuestionList
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class FirestoreQuestionListDataSourceImpl @Inject constructor(): QuestionListDataSource {

    private val db = Firebase.firestore

    override suspend fun delete(questionId: String): DataResourceResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun create(question: QuestionList): DataResourceResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun update(question: QuestionList): DataResourceResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun read(): DataResourceResult<List<QuestionList>> = runCatching {
        Log.d("FirestoreQuestionListDS", "Reading all question lists")
        val querySnapshot = db.collection("questionlist_data")
            .orderBy("_number")
            .get()
            .await()

        val questionLists = querySnapshot.documents.mapNotNull { document ->
            document.toObject(QuestionListDTO::class.java)?.toDomainQuestionList()
        }
        Log.d("FirestoreQuestionListDS", "Successfully read ${questionLists.size} question lists.")
        DataResourceResult.Success(questionLists)
    }.getOrElse { exception ->
        Log.e("FirestoreQuestionListDS", "Error reading question lists", exception)
        DataResourceResult.Failure(exception)
    }
}