package com.ahn.data.remote.firebase

import android.util.Log
import com.ahn.data.datasource.QuestionDataSource
import com.ahn.data.mapper.toDomainQuestion
import com.ahn.data.mapper.toFirestoreQuestionDTO
import com.ahn.data.remote.dto.QuestionDTO
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Question
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class FirestoreQuestionDataSourceImpl @Inject constructor(): QuestionDataSource {
    private val db = Firebase.firestore

    override suspend fun delete(questionId: String): DataResourceResult<Unit> {
        TODO("Not yet implemented")
    }

    // 문서 ID를 "{groupId}_{yyyyMMdd}"로 고정하고 트랜잭션으로 "없을 때만" 만든다.
    // 그룹원 여러 명(또는 Worker)이 동시에 만들려고 해도 문서는 하나만 생기므로
    // onDocumentCreated 트리거(일일 질문 푸시)도 한 번만 실행된다.
    // 이미 있으면 새로 만들지 않고 기존 문서를 돌려준다.
    override suspend fun create(question: Question): DataResourceResult<Question> = runCatching {
        val documentId = dailyQuestionDocumentId(
            question.questionGroupDocumentId,
            question.questionCreatedTime
        )
        val documentReference = db.collection("question_data").document(documentId)

        val existingOrCreated = db.runTransaction { transaction ->
            val snapshot = transaction.get(documentReference)
            val existing = if (snapshot.exists()) {
                snapshot.toObject(QuestionDTO::class.java)?.toDomainQuestion(snapshot.id)
            } else {
                null
            }
            if (existing != null) {
                existing
            } else {
                transaction.set(documentReference, question.toFirestoreQuestionDTO())
                question.copy(questionId = documentId)
            }
        }.await()

        DataResourceResult.Success(existingOrCreated)
    }.getOrElse { DataResourceResult.Failure(it) }

    private fun dailyQuestionDocumentId(groupId: String, createdTime: Long): String {
        val date = Instant.ofEpochMilli(createdTime)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.BASIC_ISO_DATE) // yyyyMMdd
        return "${groupId}_$date"
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
                    DataResourceResult.Success(null)
                }
            } else {
                Log.w("FirestoreQuestionDS_GetById", "Document with ID '$documentId' does not exist in question_data.")
                DataResourceResult.Success(null)
            }
        }.getOrElse {
            Log.e("FirestoreQuestionDS_GetById", "Error getting QuestionRecord by ID: '$documentId'", it)
            DataResourceResult.Failure(it) }

    override suspend fun getAllQuestionsForGroup(groupId: String): DataResourceResult<List<Question>> = runCatching{
            val querySnapshot = db.collection("question_data")
                .whereEqualTo("_questionGroupDocumentId", groupId)
                .orderBy("_questionCreatedTime", Query.Direction.DESCENDING)
                .get()
                .await()

            val questions = querySnapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(QuestionDTO::class.java)?.toDomainQuestion(document.id)
                } catch (e: Exception) {
                    Log.e("FirestoreQuestionDS", "Error converting document in getAllQuestionsForGroup: ${document.id}", e)
                    null
                }
            }
            DataResourceResult.Success(questions)
        }.getOrElse { DataResourceResult.Failure(it) }

}