package com.ahn.data.mapper

import com.ahn.data.remote.dto.QuestionDTO
import com.ahn.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** 대상: QusetionDataMapper.kt (파일명 오타는 프로덕션 그대로 둠) */
class QuestionDataMapperTest {

    private val dto = QuestionDTO(
        _questionId = "dto-internal-id",
        _questionCreatedTime = 1_700_000_000_000L,
        _questionGroupDocumentId = "group-1",
        _questionListDocumentId = "qlist-7"
    )

    private val question = Question(
        questionId = "question-1",
        questionCreatedTime = 1_700_000_000_000L,
        questionGroupDocumentId = "group-1",
        questionListDocumentId = "qlist-7"
    )

    @Test
    fun `DTO를 도메인으로 변환하면 모든 필드가 그대로 옮겨진다`() {
        val result = dto.toDomainQuestion("question-1")
        assertEquals(question, result)
    }

    @Test
    fun `도메인 questionId는 DTO의 _questionId가 아니라 문서 ID를 사용한다`() {
        val result = dto.toDomainQuestion("doc-id")
        assertEquals("doc-id", result.questionId)
    }

    @Test
    fun `기본값 DTO는 기본값 도메인으로 변환된다`() {
        val result = QuestionDTO().toDomainQuestion("")
        assertEquals(Question(), result)
    }

    @Test
    fun `도메인을 Firestore 맵으로 변환하면 언더스코어 키에 값이 담긴다`() {
        val result = question.toFirestoreQuestionDTO()
        assertEquals(
            mapOf(
                "_questionCreatedTime" to 1_700_000_000_000L,
                "_questionGroupDocumentId" to "group-1",
                "_questionListDocumentId" to "qlist-7"
            ),
            result
        )
    }

    @Test
    fun `Firestore 맵에는 문서 ID가 저장되지 않는다`() {
        val result = question.toFirestoreQuestionDTO()
        assertFalse(result.containsKey("_questionId"))
    }

    @Test
    fun `도메인을 맵으로 저장했다가 DTO로 읽어 도메인으로 바꾸면 원본과 같다`() {
        val map = question.toFirestoreQuestionDTO()
        val restoredDto = QuestionDTO(
            _questionCreatedTime = map["_questionCreatedTime"] as Long,
            _questionGroupDocumentId = map["_questionGroupDocumentId"] as String,
            _questionListDocumentId = map["_questionListDocumentId"] as String
        )
        val result = restoredDto.toDomainQuestion(question.questionId)
        assertEquals(question, result)
    }
}
