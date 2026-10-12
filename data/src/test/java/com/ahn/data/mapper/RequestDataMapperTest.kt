package com.ahn.data.mapper

import com.ahn.data.remote.dto.RequestDTO
import com.ahn.domain.model.Request
import org.junit.Assert.assertEquals
import org.junit.Test

class RequestDataMapperTest {

    private val requestTime = 1_700_000_000_000L
    private val deadline = requestTime + 30 * 60 * 1000L

    private val dto = RequestDTO(
        _requestId = "dto-internal-id",
        _requestTime = requestTime,
        _requestUserDocumentID = "user-1",
        _requestMessage = "지금 뭐해?",
        _requestImage = "https://example.com/r.png",
        _requestGroupDocumentID = "group-1",
        _answerDeadline = deadline,
        _hasAnswer = true
    )

    private val request = Request(
        requestId = "request-1",
        requestTime = requestTime,
        requestUserDocumentID = "user-1",
        requestMessage = "지금 뭐해?",
        requestImage = "https://example.com/r.png",
        requestGroupDocumentID = "group-1",
        answerDeadline = deadline,
        hasAnswer = true
    )

    @Test
    fun `DTO를 도메인으로 변환하면 모든 필드가 그대로 옮겨진다`() {
        val result = dto.toDomainRequest("request-1")
        assertEquals(request, result)
    }

    @Test
    fun `도메인 requestId는 DTO의 _requestId가 아니라 문서 ID를 사용한다`() {
        val result = dto.toDomainRequest("doc-id")
        assertEquals("doc-id", result.requestId)
    }

    @Test
    fun `기본값 DTO는 기본값 도메인으로 변환된다`() {
        val result = RequestDTO().toDomainRequest("")
        assertEquals(Request(), result)
    }

    @Test
    fun `응답 마감 시각이 요청 시각과 함께 보존된다`() {
        val result = dto.toDomainRequest("request-1")
        assertEquals(30 * 60 * 1000L, result.answerDeadline - result.requestTime)
    }

    @Test
    fun `도메인을 Firestore 맵으로 변환하면 requestId를 포함한 언더스코어 키에 값이 담긴다`() {
        val result = request.toFirestoreRequestDTO()
        assertEquals(
            mapOf(
                "_requestId" to "request-1",
                "_requestTime" to requestTime,
                "_requestUserDocumentID" to "user-1",
                "_requestMessage" to "지금 뭐해?",
                "_requestImage" to "https://example.com/r.png",
                "_requestGroupDocumentID" to "group-1",
                "_answerDeadline" to deadline,
                "_hasAnswer" to true
            ),
            result
        )
    }

    @Test
    fun `도메인을 맵으로 저장했다가 DTO로 읽어 도메인으로 바꾸면 원본과 같다`() {
        val map = request.toFirestoreRequestDTO()
        val restoredDto = RequestDTO(
            _requestId = map["_requestId"] as String,
            _requestTime = map["_requestTime"] as Long,
            _requestUserDocumentID = map["_requestUserDocumentID"] as String,
            _requestMessage = map["_requestMessage"] as String,
            _requestImage = map["_requestImage"] as String,
            _requestGroupDocumentID = map["_requestGroupDocumentID"] as String,
            _answerDeadline = map["_answerDeadline"] as Long,
            _hasAnswer = map["_hasAnswer"] as Boolean
        )
        val result = restoredDto.toDomainRequest(request.requestId)
        assertEquals(request, result)
    }
}
