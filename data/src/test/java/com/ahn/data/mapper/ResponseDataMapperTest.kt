package com.ahn.data.mapper

import com.ahn.data.remote.dto.ResponseDTO
import com.ahn.domain.model.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ResponseDataMapperTest {

    private val dto = ResponseDTO(
        _responseId = "dto-internal-id",
        _responseMessage = "회사에서 야근 중",
        _responseCreatedTime = 1_700_000_100_000L,
        _responseImage = "https://example.com/resp.png",
        _responseUserId = "user-2",
        _responseUserProfileImage = "https://example.com/profile.png"
    )

    private val response = Response(
        responseId = "response-1",
        responseTime = 1_700_000_100_000L,
        responseImage = "https://example.com/resp.png",
        responseMessage = "회사에서 야근 중",
        responseUserDocumentID = "user-2",
        responseUserProfileImage = "https://example.com/profile.png"
    )

    @Test
    fun `DTO를 도메인으로 변환하면 모든 필드가 그대로 옮겨진다`() {
        val result = dto.toDomainResponse("response-1")
        assertEquals(response, result)
    }

    @Test
    fun `DTO의 _responseCreatedTime이 도메인 responseTime으로 매핑된다`() {
        val result = dto.toDomainResponse("response-1")
        assertEquals(1_700_000_100_000L, result.responseTime)
    }

    @Test
    fun `DTO의 _responseUserId가 도메인 responseUserDocumentID로 매핑된다`() {
        val result = dto.toDomainResponse("response-1")
        assertEquals("user-2", result.responseUserDocumentID)
    }

    @Test
    fun `도메인 responseId는 DTO의 _responseId가 아니라 문서 ID를 사용한다`() {
        val result = dto.toDomainResponse("doc-id")
        assertEquals("doc-id", result.responseId)
    }

    @Test
    fun `기본값 DTO는 기본값 도메인으로 변환된다`() {
        val result = ResponseDTO().toDomainResponse("")
        assertEquals(Response(), result)
    }

    @Test
    fun `도메인을 Firestore 맵으로 변환하면 DTO 필드명 키에 값이 담긴다`() {
        val result = response.toFirestoreResponseDTO()
        assertEquals(
            mapOf(
                "_responseMessage" to "회사에서 야근 중",
                "_responseCreatedTime" to 1_700_000_100_000L,
                "_responseImage" to "https://example.com/resp.png",
                "_responseUserId" to "user-2",
                "_responseUserProfileImage" to "https://example.com/profile.png"
            ),
            result
        )
    }

    @Test
    fun `Firestore 맵에는 DocumentId 필드가 저장되지 않는다`() {
        val result = response.toFirestoreResponseDTO()
        assertFalse(result.containsKey("_responseId"))
    }

    @Test
    fun `도메인을 맵으로 저장했다가 DTO로 읽어 도메인으로 바꾸면 원본과 같다`() {
        val map = response.toFirestoreResponseDTO()
        val restoredDto = ResponseDTO(
            _responseId = response.responseId,
            _responseMessage = map["_responseMessage"] as String,
            _responseCreatedTime = map["_responseCreatedTime"] as Long,
            _responseImage = map["_responseImage"] as String,
            _responseUserId = map["_responseUserId"] as String,
            _responseUserProfileImage = map["_responseUserProfileImage"] as String
        )
        val result = restoredDto.toDomainResponse(response.responseId)
        assertEquals(response, result)
    }
}
