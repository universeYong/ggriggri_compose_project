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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadArchivedRequests()
    }

    fun loadArchivedRequests() {
        viewModelScope.launch {
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
                            _isLoading.value = false
                            val requests = result.data
                            Log.d("RequestListViewModel", "Received ${requests.size} requests")
                            
                            // 모든 요청을 ArchivedRequestItem으로 변환
                            val archivedItems = requests.map { request ->
                                ArchivedRequestItem(
                                    requestId = request.requestId,
                                    content = request.requestMessage,
                                    userName = getUserName(request.requestUserDocumentID),
                                    date = formatTime(request.requestTime),
                                    imgUrl = request.requestImage
                                )
                            }
                            
                            _archivedRequests.value = archivedItems
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
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "알 수 없는 오류가 발생했습니다."
                Log.e("RequestListViewModel", "Exception in loadArchivedRequests", e)
            }
        }
    }

    private suspend fun getUserName(userId: String): String {
        return try {
            val userResult = userRepository.getUserByIdSync(userId)
            when (userResult) {
                is DataResourceResult.Success -> {
                    userResult.data?.userName ?: "알 수 없는 사용자"
                }
                else -> "알 수 없는 사용자"
            }
        } catch (e: Exception) {
            Log.e("RequestListViewModel", "Error getting user name for $userId", e)
            "알 수 없는 사용자"
        }
    }

    private fun formatTime(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
        return formatter.format(date)
    }
}
