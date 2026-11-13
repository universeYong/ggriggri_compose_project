package com.ahn.ggriggri.screen.ui.archive.viewmodel.requestlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.model.Request
import com.ahn.domain.repository.RequestRepository
import com.ahn.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// 화면에 표시될 아이템 모델
data class ArchivedRequestItem(
    val requestId: String,
    val content: String,
    val userName: String,
    val date: String,
    val imgUrl: String? = null,
)

@HiltViewModel
class RequestListViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val requestRepository: RequestRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _archivedRequests = MutableStateFlow<List<ArchivedRequestItem>>(emptyList())
    val archivedRequests: StateFlow<List<ArchivedRequestItem>> = _archivedRequests.asStateFlow()

    // 첫 프레임부터 로딩 상태여야 "비어 있음" 화면이 잠깐 보이는 일이 없다.
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 같은 사용자의 이름을 요청마다 다시 읽지 않도록 캐시한다.
    private val userNameCache = mutableMapOf<String, String>()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadArchivedRequests()
    }

    // 실시간 리스너(collect)가 끝나지 않으므로 다시 호출되면 이전 구독을 취소한다.
    private var archivedRequestsJob: Job? = null

    fun loadArchivedRequests() {
        archivedRequestsJob?.cancel()
        archivedRequestsJob = viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val currentGroupId = sessionManager.currentUserGroupIdFlow.filterNotNull().firstOrNull()
                if (currentGroupId.isNullOrEmpty()) {
                    _error.value = "그룹 정보를 찾을 수 없습니다."
                    _isLoading.value = false
                    return@launch
                }

                Log.d("RequestListViewModel", "Loading requests for group: $currentGroupId")

                requestRepository.readAllRequests(currentGroupId).collect { result ->
                    when (result) {
                        is DataResourceResult.Loading -> {
                            _isLoading.value = true
                        }
                        is DataResourceResult.Success -> {
                            val requests = result.data
                            Log.d("RequestListViewModel", "Received ${requests.size} requests")

                            // 요청 작성자 이름을 (중복 없이) 한꺼번에 병렬로 읽는다. 이름이 준비된 뒤에 로딩을 끈다.
                            val names = loadUserNames(requests.map { it.requestUserDocumentID }.distinct())

                            // 모든 요청을 ArchivedRequestItem으로 변환
                            val archivedItems = requests.map { request ->
                                ArchivedRequestItem(
                                    requestId = request.requestId,
                                    content = request.requestMessage,
                                    userName = names[request.requestUserDocumentID] ?: UNKNOWN_USER,
                                    date = formatTime(request.requestTime),
                                    imgUrl = request.requestImage
                                )
                            }

                            _archivedRequests.value = archivedItems
                            _isLoading.value = false
                            Log.d("RequestListViewModel", "Converted to ${archivedItems.size} archived items")
                        }
                        is DataResourceResult.Failure -> {
                            _isLoading.value = false
                            _error.value = result.exception.message ?: "요청을 불러오는 중 오류가 발생했습니다."
                            Log.e("RequestListViewModel", "Error loading requests", result.exception)
                        }
                        else -> {}
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "알 수 없는 오류가 발생했습니다."
                Log.e("RequestListViewModel", "Exception in loadArchivedRequests", e)
            }
        }
    }

    /** 캐시에 없는 사용자만 서버에서 병렬로 읽어 userId -> 이름 맵을 돌려준다. 실패한 사용자는 캐시하지 않는다. */
    private suspend fun loadUserNames(userIds: List<String>): Map<String, String> = coroutineScope {
        val missing = userIds.filter { it !in userNameCache }
        missing.map { userId -> async { userId to fetchUserName(userId) } }
            .awaitAll()
            .forEach { (userId, name) -> if (name != null) userNameCache[userId] = name }
        userIds.mapNotNull { id -> userNameCache[id]?.let { id to it } }.toMap()
    }

    /** 이름을 읽어 온다. 읽기에 실패하면 null(캐시하지 않고 다음에 다시 시도). 사용자 문서가 없으면 "알 수 없는 사용자". */
    private suspend fun fetchUserName(userId: String): String? {
        return try {
            when (val userResult = userRepository.getUserByIdSync(userId)) {
                is DataResourceResult.Success -> userResult.data?.userName ?: UNKNOWN_USER
                else -> null
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("RequestListViewModel", "Error getting user name for $userId", e)
            null
        }
    }

    private fun formatTime(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
        return formatter.format(date)
    }

    private companion object {
        const val UNKNOWN_USER = "알 수 없는 사용자"
    }
}
