package com.ahn.ggriggri.screen.ui.main.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.model.Profile
import com.ahn.domain.model.Question
import com.ahn.domain.model.QuestionList
import com.ahn.domain.model.Request
import com.ahn.domain.model.User
import com.ahn.domain.usecase.home.GetOrCreateTodayQuestionUseCase
import com.ahn.domain.usecase.home.HasUserRespondedUseCase
import com.ahn.domain.usecase.home.LoadAllQuestionListsUseCase
import com.ahn.domain.usecase.home.LoadProfilesUseCase
import com.ahn.domain.usecase.home.LoadRequestsUseCase
import com.ahn.domain.usecase.home.GetUserNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val loadProfilesUseCase: LoadProfilesUseCase,
    private val loadAllQuestionListsUseCase: LoadAllQuestionListsUseCase,
    private val getOrCreateTodayQuestionUseCase: GetOrCreateTodayQuestionUseCase,
    private val loadRequestsUseCase: LoadRequestsUseCase,
    private val hasUserRespondedUseCase: HasUserRespondedUseCase,
    private val getUserNameUseCase: GetUserNameUseCase,
) : ViewModel() {

    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _todayQuestionRecord = MutableStateFlow<Question?>(null)
    private val _allQuestionLists = MutableStateFlow<List<QuestionList>>(emptyList())

    private val _requests = MutableStateFlow<List<Request>>(emptyList())
    val requests: StateFlow<List<Request>> = _requests.asStateFlow()

    init {
        viewModelScope.launch {
            loadAllQuestionLists()

            sessionManager.currentUserGroupIdFlow.collect { groupId ->
                if (groupId.isNullOrBlank()) {
                    _profiles.value = emptyList()
                    _requests.value = emptyList()
                    _todayQuestionRecord.value = null
                    return@collect
                }

                loadProfiles(groupId)
                fetchOrGenerateTodayQuestionRecord(groupId)
                loadRequests()
            }
        }
    }

    fun loadProfiles(groupId: String) {
        viewModelScope.launch {
            _errorMessage.value = null

            when (val result = loadProfilesUseCase(groupId)) {
                is DataResourceResult.Success -> {
                    _profiles.value = result.data
                }
                is DataResourceResult.Failure -> {
                    _profiles.value = emptyList()
                    _errorMessage.value =
                        "그룹 멤버 정보를 가져오는데 실패했습니다: ${result.exception.message}"
                }
                else -> Unit
            }
        }
    }

    val currentUser: StateFlow<User?> = sessionManager.currentUserFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun getCurrentUserId(): String? = currentUser.value?.userId

    private suspend fun loadAllQuestionLists() {
        when (val result = loadAllQuestionListsUseCase()) {
            is DataResourceResult.Success -> {
                _allQuestionLists.value = result.data
            }
            is DataResourceResult.Failure -> {
                _allQuestionLists.value = emptyList()
                _errorMessage.value = "질문 목록을 불러오는데 실패했습니다."
            }
            else -> {
                _allQuestionLists.value = emptyList()
            }
        }
    }

    private suspend fun fetchOrGenerateTodayQuestionRecord(groupId: String) {
        _isLoading.value = true
        _todayQuestionRecord.value = null

        when (
            val result = getOrCreateTodayQuestionUseCase(
                groupId = groupId,
                questionLists = _allQuestionLists.value
            )
        ) {
            is DataResourceResult.Success -> {
                _todayQuestionRecord.value = result.data
            }
            is DataResourceResult.Failure -> {
                _errorMessage.value = "오늘의 질문 정보를 가져오거나 생성하는데 실패했습니다."
            }
            else -> Unit
        }

        _isLoading.value = false
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val todayQuestionContent: StateFlow<QuestionList?> = combine(
        _todayQuestionRecord,
        _allQuestionLists
    ) { record, allLists ->
        if (record == null || allLists.isEmpty()) {
            null
        } else {
            allLists.find { it.number.toString() == record.questionListDocumentId }
        }
    }.onEach {
        Unit
    }
        .catch { emit(null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )

    fun hasActiveRequest(): Boolean {
        return _requests.value.any { it.isAnswerable() }
    }

    fun getActiveRequest(): Request? {
        return _requests.value.firstOrNull { it.isAnswerable() }
    }

    fun loadRequests() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val currentGroupId = sessionManager.currentUserGroupIdFlow
                .filterNotNull()
                .firstOrNull()

            if (currentGroupId.isNullOrBlank()) {
                _errorMessage.value = "그룹 정보를 찾을 수 없습니다."
                _isLoading.value = false
                return@launch
            }

            loadRequestsUseCase(currentGroupId).collect { result ->
                when (result) {
                    is DataResourceResult.Loading -> {
                        _isLoading.value = true
                    }
                    is DataResourceResult.Success -> {
                        _requests.value = result.data ?: emptyList()
                        _isLoading.value = false
                    }
                    is DataResourceResult.Failure -> {
                        _errorMessage.value = "요청 목록을 불러오는데 실패했습니다."
                        _isLoading.value = false
                    }
                    else -> {
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    fun refreshRequests() {
        loadRequests()
    }

    fun getUserName(userId: String): String {
        return runBlocking { getUserNameUseCase(userId) }
    }

    fun navigateToCreateRequest(onNavigate: () -> Unit) {
        onNavigate()
    }

    fun showResponse(request: Request, onNavigate: (Request) -> Unit) {
        onNavigate(request)
    }

    suspend fun hasUserResponded(requestId: String): Boolean {
        return try {
            val userId = currentUser.first()?.userId
            if (userId.isNullOrBlank()) return false
            hasUserRespondedUseCase(requestId, userId)
        } catch (e: Exception) {
            Log.e("HomeViewModel", "hasUserResponded failed", e)
            false
        }
    }
}
