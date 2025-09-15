package com.ahn.ggriggri.screen.ui.archive.viewmodel.requestdetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.model.Answer
import com.ahn.domain.model.Request
import com.ahn.domain.repository.AnswerRepository
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
    private val answerRepository: AnswerRepository,
) : ViewModel() {

    private val _request = MutableStateFlow<Request?>(null)
    val request: StateFlow<Request?> = _request.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _userProfileImage = MutableStateFlow<String?>(null)
    val userProfileImage: StateFlow<String?> = _userProfileImage.asStateFlow()

    private val _answers = MutableStateFlow<List<Answer>>(emptyList())
    val answers: StateFlow<List<Answer>> = _answers.asStateFlow()

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
                                
                                // 답변 목록 가져오기
                                loadAnswers(requestId)
                                
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

    private suspend fun loadAnswers(requestId: String) {
        try {
            Log.d("RequestDetailViewModel", "Loading answers for request: $requestId")
            
            // TODO: AnswerRepository에서 특정 요청의 답변을 가져오는 함수가 필요
            // 현재는 임시로 빈 목록으로 설정
            _answers.value = emptyList()
            
            Log.d("RequestDetailViewModel", "Loaded ${_answers.value.size} answers")
        } catch (e: Exception) {
            Log.e("RequestDetailViewModel", "Error loading answers for $requestId", e)
            _answers.value = emptyList()
        }
    }
}
