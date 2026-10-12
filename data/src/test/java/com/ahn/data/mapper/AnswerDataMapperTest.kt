package com.ahn.data.mapper

import com.ahn.data.remote.dto.AnswerDTO
import com.ahn.domain.model.Answer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AnswerDataMapperTest {

    private val dto = AnswerDTO(
        _answerID = "dto-internal-id",
        _answerMessage = "오늘 저녁은 김치찌개",
        _answerResponseTime = 1_700_000_000_000L,
        _answerRequestState = 2,
        _answerUserDocumentID = "user-1"
    )

    private val answer = Answer(
        answerId = "answer-1",
        answerMessage = "오늘 저녁은 김치찌개",
        answerResponseTime = 1_700_000_000_000L,
        answerRequestState = 2,
        answerUserDocumentID = "user-1"
    )

    @Test
    fun `DTO를 도메인으로 변환하면 모든 필드가 그대로 옮겨진다`() {
        // Given dto, When
        val result = dto.toDomainAnswer("answer-1")
        // Then
        assertEquals(answer, result)
    }

    @Test
    fun `도메인 answerId는 DTO의 _answerID가 아니라 문서 ID를 사용한다`() {
        val result = dto.toDomainAnswer("doc-id")
        assertEquals("doc-id", result.answerId)
    }

    @Test
    fun `기본값 DTO는 기본값 도메인으로 변환된다`() {
        val result = AnswerDTO().toDomainAnswer("")
        assertEquals(Answer(), result)
    }

    @Test
    fun `도메인을 Firestore 맵으로 변환하면 언더스코어 키에 값이 담긴다`() {
        val result = answer.toFirestoreAnswerDTO()
        assertEquals(
            mapOf(
                "_answerMessage" to "오늘 저녁은 김치찌개",
                "_answerResponseTime" to 1_700_000_000_000L,
                "_answerRequestState" to 2,
                "_answerUserDocumentID" to "user-1"
            ),
            result
        )
    }

    @Test
    fun `Firestore 맵에는 문서 ID가 저장되지 않는다`() {
        val result = answer.toFirestoreAnswerDTO()
        assertFalse(result.containsKey("_answerID"))
    }

    @Test
    fun `도메인을 맵으로 저장했다가 DTO로 읽어 도메인으로 바꾸면 원본과 같다`() {
        // Given: Firestore 저장/역직렬화를 맵 -> DTO 생성으로 흉내낸다
        val map = answer.toFirestoreAnswerDTO()
        val restoredDto = AnswerDTO(
            _answerMessage = map["_answerMessage"] as String,
            _answerResponseTime = map["_answerResponseTime"] as Long,
            _answerRequestState = map["_answerRequestState"] as Int,
            _answerUserDocumentID = map["_answerUserDocumentID"] as String
        )
        // When
        val result = restoredDto.toDomainAnswer(answer.answerId)
        // Then
        assertEquals(answer, result)
    }
}
