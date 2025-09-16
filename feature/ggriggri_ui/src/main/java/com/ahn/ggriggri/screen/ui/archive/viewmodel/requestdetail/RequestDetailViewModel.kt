package com.ahn.ggriggri.screen.ui.archive.viewmodel.requestdetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.model.Response
import com.ahn.domain.model.Request
import com.ahn.domain.repository.ResponseRepository
import com.ahn.domain.repository.RequestRepository
import com.ahn.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@HiltViewModel
class RequestDetailViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val requestRepository: RequestRepository,
    private val userRepository: UserRepository,
    private val responseRepository: ResponseRepository,
) : ViewModel() {

    private val _request = MutableStateFlow<Request?>(null)
    val request: StateFlow<Request?> = _request.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _userProfileImage = MutableStateFlow<String?>(null)
    val userProfileImage: StateFlow<String?> = _userProfileImage.asStateFlow()

    private val _responses = MutableStateFlow<List<Response>>(emptyList())
    val responses: StateFlow<List<Response>> = _responses.asStateFlow()

    // 응답자 정보를 저장하는 Map
    private val _responseUserInfo = MutableStateFlow<Map<String, Pair<String, String>>>(emptyMap())
    val responseUserInfo: StateFlow<Map<String, Pair<String, String>>> = _responseUserInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadRequest(requestId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                Log.d("RequestDetailViewModel", "Loading request: $requestId")
                
                // TODO: requestRepository에서 특정 요청을 가져오는 함수가 필요
                // 현재는 readAllRequests를 사용하여 필터링
                // 추후 getRequestById 같은 함수가 필요할 수 있음
                
                // 현재 그룹 ID 가져오기
                val currentGroupId = sessionManager.currentUserGroupIdFlow.firstOrNull()
                if (currentGroupId.isNullOrEmpty()) {
                    _error.value = "그룹 정보를 찾을 수 없습니다."
                    _isLoading.value = false
                    return@launch
                }
                
                requestRepository.readAllRequests(currentGroupId).collect { result ->
                    when (result) {
                        is DataResourceResult.Loading -> {
                            _isLoading.value = true
                        }
                        is DataResourceResult.Success -> {
                            _isLoading.value = false
                            val requests = result.data
                            
                            // 요청 ID로 필터링
                            val foundRequest = requests.find { it.requestId == requestId }
                            if (foundRequest != null) {
                                _request.value = foundRequest
                                
                                // 사용자 이름 가져오기
                                loadUserName(foundRequest.requestUserDocumentID)
                                
                                // 응답 목록 가져오기
                                loadResponses(requestId)
                                
                                Log.d("RequestDetailViewModel", "Found request: ${foundRequest.requestMessage}")
                            } else {
                                _error.value = "요청을 찾을 수 없습니다."
                                Log.e("RequestDetailViewModel", "Request not found: $requestId")
                            }
                        }
                        is DataResourceResult.Failure -> {
                            _isLoading.value = false
                            _error.value = result.exception.message ?: "요청을 불러오는 중 오류가 발생했습니다."
                            Log.e("RequestDetailViewModel", "Error loading request", result.exception)
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "알 수 없는 오류가 발생했습니다."
                Log.e("RequestDetailViewModel", "Exception in loadRequest", e)
            }
        }
    }

    private suspend fun loadUserName(userId: String) {
        try {
            val userResult = userRepository.getUserByIdSync(userId)
            when (userResult) {
                is DataResourceResult.Success -> {
                    val user = userResult.data
                    _userName.value = user?.userName ?: "알 수 없는 사용자"
                    _userProfileImage.value = user?.userProfileImage ?: ""
                }
                else -> {
                    _userName.value = "알 수 없는 사용자"
                    _userProfileImage.value = ""
                }
            }
        } catch (e: Exception) {
            Log.e("RequestDetailViewModel", "Error getting user name for $userId", e)
            _userName.value = "알 수 없는 사용자"
            _userProfileImage.value = ""
        }
    }

    private suspend fun loadResponses(requestId: String) {
        try {
            Log.d("RequestDetailViewModel", "Loading responses for request: $requestId")
            
            responseRepository.read(requestId).collect { result ->
                when (result) {
                    is DataResourceResult.Loading -> {
                        Log.d("RequestDetailViewModel", "Loading responses...")
                    }
                    is DataResourceResult.Success -> {
                        _responses.value = result.data
                        Log.d("RequestDetailViewModel", "Loaded ${result.data.size} responses")
                        
                        // 응답자 정보 로드
                        loadResponseUserInfo(result.data)
                    }
                    is DataResourceResult.Failure -> {
                        Log.e("RequestDetailViewModel", "Error loading responses", result.exception)
                        _responses.value = emptyList()
                    }
                    else -> {}
                }
            }
        } catch (e: Exception) {
            Log.e("RequestDetailViewModel", "Error loading responses for $requestId", e)
            _responses.value = emptyList()
        }
    }

    private suspend fun loadResponseUserInfo(responses: List<Response>) {
        try {
            Log.d("RequestDetailViewModel", "Loading user info for ${responses.size} responses")
            
            val userInfoMap = mutableMapOf<String, Pair<String, String>>()
            
            responses.forEach { response ->
                val userId = response.responseUserDocumentID
                if (userId.isNotEmpty() && !userInfoMap.containsKey(userId)) {
                    try {
                        val userResult = userRepository.getUserByIdSync(userId)
                        when (userResult) {
                            is DataResourceResult.Success -> {
                                val user = userResult.data
                                if (user != null) {
                                    userInfoMap[userId] = Pair(
                                        user.userName ?: "알 수 없는 사용자",
                                        user.userProfileImage ?: ""
                                    )
                                    Log.d("RequestDetailViewModel", "Loaded user info for $userId: ${user.userName}")
                                }
                            }
                            else -> {
                                userInfoMap[userId] = Pair("알 수 없는 사용자", "")
                                Log.w("RequestDetailViewModel", "Failed to load user info for $userId")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("RequestDetailViewModel", "Error loading user info for $userId", e)
                        userInfoMap[userId] = Pair("알 수 없는 사용자", "")
                    }
                }
            }
            
            _responseUserInfo.value = userInfoMap
            Log.d("RequestDetailViewModel", "Loaded user info for ${userInfoMap.size} users")
        } catch (e: Exception) {
            Log.e("RequestDetailViewModel", "Error loading response user info", e)
            _responseUserInfo.value = emptyMap()
        }
    }
}
