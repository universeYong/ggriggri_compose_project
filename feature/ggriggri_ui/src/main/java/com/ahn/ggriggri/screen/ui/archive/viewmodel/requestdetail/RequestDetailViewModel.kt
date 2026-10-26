package com.ahn.ggriggri.screen.ui.archive.viewmodel.requestdetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.model.Request
import com.ahn.domain.model.Response
import com.ahn.domain.repository.RequestRepository
import com.ahn.domain.repository.ResponseRepository
import com.ahn.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 요청 본문 표시용 값. 시간은 미리 포맷한 문자열이다. */
data class RequestDetailItem(
    val requestId: String,
    val message: String,
    val imageUrl: String,
    val timeText: String,
    /** 작성자 정보를 아직 못 불러왔으면 null. */
    val authorName: String?,
    val authorProfileImage: String?,
)

/** 응답 카드 표시용 값. */
data class ResponseItem(
    val key: String,
    val message: String,
    val imageUrl: String,
    val timeText: String,
    val authorName: String,
    val authorProfileImage: String,
)

data class RequestDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val request: RequestDetailItem? = null,
    val responses: List<ResponseItem> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RequestDetailViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val requestRepository: RequestRepository,
    private val userRepository: UserRepository,
    private val responseRepository: ResponseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestDetailUiState())
    val uiState: StateFlow<RequestDetailUiState> = _uiState.asStateFlow()

    // 실시간 리스너(collect)가 끝나지 않으므로 다시 호출되면 이전 구독을 취소한다.
    private var requestJob: Job? = null

    // userId별 조회 작업. 같은 사용자를 다시(또는 동시에) 조회하지 않도록 공유한다.
    // viewModelScope(Main)에서만 읽고 쓰므로 동기화가 필요 없다.
    private val userInfoRequests = mutableMapOf<String, Deferred<UserInfo>>()

    private data class UserInfo(val name: String, val profileImage: String)

    /** 요청 본문 쪽 흐름의 중간 상태. */
    private sealed interface RequestSection {
        data object Loading : RequestSection
        data class Error(val message: String) : RequestSection
        data class Found(val request: Request, val author: UserInfo?) : RequestSection
    }

    fun loadRequest(requestId: String) {
        requestJob?.cancel()
        requestJob = viewModelScope.launch {
            _uiState.value = RequestDetailUiState(isLoading = true)

            val currentGroupId = sessionManager.currentUserGroupIdFlow.firstOrNull()
            if (currentGroupId.isNullOrEmpty()) {
                _uiState.value = RequestDetailUiState(error = "그룹 정보를 찾을 수 없습니다.")
                return@launch
            }

            combine(
                requestSectionFlow(requestId, currentGroupId),
                responsesFlow(requestId),
            ) { section, responses ->
                when (section) {
                    RequestSection.Loading -> RequestDetailUiState(isLoading = true)
                    is RequestSection.Error -> RequestDetailUiState(error = section.message)
                    is RequestSection.Found -> RequestDetailUiState(
                        request = section.request.toItem(section.author),
                        responses = responses,
                    )
                }
            }
                .catch { e ->
                    Log.e(TAG, "Exception in loadRequest", e)
                    emit(RequestDetailUiState(error = e.message ?: "알 수 없는 오류가 발생했습니다."))
                }
                .collect { state -> _uiState.value = state }
        }
    }

    /** 요청 문서 하나를 구독하고, 작성자 정보를 붙인다. 다른 그룹의 요청은 찾을 수 없음으로 처리한다. */
    private suspend fun requestSectionFlow(
        requestId: String,
        groupId: String,
    ): Flow<RequestSection> =
        requestRepository.getRequestById(requestId).transformLatest { result ->
            when (result) {
                is DataResourceResult.Loading -> emit(RequestSection.Loading)
                is DataResourceResult.Failure -> {
                    Log.e(TAG, "Error loading request", result.exception)
                    emit(RequestSection.Error(result.exception.message ?: "요청을 불러오는 중 오류가 발생했습니다."))
                }
                is DataResourceResult.Success -> {
                    val request = result.data
                    if (request == null || request.requestGroupDocumentID != groupId) {
                        Log.e(TAG, "Request not found: $requestId")
                        emit(RequestSection.Error(NOT_FOUND_MESSAGE))
                    } else {
                        val authorId = request.requestUserDocumentID
                        val cached = cachedUserInfo(authorId)
                        // 작성자 정보 없이 먼저 내보내고, 조회가 끝나면 다시 내보낸다.
                        emit(RequestSection.Found(request, cached))
                        if (cached == null) {
                            emit(RequestSection.Found(request, userInfoAsync(authorId).await()))
                        }
                    }
                }
                is DataResourceResult.DummyConstructor -> Unit
            }
        }

    /** 응답 목록을 읽고, 아직 모르는 작성자만 병렬로 조회해 붙인다. 실패하면 빈 목록. */
    private fun responsesFlow(requestId: String): Flow<List<ResponseItem>> = flow {
        responseRepository.read(requestId).collect { result ->
            when (result) {
                is DataResourceResult.Loading -> Unit
                is DataResourceResult.Failure -> {
                    Log.e(TAG, "Error loading responses", result.exception)
                    emit(emptyList())
                }
                is DataResourceResult.Success -> {
                    val responses = result.data
                    val userInfos = loadUserInfos(responses.map { it.responseUserDocumentID })
                    emit(responses.map { it.toItem(userInfos[it.responseUserDocumentID]) })
                }
                is DataResourceResult.DummyConstructor -> Unit
            }
        }
    }
        .catch { e ->
            if (e is CancellationException) throw e
            Log.e(TAG, "Error loading responses for $requestId", e)
            emit(emptyList())
        }
        .onStart { emit(emptyList()) }

    /** 서로 다른 userId를 병렬로 조회한다(같은 userId는 진행 중인 조회를 공유). */
    private suspend fun loadUserInfos(userIds: List<String>): Map<String, UserInfo> =
        userIds
            .filter { it.isNotEmpty() }
            .distinct()
            .map { userId -> userId to userInfoAsync(userId) }
            .associate { (userId, deferred) -> userId to deferred.await() }

    /** 이미 조회가 끝난 사용자만 즉시 돌려준다. */
    private fun cachedUserInfo(userId: String): UserInfo? =
        userInfoRequests[userId]?.takeIf { it.isCompleted && !it.isCancelled }?.getCompleted()

    private fun userInfoAsync(userId: String): Deferred<UserInfo> =
        userInfoRequests.getOrPut(userId) { viewModelScope.async { fetchUserInfo(userId) } }

    /** 사용자가 없거나 조회에 실패하면 "알 수 없는 사용자". */
    private suspend fun fetchUserInfo(userId: String): UserInfo = try {
        when (val result = userRepository.getUserByIdSync(userId)) {
            is DataResourceResult.Success ->
                result.data?.let { UserInfo(it.userName, it.userProfileImage) } ?: UNKNOWN_USER
            is DataResourceResult.Failure -> {
                Log.e(TAG, "Error loading user info for $userId", result.exception)
                UNKNOWN_USER
            }
            else -> UNKNOWN_USER
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Error loading user info for $userId", e)
        UNKNOWN_USER
    }

    private fun Request.toItem(author: UserInfo?) = RequestDetailItem(
        requestId = requestId,
        message = requestMessage,
        imageUrl = requestImage,
        timeText = formatTime(requestTime),
        authorName = author?.name,
        authorProfileImage = author?.profileImage,
    )

    private fun Response.toItem(author: UserInfo?) = ResponseItem(
        key = responseId.ifBlank { "${responseUserDocumentID}_$responseTime" },
        message = responseMessage,
        imageUrl = responseImage,
        timeText = formatTime(responseTime),
        authorName = author?.name ?: UNKNOWN_USER_NAME,
        authorProfileImage = author?.profileImage ?: "",
    )

    companion object {
        private const val TAG = "RequestDetailViewModel"
        const val NOT_FOUND_MESSAGE = "요청을 찾을 수 없습니다."
        const val UNKNOWN_USER_NAME = "알 수 없는 사용자"
        private val UNKNOWN_USER = UserInfo(UNKNOWN_USER_NAME, "")

        // DateTimeFormatter는 불변이라 공유해도 안전하다.
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("MM월 dd일 HH:mm", Locale.getDefault())

        fun formatTime(timestamp: Long): String =
            TIME_FORMATTER.format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()))
    }
}
