package com.ahn.data.mapper

import com.ahn.data.remote.dto.GroupDTO
import com.ahn.domain.model.Group
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class GroupDataMapperTest {

    private val dto = GroupDTO(
        _groupState = 2,
        _groupName = "끼리끼리",
        _groupCode = "ABC123",
        _groupPw = "pw1234",
        _groupUserDocumentID = listOf("user-1", "user-2"),
        _groupRequestDocumentID = listOf("req-1"),
        _groupQuestionDocumentID = listOf("q-1", "q-2", "q-3")
    )

    private val group = Group(
        groupDocumentId = "group-1",
        groupState = 2,
        groupName = "끼리끼리",
        groupCode = "ABC123",
        groupPw = "pw1234",
        groupUserDocumentID = listOf("user-1", "user-2"),
        groupRequestDocumentID = listOf("req-1"),
        groupQuestionDocumentID = listOf("q-1", "q-2", "q-3")
    )

    @Test
    fun `DTO를 도메인으로 변환하면 모든 필드가 그대로 옮겨진다`() {
        val result = dto.toDomainGroup("group-1")
        assertEquals(group, result)
    }

    @Test
    fun `도메인 groupDocumentId에는 전달한 문서 ID가 들어간다`() {
        val result = dto.toDomainGroup("doc-xyz")
        assertEquals("doc-xyz", result.groupDocumentId)
    }

    @Test
    fun `기본값 DTO는 활성 상태와 빈 리스트를 가진 기본값 도메인으로 변환된다`() {
        val result = GroupDTO().toDomainGroup("")
        assertEquals(Group(), result)
    }

    @Test
    fun `리스트 필드의 순서가 유지된다`() {
        val result = dto.toDomainGroup("group-1")
        assertEquals(listOf("q-1", "q-2", "q-3"), result.groupQuestionDocumentID)
    }

    @Test
    fun `도메인을 Firestore 맵으로 변환하면 언더스코어 키에 값이 담긴다`() {
        val result = group.toFirestoreGroupDTO()
        assertEquals(
            mapOf(
                "_groupState" to 2,
                "_groupName" to "끼리끼리",
                "_groupCode" to "ABC123",
                "_groupPw" to "pw1234",
                "_groupUserDocumentID" to listOf("user-1", "user-2"),
                "_groupRequestDocumentID" to listOf("req-1"),
                "_groupQuestionDocumentID" to listOf("q-1", "q-2", "q-3")
            ),
            result
        )
    }

    @Test
    fun `Firestore 맵에는 문서 ID가 저장되지 않는다`() {
        val result = group.toFirestoreGroupDTO()
        assertFalse(result.keys.any { it.contains("DocumentId") })
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `도메인을 맵으로 저장했다가 DTO로 읽어 도메인으로 바꾸면 원본과 같다`() {
        val map = group.toFirestoreGroupDTO()
        val restoredDto = GroupDTO(
            _groupState = map["_groupState"] as Int,
            _groupName = map["_groupName"] as String,
            _groupCode = map["_groupCode"] as String,
            _groupPw = map["_groupPw"] as String,
            _groupUserDocumentID = map["_groupUserDocumentID"] as List<String>,
            _groupRequestDocumentID = map["_groupRequestDocumentID"] as List<String>,
            _groupQuestionDocumentID = map["_groupQuestionDocumentID"] as List<String>
        )
        val result = restoredDto.toDomainGroup(group.groupDocumentId)
        assertEquals(group, result)
    }
}
