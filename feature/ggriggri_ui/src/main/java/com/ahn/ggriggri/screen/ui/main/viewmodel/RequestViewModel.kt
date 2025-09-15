package com.ahn.ggriggri.screen.ui.main.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.data.repository.StorageRepository
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.model.Request
import com.ahn.domain.model.User
import com.ahn.domain.repository.RequestRepository
import com.ahn.ggriggri.screen.ui.main.utils.ImageCompressionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


@HiltViewModel
class RequestViewModel @Inject constructor(
    private val requestRepository: RequestRepository,
    private val storageRepository: StorageRepository,
    private val sessionManager: SessionManager,
    private val imageCompressionUtils: ImageCompressionUtils
): ViewModel() {

    val currentUser: StateFlow<User?> = sessionManager.currentUserFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        // SessionManager 상태 확인을 위한 로그
        viewModelScope.launch {
            currentUser.collect { user ->
                Log.d("RequestViewModel", "currentUser StateFlow 업데이트: $user")
            }
        }
        
        viewModelScope.launch {
            sessionManager.isLoggedInFlow.collect { isLoggedIn ->
                Log.d("RequestViewModel", "isLoggedIn StateFlow 업데이트: $isLoggedIn")
            }
        }
    }

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _requestMessage = MutableStateFlow("")
    val requestMessage: StateFlow<String> = _requestMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val maxMessageLength = 100

    val isRequestButtonEnabled: StateFlow<Boolean> = combine(
        requestMessage,
        selectedImageUri
    ) { message, imageUri ->
        message.isNotBlank() || imageUri != null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun selectImage(uri: Uri) {
        _selectedImageUri.value = uri
        _errorMessage.value = null
    }

    fun removeImage() {
        _selectedImageUri.value = null
    }

    fun updateMessage(message: String) {
        if (message.length <= maxMessageLength) {
            _requestMessage.value = message
            _errorMessage.value = null
        }
    }

    fun createRequest() {
        Log.d("RequestViewModel", "createRequest() 호출됨")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // 로그인 상태 확인
            val isLoggedIn = sessionManager.isLoggedInFlow.first()
            Log.d("RequestViewModel", "로그인 상태: $isLoggedIn")
            
            // 현재 사용자 정보 가져오기 - StateFlow에서 첫 번째 값을 가져옴
            val currentUser = currentUser.first()
            Log.d("RequestViewModel", "현재 사용자: $currentUser")
            
            if (!isLoggedIn || currentUser == null) {
                _isLoading.value = false
                _errorMessage.value = "로그인이 필요합니다."
                Log.e("RequestViewModel", "사용자가 로그인되지 않음 - isLoggedIn: $isLoggedIn, currentUser: $currentUser")
                return@launch
            }

            runCatching {
                var imageUrl = ""
                val selectedUri = _selectedImageUri.value
                Log.d("RequestViewModel", "선택된 이미지 URI: $selectedUri")
                
                // 이미지가 있는 경우 압축 후 업로드
                if (selectedUri != null) {
                    Log.d("RequestViewModel", "이미지 압축 및 업로드 시작")
                    
                    // 이미지 압축
                    val compressedUri = imageCompressionUtils.compressImage(selectedUri)
                    if (compressedUri == null) {
                        _errorMessage.value = "이미지 압축에 실패했습니다."
                        _isLoading.value = false
                        return@runCatching
                    }
                    
                    Log.d("RequestViewModel", "압축된 이미지 URI: $compressedUri")
                    
                    // 압축된 이미지 업로드
                    val uploadResult = storageRepository.uploadImage(compressedUri, "requests")
                    uploadResult.collect { result ->
                        when (result) {
                            is DataResourceResult.Success -> {
                                imageUrl = result.data
                                Log.d("RequestViewModel", "이미지 업로드 성공: $imageUrl")
                            }
                            is DataResourceResult.Failure -> {
                                Log.e("RequestViewModel", "이미지 업로드 실패: ${result.exception}")
                                _errorMessage.value = "이미지 업로드에 실패했습니다: ${result.exception.message}"
                                _isLoading.value = false
                                return@collect
                            }
                            else -> { }
                        }
                    }
                }

                val requestTime = System.currentTimeMillis()
                val answerDeadline = requestTime + (30 * 60 * 1000)

                val request = Request(
                    requestId = "",
                    requestTime = requestTime,
                    requestUserDocumentID = currentUser.userId,
                    requestMessage = _requestMessage.value,
                    requestImage = imageUrl,
                    requestGroupDocumentID = currentUser.userGroupDocumentId,
                    answerDeadline = answerDeadline,
                    hasAnswer = false
                )

                Log.d("RequestViewModel", "Request 객체 생성: $request")
                Log.d("RequestViewModel", "요청 시간: $requestTime, 답변 마감 시간: $answerDeadline")
                Log.d("RequestViewModel", "현재 시간: ${System.currentTimeMillis()}, 답변 가능 여부: ${request.isAnswerable()}")
                Log.d("RequestViewModel", "requestRepository.create() 호출 시작")

                requestRepository.create(request).collect { result ->
                    Log.d("RequestViewModel", "requestRepository.create() 결과: $result")
                    when (result) {
                        is DataResourceResult.Success -> {
                            Log.d("RequestViewModel", "요청 생성 성공!")
                            _isLoading.value = false
                            _successMessage.value = "요청이 성공적으로 등록되었습니다!"

                            _requestMessage.value = ""
                            _selectedImageUri.value = null
                        }
                        is DataResourceResult.Failure -> {
                            Log.e("RequestViewModel", "요청 생성 실패: ${result.exception}")
                            _isLoading.value = false
                            _errorMessage.value = "요청 등록에 실패했습니다: ${result.exception.message}"
                        }
                        else -> {
                            Log.d("RequestViewModel", "요청 생성 중...")
                            _isLoading.value = false
                        }
                    }
                }

            }.getOrElse {
                Log.e("RequestViewModel", "요청 처리 중 예외 발생: ${it.message}", it)
                _isLoading.value = false
                _errorMessage.value = "요청 처리 중 오류가 발생했습니다: ${it.message}"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    val currentMessageLength: StateFlow<Int> = requestMessage.map { it.length }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

}