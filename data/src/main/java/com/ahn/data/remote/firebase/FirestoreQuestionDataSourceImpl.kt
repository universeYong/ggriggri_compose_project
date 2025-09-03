package com.ahn.data.remote.firebase

import android.util.Log
import com.ahn.data.datasource.QuestionDataSource
import com.ahn.data.mapper.toDomainQuestion
import com.ahn.data.mapper.toFirestoreQuestionDTO
import com.ahn.data.remote.dto.QuestionDTO
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Question
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class FirestoreQuestionDataSourceImpl: QuestionDataSource {
    private val db = Firebase.firestore

    override suspend fun delete(questionId: String): DataResourceResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun create(question: Question): DataResourceResult<Unit> = runCatching {
        Log.d(
            "FirestoreQuestionDS",
            "Creating question for group: ${question.questionGroupDocumentId}, listId: ${question.questionListDocumentId}"
        )
        db.collection("question_data")
            .add(question.toFirestoreQuestionDTO())
            .await()
        Log.d("FirestoreQuestionDS", "Successfully created question.")
        DataResourceResult.Success(Unit)
    }.getOrElse { exception ->
        Log.e("FirestoreQuestionDS", "Error creating question", exception)
        DataResourceResult.Failure(exception)
    }

    override suspend fun update(question: Question): DataResourceResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun read(): DataResourceResult<List<Question>> {
        TODO("Not yet implemented")
    }

    override suspend fun getQuestionForGroupAndDate(
        groupId: String,
        dateTimestamp: Long,
    ): DataResourceResult<Question?> = runCatching{
        Log.d("FirestoreQuestionDS", "Getting question for group: $groupId, dateTimestamp: $dateTimestamp")
        val nextDayTimestamp = dateTimestamp + (24 * 60 * 60 * 1000)

        val querySnapshot = db.collection("question_data")
            .whereEqualTo("_questionGroupDocumentId", groupId)
            .whereGreaterThanOrEqualTo("_questionCreatedTime", dateTimestamp)
            .whereLessThan("_questionCreatedTime", nextDayTimestamp)
            .orderBy("_questionCreatedTime")
            .limit(1)
            .get()
            .await()

        if (querySnapshot.isEmpty) {
            Log.d("FirestoreQuestionDS", "No question found for group: $groupId, dateTimestamp: $dateTimestamp")
            DataResourceResult.Success(null)
        } else {
            val document = querySnapshot.documents.first()
            val question = document.toObject(QuestionDTO::class.java)?.toDomainQuestion(document.id)
            Log.d("FirestoreQuestionDS", "Question found: $question")
            DataResourceResult.Success(question)
        }
    }.getOrElse { exception ->
        Log.e("FirestoreQuestionDS", "Error getting question for group and date", exception)
        DataResourceResult.Failure(exception)
    }
}