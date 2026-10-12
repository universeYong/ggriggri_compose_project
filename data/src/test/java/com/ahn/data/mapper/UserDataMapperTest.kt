package com.ahn.data.mapper

import com.ahn.data.remote.dto.UserDTO
import com.ahn.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserDataMapperTest {

    private fun dto(id: String = "kakao-1") = UserDTO(
        _userId = id,
        _userName = "안끼리",
        _userState = 2,
        _userJoinTime = 1_690_000_000_000L,
        _userFcmCode = mutableListOf("token-a", "token-b"),
        _userProfileImage = "https://example.com/p.png",
        _userGroupDocumentId = "group-1",
        _userAutoLoginToken = "auto-token"
    )

    private fun user(id: String = "kakao-1") = User(
        userId = id,
        userName = "안끼리",
        userState = 2,
        userJoinTime = 1_690_000_000_000L,
        userFcmCode = mutableListOf("token-a", "token-b"),
        userProfileImage = "https://example.com/p.png",
        userGroupDocumentId = "group-1",
        userAutoLoginToken = "auto-token"
    )

    @Test
    fun `DTO를 도메인으로 변환하면 모든 필드가 그대로 옮겨진다`() {
        val result = dto().toDomainUser()
        assertEquals(user(), result)
    }

    @Test
    fun `기본값 DTO는 기본값 도메인으로 변환된다`() {
        val result = UserDTO().toDomainUser()
        assertEquals(User(), result)
    }

    @Test
    fun `FCM 토큰 목록의 순서가 유지된다`() {
        val result = dto().toDomainUser()
        assertEquals(listOf("token-a", "token-b"), result.userFcmCode)
    }

    @Test
    fun `도메인을 Firestore 맵으로 변환하면 언더스코어 키에 값이 담긴다`() {
        val result = user().toFirestoreUserDTO()
        assertEquals(
            mapOf(
                "_userId" to "kakao-1",
                "_userName" to "안끼리",
                "_userState" to 2,
                "_userJoinTime" to 1_690_000_000_000L,
                "_userFcmCode" to listOf("token-a", "token-b"),
                "_userProfileImage" to "https://example.com/p.png",
                "_userGroupDocumentId" to "group-1",
                "_userAutoLoginToken" to "auto-token"
            ),
            result
        )
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `도메인을 맵으로 저장했다가 DTO로 읽어 도메인으로 바꾸면 원본과 같다`() {
        val map = user().toFirestoreUserDTO()
        val restoredDto = UserDTO(
            _userId = map["_userId"] as String,
            _userName = map["_userName"] as String,
            _userState = map["_userState"] as Int,
            _userJoinTime = map["_userJoinTime"] as Long,
            _userFcmCode = (map["_userFcmCode"] as List<String>).toMutableList(),
            _userProfileImage = map["_userProfileImage"] as String,
            _userGroupDocumentId = map["_userGroupDocumentId"] as String,
            _userAutoLoginToken = map["_userAutoLoginToken"] as String
        )
        val result = restoredDto.toDomainUser()
        assertEquals(user(), result)
    }

    @Test
    fun `DTO 리스트를 도메인 리스트로 변환하면 순서와 개수가 유지된다`() {
        val result = listOf(dto("a"), dto("b"), dto("c")).toDomainUserList()
        assertEquals(listOf(user("a"), user("b"), user("c")), result)
    }

    @Test
    fun `빈 DTO 리스트는 빈 도메인 리스트로 변환된다`() {
        val result = emptyList<UserDTO>().toDomainUserList()
        assertTrue(result.isEmpty())
    }
}
