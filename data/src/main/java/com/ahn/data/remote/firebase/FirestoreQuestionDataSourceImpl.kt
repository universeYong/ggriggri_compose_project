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
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class FirestoreQuestionDataSourceImpl : QuestionDataSource {
    private val db = Firebase.firestore

    override suspend fun delete(questionId: String): DataResourceResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun create(question: Question): DataResourceResult<Question> = runCatching {
        val documentReference = db.collection("question_data")
            .add(question.toFirestoreQuestionDTO())
            .await()

        val createdQuestion = question.copy(questionId = documentReference.id)
        DataResourceResult.Success(createdQuestion)
    }.getOrElse { DataResourceResult.Failure(it) }

    override suspend fun update(question: Question): DataResourceResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun read(): DataResourceResult<List<Question>> {
        TODO("Not yet implemented")
    }

    override suspend fun getQuestionForGroupAndDate(
        groupId: String,
        dateTimestamp: Long,
    ): DataResourceResult<Question?> = runCatching {
        Log.d(
            "FirestoreQuestionDS",
            "Getting question for group: $groupId, dateTimestamp: $dateTimestamp"
        )
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
            Log.d(
                "FirestoreQuestionDS",
                "No question found for group: $groupId, dateTimestamp: $dateTimestamp"
            )
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

    override suspend fun getQuestionRecordById(documentId: String): DataResourceResult<Question?> =
        runCatching {
            Log.d("FirestoreQuestionDS_GetById", "Attempting to get QuestionRecord by ID: '$documentId'")
            val documentSnapshot = db.collection("question_data")
                .document(documentId)
                .get()
                .await()

            if (documentSnapshot.exists()) {
                Log.d("FirestoreQuestionDS_GetById", "Document exists. Data: ${documentSnapshot.data}")

                // Firestore 문서를 QuestionDTO로 변환
                val questionDTO = try {
                    documentSnapshot.toObject(QuestionDTO::class.java)
                } catch (e: Exception) {
                    Log.e("FirestoreQuestionDS_GetById", "Error converting document to QuestionDTO: ${e.message}", e)
                    null
                }

                if (questionDTO != null) {
                    // QuestionDTO를 Question (도메인 모델)으로 변환
                    val question = questionDTO.toDomainQuestion(documentSnapshot.id) // 매퍼 사용, 문서 ID 전달
                    Log.d("FirestoreQuestionDS_GetById", "Successfully converted DTO to Domain Question: $question. questionListDocumentId from Domain: '${question.questionListDocumentId}'")
                    DataResourceResult.Success(question)
                } else {
                    Log.w("FirestoreQuestionDS_GetById", "QuestionDTO was null after conversion. Document ID: ${documentSnapshot.id}")
                    DataResourceResult.Success(null) // DTO 변환 실패 시
                }
            } else {
                Log.w("FirestoreQuestionDS_GetById", "Document with ID '$documentId' does not exist in question_data.")
                DataResourceResult.Success(null)
            }
        }.getOrElse {
            Log.e("FirestoreQuestionDS_GetById", "Error getting QuestionRecord by ID: '$documentId'", it)
            DataResourceResult.Failure(it) }

}