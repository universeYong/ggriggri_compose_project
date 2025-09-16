package com.ahn.ggriggri.screen.ui.main.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.data.repository.StorageRepository
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.model.Response
import com.ahn.domain.repository.ResponseRepository
import com.ahn.ggriggri.screen.ui.main.utils.ImageCompressionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResponseViewModel @Inject constructor(
    private val responseRepository: ResponseRepository,
    private val sessionManager: SessionManager,
    private val storageRepository: StorageRepository,
    private val imageCompressionUtils: ImageCompressionUtils
) : ViewModel() {

    private val _requestDocumentId = MutableStateFlow<String>("")
    val requestDocumentId: StateFlow<String> = _requestDocumentId.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _responseMessage = MutableStateFlow("")
    val responseMessage: StateFlow<String> = _responseMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    val currentMessageLength: StateFlow<Int> = _responseMessage.map { it.length }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val isResponseButtonEnabled: StateFlow<Boolean> = 
        _responseMessage.map { it.isNotBlank() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )

    fun setRequestDocumentId(requestId: String) {
        Log.d("ResponseViewModel", "setRequestDocumentId 호출됨: $requestId")
        _requestDocumentId.value = requestId
    }

    fun selectImage(uri: Uri) {
        _selectedImageUri.value = uri
    }

    fun removeImage() {
        _selectedImageUri.value = null
    }

    fun updateMessage(message: String) {
        if (message.length <= 100) {
            _responseMessage.value = message
        }
    }

    fun createResponse() {
        viewModelScope.launch {
            Log.d("ResponseViewModel", "createResponse 시작")
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val currentUser = sessionManager.currentUserFlow.first()
                Log.d("ResponseViewModel", "현재 사용자: $currentUser")
                if (currentUser == null) {
                    _errorMessage.value = "사용자 정보를 찾을 수 없습니다."
                    _isLoading.value = false
                    Log.e("ResponseViewModel", "사용자 정보가 null입니다.")
                    return@launch
                }

                val requestDocumentId = _requestDocumentId.value
                Log.d("ResponseViewModel", "요청 문서 ID: $requestDocumentId")
                if (requestDocumentId.isEmpty()) {
                    _errorMessage.value = "요청 정보를 찾을 수 없습니다."
                    _isLoading.value = false
                    Log.e("ResponseViewModel", "요청 문서 ID가 비어있습니다.")
                    return@launch
                }

                var imageUrl = ""
                val selectedUri = _selectedImageUri.value
                Log.d("ResponseViewModel", "선택된 이미지 URI: $selectedUri")
                
                // 이미지가 있으면 업로드
                if (selectedUri != null) {
                    try {
                        Log.d("ResponseViewModel", "이미지 압축 시작")
                        // 이미지 압축
                        val compressedUri = imageCompressionUtils.compressImage(selectedUri)
                        if (compressedUri == null) {
                            _errorMessage.value = "이미지 압축에 실패했습니다."
                            _isLoading.value = false
                            Log.e("ResponseViewModel", "이미지 압축 실패")
                            return@launch
                        }
                        
                        Log.d("ResponseViewModel", "압축된 이미지 URI: $compressedUri")
                        Log.d("ResponseViewModel", "이미지 업로드 시작")
                        // 압축된 이미지 업로드
                        val uploadResult = storageRepository.uploadImage(compressedUri, "response")
                        uploadResult.collect { result ->
                            when (result) {
                                is DataResourceResult.Success -> {
                                    imageUrl = result.data
                                    Log.d("ResponseViewModel", "이미지 업로드 성공: $imageUrl")
                                }
                                is DataResourceResult.Failure -> {
                                    _errorMessage.value = "이미지 업로드에 실패했습니다: ${result.exception.message}"
                                    _isLoading.value = false
                                    Log.e("ResponseViewModel", "이미지 업로드 실패", result.exception)
                                    return@collect
                                }
                                else -> { 
                                    Log.d("ResponseViewModel", "이미지 업로드 로딩 중...")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        _errorMessage.value = "이미지 업로드 중 오류가 발생했습니다: ${e.message}"
                        _isLoading.value = false
                        Log.e("ResponseViewModel", "이미지 업로드 중 예외 발생", e)
                        return@launch
                    }
                } else {
                    Log.d("ResponseViewModel", "이미지가 없어서 업로드 건너뜀")
                }

                Log.d("ResponseViewModel", "Response 객체 생성 시작")
                val response = Response(
                    responseId = "", // Firebase에서 자동 생성될 ID
                    responseMessage = _responseMessage.value,
                    responseTime = System.currentTimeMillis(),
                    responseImage = imageUrl,
                    responseUserDocumentID = currentUser.userId ?: "",
                    responseUserProfileImage = currentUser.userProfileImage ?: ""
                )
                Log.d("ResponseViewModel", "생성된 Response: $response")

                Log.d("ResponseViewModel", "Firebase에 Response 저장 시작")
                val result = responseRepository.create(requestDocumentId, response)
                    .filter { it !is DataResourceResult.Loading }
                    .first()

                Log.d("ResponseViewModel", "Firebase 저장 결과: $result")
                when (result) {
                    is DataResourceResult.Success -> {
                        _successMessage.value = "응답이 성공적으로 등록되었습니다."
                        _responseMessage.value = ""
                        _selectedImageUri.value = null
                        Log.d("ResponseViewModel", "Response 저장 성공")
                    }
                    is DataResourceResult.Failure -> {
                        _errorMessage.value = "응답 등록에 실패했습니다: ${result.exception.message}"
                        Log.e("ResponseViewModel", "Response 저장 실패", result.exception)
                    }
                    else -> {
                        _errorMessage.value = "알 수 없는 오류가 발생했습니다."
                        Log.e("ResponseViewModel", "알 수 없는 결과: $result")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "응답 등록 중 오류가 발생했습니다: ${e.message}"
                Log.e("ResponseViewModel", "Response 생성 중 예외 발생", e)
            } finally {
                _isLoading.value = false
                Log.d("ResponseViewModel", "createResponse 완료")
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}
