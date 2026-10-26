package com.ahn.ggriggri.screen.ui.setting.viewmodel

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Group
import com.ahn.domain.model.User
import com.ahn.ggriggri.testutil.FakeGroupRepository
import com.ahn.ggriggri.testutil.FakeSessionManager
import com.ahn.ggriggri.testutil.FakeUserRepository
import com.ahn.ggriggri.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingGroupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val group = Group(
        groupDocumentId = "group-1",
        groupName = "우리 가족",
        groupCode = "ABC123",
        groupPw = "1234",
        groupUserDocumentID = listOf("user-1"),
    )
    private val user = User(userId = "user-1", userName = "안", userGroupDocumentId = "group-1")

    private lateinit var sessionManager: FakeSessionManager
    private lateinit var groupRepository: FakeGroupRepository
    private lateinit var userRepository: FakeUserRepository

    @Before
    fun setUp() {
        sessionManager = FakeSessionManager(initialUser = user)
        groupRepository = FakeGroupRepository().apply { groups[group.groupDocumentId] = group }
        userRepository = FakeUserRepository().apply { users[user.userId] = user }
    }

    private fun createViewModel() = SettingGroupViewModel(sessionManager, groupRepository, userRepository)

    // ---- 그룹 정보 로드 ----

    @Test
    fun `세션 사용자의 그룹 ID로 현재 그룹을 불러온다`() = runTest {
        // Given / When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(group, viewModel.currentGroup.value)
    }

    @Test
    fun `그룹 ID가 비어 있으면 그룹 미가입 오류를 보여준다`() = runTest {
        // Given
        sessionManager.userState.value = user.copy(userGroupDocumentId = "")

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals("그룹에 가입되지 않았습니다.", viewModel.errorMessage.value)
    }

    // ---- updateGroupName ----

    @Test
    fun `빈 그룹명은 거부하고 오류 메시지를 보여준다`() = runTest {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.updateGroupName("   ")
        advanceUntilIdle()

        // Then
        assertEquals("그룹명을 입력해주세요.", viewModel.errorMessage.value)
    }

    @Test
    fun `빈 그룹명이면 저장소에 쓰지 않는다`() = runTest {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.updateGroupName("")
        advanceUntilIdle()

        // Then
        assertTrue(groupRepository.updateCalls.isEmpty())
    }

    @Test
    fun `그룹명 변경에 성공하면 현재 그룹의 이름이 바뀐다`() = runTest {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.updateGroupName("새 이름")
        advanceUntilIdle()

        // Then
        assertEquals("새 이름", viewModel.currentGroup.value?.groupName)
    }

    @Test
    fun `그룹명 변경에 성공하면 성공 메시지를 보여준다`() = runTest {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.updateGroupName("새 이름")
        advanceUntilIdle()

        // Then
        assertEquals("그룹명이 성공적으로 변경되었습니다.", viewModel.successMessage.value)
    }

    @Test
    fun `그룹명 변경에 실패하면 기존 그룹명을 유지한다`() = runTest {
        // Given
        groupRepository.updateResult = DataResourceResult.Failure(RuntimeException("네트워크 오류"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.updateGroupName("새 이름")
        advanceUntilIdle()

        // Then
        assertEquals("우리 가족", viewModel.currentGroup.value?.groupName)
    }

    @Test
    fun `그룹명 변경에 실패하면 로딩이 끝난다`() = runTest {
        // Given
        groupRepository.updateResult = DataResourceResult.Failure(RuntimeException("네트워크 오류"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.updateGroupName("새 이름")
        advanceUntilIdle()

        // Then
        assertEquals(false, viewModel.isLoading.value)
    }

    // ---- updateGroupPassword ----

    @Test
    fun `현재와 같은 비밀번호로는 변경하지 않는다`() = runTest {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.updateGroupPassword("1234")
        advanceUntilIdle()

        // Then
        assertTrue(groupRepository.updateCalls.isEmpty())
    }

    // ---- leaveGroup ----

    @Test
    fun `그룹 나가기에 성공하면 안내 메시지 이벤트를 한 번 발행한다`() = runTest {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.leaveGroup()
        advanceUntilIdle()

        // Then
        assertEquals("그룹에서 성공적으로 나갔습니다.", viewModel.leaveGroupSuccess.first())
    }

    @Test
    fun `그룹 나가기에 성공하면 세션 사용자의 그룹 ID가 비워진다`() = runTest {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.leaveGroup()
        advanceUntilIdle()

        // Then
        assertEquals("", sessionManager.userState.value?.userGroupDocumentId)
    }

    @Test
    fun `그룹을 나간 뒤에는 그룹 미가입 오류를 띄우지 않는다`() = runTest {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.leaveGroup()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `그룹에서 사용자 제거에 실패하면 사용자 그룹 ID를 건드리지 않는다`() = runTest {
        // Given
        groupRepository.removeUserResult = DataResourceResult.Failure(RuntimeException("권한 없음"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.leaveGroup()
        advanceUntilIdle()

        // Then
        assertEquals("group-1", userRepository.users.getValue("user-1").userGroupDocumentId)
    }

    @Test
    fun `그룹에서 사용자 제거에 실패하면 오류 메시지를 보여준다`() = runTest {
        // Given
        groupRepository.removeUserResult = DataResourceResult.Failure(RuntimeException("권한 없음"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.leaveGroup()
        advanceUntilIdle()

        // Then
        assertEquals("그룹 나가기에 실패했습니다: 권한 없음", viewModel.errorMessage.value)
    }
}
