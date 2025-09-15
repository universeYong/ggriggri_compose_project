package com.ahn.ggriggri.screen.ui.main.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.common.TodayQuestionPreferences
import com.ahn.domain.model.Question
import com.ahn.domain.model.QuestionList
import com.ahn.domain.model.Request
import com.ahn.domain.model.User
import com.ahn.domain.repository.GroupRepository
import com.ahn.domain.repository.QuestionListRepository
import com.ahn.domain.repository.QuestionRepository
import com.ahn.domain.repository.RequestRepository
import com.ahn.domain.repository.UserRepository
import com.ahn.ggriggri.screen.ui.main.worker.DateChangeNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

data class Profile(
    val id: String, // 사용자 ID
    val name: String,
    val profileImageUrl: String?,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val questionListRepository: QuestionListRepository,
    private val questionRepository: QuestionRepository,
    private val requestRepository: RequestRepository,
    private val todayQuestionPreferences: TodayQuestionPreferences,
) : ViewModel(){

    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _todayQuestionRecord = MutableStateFlow<Question?>(null)
    private val _allQuestionLists = MutableStateFlow<List<QuestionList>>(emptyList())
    private val baseDate = LocalDate.of(2025, 8, 15)

    private val _requests = MutableStateFlow<List<Request>>(emptyList())
    val requests: StateFlow<List<Request>> = _requests.asStateFlow()

    init {
        viewModelScope.launch {
            loadAllQuestionLists()

            sessionManager.currentUserGroupIdFlow
                .filterNotNull()
                .firstOrNull()?.let { initialGroupId ->
                    if (initialGroupId.isNotEmpty()) {
                        loadProfiles(initialGroupId)
                    } else {
                        Log.w("HomeViewModel", "Initial Group ID is empty during init.")
                    }
                } ?: Log.w(
                "HomeViewModel",
                "Initial Group ID is null or Flow was empty during init."
            )

            DateChangeNotifier.dateChangedEvents
                .onStart {
                    emit(Unit) }
                .collectLatest { fetchOrGenerateTodayQuestionRecord() }
        }
    }


    fun loadProfiles(groupId: String) {
        viewModelScope.launch {
            _errorMessage.value = null

            val groupMemberResult = groupRepository
                .getGroupMembers(groupId)
                .filter { it !is DataResourceResult.Loading }
                .first()

            when (groupMemberResult) {
                is DataResourceResult.Success -> {
                    val userIds = groupMemberResult.data
                    if (userIds.isNullOrEmpty()) {
                        Log.w(
                            "HomeViewModel",
                            "User IDs list is null or empty. Returning from loadProfiles."
                        )
                        _profiles.value = emptyList()
                        return@launch
                    }
                    runCatching {
                        val profileListDeferred = userIds.map { userId ->
                            async {
                                val userResult = userRepository
                                    .getUserById(userId)
                                    .filter { it !is DataResourceResult.Loading }
                                    .first()
                                when (userResult) {
                                    is DataResourceResult.Success -> {
                                        userResult.data?.let { user ->
                                            val imageUrl = user.userProfileImage
                                            Profile(
                                                id = user.userId ?: userId,
                                                name = user.userName,
                                                profileImageUrl = user.userProfileImage
                                            )
                                        }
                                    }

                                    is DataResourceResult.Failure -> { null }
                                    else -> { null }
                                }
                            }
                        }
                        val fetchedProfiles = profileListDeferred.awaitAll().filterNotNull()
                        _profiles.value = fetchedProfiles
                    }
                }

                is DataResourceResult.Failure -> {
                    _errorMessage.value = "그룹 멤버 정보를 가져오는데 실패했습니다: ${groupMemberResult}"
                }

                else -> {
                    null
                }
            }
        }
    }

    val currentUser: StateFlow<User?> = sessionManager.currentUserFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun getCurrentUserId(): String? {
        return currentUser.value?.userId
    }


    // 모든 질문 목록 로드 함수
    private suspend fun loadAllQuestionLists() {
        questionListRepository.read()
            .filter { it !is DataResourceResult.Loading }
            .firstOrNull()
            .let { result ->
                when (result) {
                    is DataResourceResult.Success -> {
                        _allQuestionLists.value = result.data ?: emptyList()
                    }
                    is DataResourceResult.Failure -> {
                        _errorMessage.value = "질문 목록을 불러오는데 실패했습니다."
                    }
                    is DataResourceResult.DummyConstructor -> {
                        _allQuestionLists.value = emptyList()
                    }
                    null -> {
                        _errorMessage.value = "질문 목록 정보를 가져오지 못했습니다."
                        _allQuestionLists.value = emptyList()
                    }
                    else -> {}
                }
            }
    }

    private suspend fun fetchOrGenerateTodayQuestionRecord() {
        _isLoading.value = true
        _todayQuestionRecord.value = null

        val currentGroupId = sessionManager.currentUserGroupIdFlow.filterNotNull().firstOrNull()
        if (currentGroupId.isNullOrEmpty()) {
            _errorMessage.value = "그룹 정보를 찾을 수 없습니다."
            _isLoading.value = false
            return
        }

        val todayStartTimestamp =
            LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val questionRecordResult = questionRepository
            .getQuestionForGroupAndDate(currentGroupId, todayStartTimestamp)
            .filter { it !is DataResourceResult.Loading }
            .firstOrNull()

        var finalQuestionRecord: Question? = null
        when (questionRecordResult) {
            is DataResourceResult.Success -> {
                finalQuestionRecord = questionRecordResult.data
                if (finalQuestionRecord == null) {
                    finalQuestionRecord = createNewQuestionRecordForToday(currentGroupId, todayStartTimestamp)

                }
            }
            is DataResourceResult.Failure -> {
                finalQuestionRecord = createNewQuestionRecordForToday(currentGroupId, todayStartTimestamp)
                if (finalQuestionRecord == null) {
                    _errorMessage.value = "오늘의 질문 정보를 가져오거나 생성하는데 실패했습니다."
                }
            }
            null -> {
                finalQuestionRecord = createNewQuestionRecordForToday(currentGroupId, todayStartTimestamp)
                if (finalQuestionRecord == null) {
                    _errorMessage.value = "오늘의 질문 정보를 가져오지 못했습니다(null result)."
                }
            }
            else -> {
                finalQuestionRecord = createNewQuestionRecordForToday(currentGroupId, todayStartTimestamp)
            }
        }

        _todayQuestionRecord.value = finalQuestionRecord
        _isLoading.value = false

        if (finalQuestionRecord != null) {
            val firestoreDocumentId = finalQuestionRecord.questionId
            if (!firestoreDocumentId.isNullOrEmpty()) {
                todayQuestionPreferences.saveTodayQuestionId(firestoreDocumentId)
            }
        }
    }

    private suspend fun createNewQuestionRecordForToday(
        groupId: String,
        timestamp: Long,
    ): Question? {
        val allLists = _allQuestionLists.value
        if (allLists.isEmpty()) {
            return null
        }
        val selectedQuestionList = selectQuestionForTheDayLogic(allLists) ?: return null
        val questionListIdToStore = selectedQuestionList.number.toString()

        val newQuestionRecord = Question(
            questionCreatedTime = timestamp,
            questionGroupDocumentId = groupId,
            questionListDocumentId = questionListIdToStore
        )

        val creationResult = questionRepository.create(newQuestionRecord)
            .filter { it !is DataResourceResult.Loading }
            .firstOrNull()

        return when (creationResult) {
            is DataResourceResult.Success -> {
                val createdQuestionWithId = creationResult.data
                createdQuestionWithId
            }
            is DataResourceResult.Failure -> {
                _errorMessage.value = "오늘의 질문을 생성하는데 실패했습니다."
                null
            }

            else -> { null }
        }
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
        }
        .catch { e ->
            emit(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )

    private fun selectQuestionForTheDayLogic(lists: List<QuestionList>): QuestionList? {
        if (lists.isEmpty()) return null
        val today = LocalDate.now()
        val daysSinceBase = ChronoUnit.DAYS.between(baseDate, today)
        if (lists.isEmpty()) return null
        val questionIndex = (daysSinceBase % lists.size).toInt()
        return lists[questionIndex]
    }


    fun hasActiveRequest(): Boolean {
        return _requests.value.any { it.isAnswerable() }
    }


    fun getActiveRequest(): Request? {
        return _requests.value.firstOrNull { it.isAnswerable() }
    }


    fun loadRequests() {
        viewModelScope.launch {
            val currentGroupId = sessionManager.currentUserGroupIdFlow.filterNotNull().firstOrNull()
            if (currentGroupId.isNullOrEmpty()) {
                _errorMessage.value = "그룹 정보를 찾을 수 없습니다."
                return@launch
            }
            
            requestRepository.read(currentGroupId).collect { result ->
                when (result) {
                    is DataResourceResult.Loading -> {
                        _isLoading.value = true
                    }
                    is DataResourceResult.Success -> {
                        _requests.value = result.data ?: emptyList()
                    }
                    is DataResourceResult.Failure -> {
                        _errorMessage.value = "질문을 불러오는데 실패하였습니다."
                    }
                    else -> {}
                }
            }
        }
    }

    fun refreshRequests() {
        loadRequests()
    }

    fun getUserName(userId: String): String {
        return try {
            runBlocking {
                when (val result = userRepository.getUserByIdSync(userId)) {
                    is DataResourceResult.Success -> result.data?.userName ?: "알 수 없음"
                    else -> "알 수 없음"
                }
            }
        } catch (e: Exception) {
            "알 수 없음"
        }
    }

    fun navigateToCreateRequest(onNavigate: () -> Unit) {
        onNavigate()
    }

    fun showResponse(request: Request, onNavigate: (Request) -> Unit) {
        onNavigate(request)
    }

    fun startAnswering(request: Request, onNavigate: (Request) -> Unit) {
        onNavigate(request)
    }

}