package com.ahn.ggriggri.screen.ui.archive.viewmodel.requestdetail

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Request
import com.ahn.domain.model.Response
import com.ahn.domain.model.User
import com.ahn.ggriggri.testutil.FakeRequestRepository
import com.ahn.ggriggri.testutil.FakeResponseRepository
import com.ahn.ggriggri.testutil.FakeSessionManager
import com.ahn.ggriggri.testutil.FakeUserRepository
import com.ahn.ggriggri.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RequestDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val me = User(userId = "user-1", userName = "안", userProfileImage = "me.png", userGroupDocumentId = "group-1")
    private val other = User(userId = "user-2", userName = "김", userProfileImage = "", userGroupDocumentId = "group-1")

    private val request = Request(
        requestId = "req-1",
        requestTime = 1_700_000_000_000L,
        requestUserDocumentID = me.userId,
        requestMessage = "오늘 뭐 먹었어?",
        requestGroupDocumentID = "group-1",
    )

    private lateinit var sessionManager: FakeSessionManager
    private lateinit var requestRepository: FakeRequestRepository
    private lateinit var userRepository: FakeUserRepository
    private lateinit var responseRepository: FakeResponseRepository

    @Before
    fun setUp() {
        sessionManager = FakeSessionManager(initialUser = me)
        requestRepository = FakeRequestRepository().apply { put(request) }
        userRepository = FakeUserRepository().apply {
            users[me.userId] = me
            users[other.userId] = other
        }
        responseRepository = FakeResponseRepository()
    }

    private fun createViewModel() =
        RequestDetailViewModel(sessionManager, requestRepository, userRepository, responseRepository)

    @Test
    fun `요청 본문과 작성자, 응답 목록을 하나의 UiState로 내보낸다`() = runTest {
        // Given
        responseRepository.responses[request.requestId] = listOf(
            Response(responseId = "res-1", responseTime = 1L, responseMessage = "김밥", responseUserDocumentID = other.userId),
            Response(responseId = "res-2", responseTime = 2L, responseMessage = "라면", responseUserDocumentID = "ghost"),
        )
        val viewModel = createViewModel()

        // When
        viewModel.loadRequest(request.requestId)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        val item = state.request!!
        assertEquals("오늘 뭐 먹었어?", item.message)
        assertEquals("안", item.authorName)
        assertEquals("me.png", item.authorProfileImage)
        assertEquals(RequestDetailViewModel.formatTime(request.requestTime), item.timeText)

        assertEquals(listOf("res-1", "res-2"), state.responses.map { it.key })
        assertEquals("김", state.responses[0].authorName)
        // 없는 사용자는 기본 이름으로 표시
        assertEquals(RequestDetailViewModel.UNKNOWN_USER_NAME, state.responses[1].authorName)
        assertEquals(listOf(request.requestId), requestRepository.getRequestByIdCalls)
    }

    @Test
    fun `같은 사용자는 한 번만 조회한다`() = runTest {
        // Given: 요청 작성자(me)도 응답하고, other가 두 번 응답
        responseRepository.responses[request.requestId] = listOf(
            Response(responseId = "a", responseUserDocumentID = me.userId),
            Response(responseId = "b", responseUserDocumentID = other.userId),
            Response(responseId = "c", responseUserDocumentID = other.userId),
        )
        val viewModel = createViewModel()

        // When
        viewModel.loadRequest(request.requestId)
        advanceUntilIdle()

        // Then
        assertEquals(3, viewModel.uiState.value.responses.size)
        assertEquals(userRepository.getUserByIdSyncCalls.distinct(), userRepository.getUserByIdSyncCalls)
        assertEquals(setOf(me.userId, other.userId), userRepository.getUserByIdSyncCalls.toSet())
    }

    @Test
    fun `다른 그룹의 요청은 찾을 수 없음으로 처리한다`() = runTest {
        // Given
        requestRepository.put(request.copy(requestGroupDocumentID = "group-other"))
        val viewModel = createViewModel()

        // When
        viewModel.loadRequest(request.requestId)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertNull(state.request)
        assertEquals(RequestDetailViewModel.NOT_FOUND_MESSAGE, state.error)
    }

    @Test
    fun `없는 요청이면 찾을 수 없음 에러`() = runTest {
        val viewModel = createViewModel()

        viewModel.loadRequest("missing")
        advanceUntilIdle()

        assertEquals(RequestDetailViewModel.NOT_FOUND_MESSAGE, viewModel.uiState.value.error)
    }

    @Test
    fun `요청 조회 실패는 에러 메시지로 반영한다`() = runTest {
        requestRepository.resultFlow(request.requestId).value =
            DataResourceResult.Failure(IllegalStateException("네트워크 오류"))
        val viewModel = createViewModel()

        viewModel.loadRequest(request.requestId)
        advanceUntilIdle()

        assertEquals("네트워크 오류", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `그룹 정보가 없으면 에러`() = runTest {
        sessionManager.userState.value = me.copy(userGroupDocumentId = "")
        val viewModel = createViewModel()

        viewModel.loadRequest(request.requestId)
        advanceUntilIdle()

        assertEquals("그룹 정보를 찾을 수 없습니다.", viewModel.uiState.value.error)
        assertTrue(requestRepository.getRequestByIdCalls.isEmpty())
    }

    @Test
    fun `응답 조회가 실패해도 요청은 보이고 응답은 빈 목록`() = runTest {
        responseRepository.readFailure = RuntimeException("응답 실패")
        val viewModel = createViewModel()

        viewModel.loadRequest(request.requestId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(request.requestMessage, state.request?.message)
        assertTrue(state.responses.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `요청 문서가 바뀌면 실시간으로 반영하고 작성자는 다시 조회하지 않는다`() = runTest {
        val viewModel = createViewModel()
        viewModel.loadRequest(request.requestId)
        advanceUntilIdle()
        val callsBefore = userRepository.getUserByIdSyncCalls.size

        // When
        requestRepository.put(request.copy(requestMessage = "수정된 메시지"))
        advanceUntilIdle()

        // Then
        assertEquals("수정된 메시지", viewModel.uiState.value.request?.message)
        assertEquals("안", viewModel.uiState.value.request?.authorName)
        assertEquals(callsBefore, userRepository.getUserByIdSyncCalls.size)
    }
}
