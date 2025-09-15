package com.ahn.data.remote.firebase

import android.util.Log
import com.ahn.data.datasource.AnswerDataSource
import com.ahn.data.mapper.toDomainAnswer
import com.ahn.data.mapper.toFirestoreAnswerDTO
import com.ahn.data.remote.dto.AnswerDTO
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Answer
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class FirestoreAnswerDataSourceImpl @Inject constructor(): AnswerDataSource {

    private val db = Firebase.firestore
    override suspend fun delete(
        questionId: String,
        answerId: String,
    ): DataResourceResult<Unit> = runCatching {
        db.collection("question_data").document(questionId)
            .collection("answer_data").document(answerId)
            .delete()
            .await()
        DataResourceResult.Success(Unit)
    }.getOrElse { DataResourceResult.Failure(it) }

    override suspend fun create(
        questionId: String,
        answer: Answer,
    ): DataResourceResult<String?> = runCatching {
        val documentReference = db.collection("question_data").document(questionId)
                .collection("answer_data")
                .add(answer.toFirestoreAnswerDTO())
                .await()
        DataResourceResult.Success(documentReference.id)
    }.getOrElse { DataResourceResult.Failure(it) }

    override suspend fun update(
        questionId: String,
        answer: Answer,
    ): DataResourceResult<Unit> = runCatching {
        if (answer.answerId.isNullOrEmpty()) {
            return DataResourceResult.Failure(
                IllegalArgumentException(
                    "Answer ID cannot be null or empty for update."
                )
            )
        }
        db.collection("question_data").document(questionId)
            .collection("answer_data").document(answer.answerId)
            .set(answer.toFirestoreAnswerDTO(), SetOptions.merge()) // merge 옵션으로 부분 업데이트 가능
            .await()
        DataResourceResult.Success(Unit)
    }.getOrElse { DataResourceResult.Failure(it) }

    override suspend fun read(questionId: String): DataResourceResult<List<Answer>> = runCatching {
        val querySnapshot =
            db.collection("question_data").document(questionId)
            .collection("answer_data")
            .get()
            .await()

        val answers = querySnapshot.documents.mapNotNull { document ->
            // Firestore 문서를 AnswerDTO로 변환 후 도메인 모델로 매핑
            document.toObject(AnswerDTO::class.java)?.toDomainAnswer(document.id)
        }
        DataResourceResult.Success(answers)
    }.getOrElse { DataResourceResult.Failure(it) }

    override suspend fun getAllAnswersForQuestion(questionDataDocumentId: String): DataResourceResult<List<Answer>> = runCatching {
            val querySnapshot = db.collection("question_data").document(questionDataDocumentId)
                .collection("answer_data")
                .orderBy("_answerResponseTime", Query.Direction.ASCENDING)
                .get()
                .await()

            val answers = querySnapshot.documents.mapNotNull { document ->
                try {
                    // Firestore 문서를 AnswerDTO로 변환 후 도메인 모델로 매핑
                    document.toObject(AnswerDTO::class.java)?.toDomainAnswer(document.id) // DTO 및 매퍼 사용 가정
                } catch (e: Exception) {
                    Log.e("FirestoreAnswerDS", "Error converting answer document: ${document.id}", e)
                    null
                }
            }
            DataResourceResult.Success(answers)
        }.getOrElse {
            Log.e("FirestoreAnswerDS", "Error in getAllAnswersForQuestion for $questionDataDocumentId", it)
            DataResourceResult.Failure(it)
    }
}