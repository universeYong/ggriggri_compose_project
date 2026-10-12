package com.ahn.data.mapper

import com.ahn.data.remote.dto.QuestionListDTO
import com.ahn.domain.model.QuestionList
import org.junit.Assert.assertEquals
import org.junit.Test

class QuestionListDataMapperTest {

    private val dto = QuestionListDTO(
        _color = "#FFAA00",
        _content = "가장 기억에 남는 여행지는?",
        _imgUrl = "https://example.com/q.png",
        _number = 7
    )

    private val questionList = QuestionList(
        color = "#FFAA00",
        content = "가장 기억에 남는 여행지는?",
        imgUrl = "https://example.com/q.png",
        number = 7
    )

    @Test
    fun `DTO를 도메인으로 변환하면 모든 필드가 그대로 옮겨진다`() {
        val result = dto.toDomainQuestionList()
        assertEquals(questionList, result)
    }

    @Test
    fun `기본값 DTO는 기본값 도메인으로 변환된다`() {
        val result = QuestionListDTO().toDomainQuestionList()
        assertEquals(QuestionList(), result)
    }

    @Test
    fun `도메인을 Firestore 맵으로 변환하면 언더스코어 키에 값이 담긴다`() {
        val result = questionList.toFirestoreQuestionListDTO()
        assertEquals(
            mapOf(
                "_color" to "#FFAA00",
                "_content" to "가장 기억에 남는 여행지는?",
                "_imgUrl" to "https://example.com/q.png",
                "_number" to 7
            ),
            result
        )
    }

    @Test
    fun `도메인을 맵으로 저장했다가 DTO로 읽어 도메인으로 바꾸면 원본과 같다`() {
        val map = questionList.toFirestoreQuestionListDTO()
        val restoredDto = QuestionListDTO(
            _color = map["_color"] as String,
            _content = map["_content"] as String,
            _imgUrl = map["_imgUrl"] as String,
            _number = map["_number"] as Int
        )
        val result = restoredDto.toDomainQuestionList()
        assertEquals(questionList, result)
    }
}
