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
import com.ahn.domain.repository.ResponseRepository
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
    private val responseRepository: ResponseRepository,
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
    
    // 중복 생성 방지를 위한 플래그
    private var isCreatingQuestion = false

    private val _requests = MutableStateFlow<List<Request>>(emptyList())
    val requests: StateFlow<List<Request>> = _requests.asStateFlow()

    init {
        Log.d("HomeViewModel", "HomeViewModel 초기화 시작")
        viewModelScope.launch {
            Log.d("HomeViewModel", "ViewModelScope launch 시작")
            loadAllQuestionLists()

            // 그룹 ID 변경을 지속적으로 감지하여 프로필 로드
            Log.d("HomeViewModel", "그룹 ID Flow 구독 시작")
            sessionManager.currentUserGroupIdFlow
                .collect { groupId ->
                    Log.d("HomeViewModel", "그룹 ID Flow에서 값 수신: $groupId")
                    if (groupId != null && groupId.isNotEmpty()) {
                        Log.d("HomeViewModel", "Group ID changed to: $groupId, loading profiles...")
                        loadProfiles(groupId)
                        // 그룹 ID가 변경되면 요청 목록도 새로고침
                        loadRequests()
                    } else {
                        Log.w("HomeViewModel", "Group ID is null or empty, clearing profiles.")
                        _profiles.value = emptyList()
                        _requests.value = emptyList()
                    }
                }
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
                        
                        // 프로필 로딩 완료 후 오늘의 질문도 로드
                        Log.d("HomeViewModel", "프로필 로딩 완료, 오늘의 질문 로드 시작")
                        fetchOrGenerateTodayQuestionRecord()
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
        Log.d("HomeViewModel", "loadAllQuestionLists 시작")
        questionListRepository.read()
            .filter { it !is DataResourceResult.Loading }
            .firstOrNull()
            .let { result ->
                when (result) {
                    is DataResourceResult.Success -> {
                        val questionLists = result.data ?: emptyList()
                        Log.d("HomeViewModel", "질문 목록 로드 성공: ${questionLists.size}개")
                        _allQuestionLists.value = questionLists
                    }
                    is DataResourceResult.Failure -> {
                        Log.e("HomeViewModel", "질문 목록 로드 실패: ${result.exception.message}")
                        _errorMessage.value = "질문 목록을 불러오는데 실패했습니다."
                    }
                    is DataResourceResult.DummyConstructor -> {
                        Log.w("HomeViewModel", "DummyConstructor 상태")
                        _allQuestionLists.value = emptyList()
                    }
                    null -> {
                        Log.w("HomeViewModel", "질문 목록 결과가 null")
                        _errorMessage.value = "질문 목록 정보를 가져오지 못했습니다."
                        _allQuestionLists.value = emptyList()
                    }
                    else -> {
                        Log.w("HomeViewModel", "예상치 못한 결과: $result")
                    }
                }
            }
    }

    private suspend fun fetchOrGenerateTodayQuestionRecord() {
        // 중복 생성 방지
        if (isCreatingQuestion) {
            Log.d("HomeViewModel", "이미 질문 생성 중이므로 중복 호출 방지")
            return
        }
        
        isCreatingQuestion = true
        Log.d("HomeViewModel", "fetchOrGenerateTodayQuestionRecord 시작")
        _isLoading.value = true
        _todayQuestionRecord.value = null

        val currentGroupId = sessionManager.currentUserGroupIdFlow.first()
        Log.d("HomeViewModel", "현재 그룹 ID: $currentGroupId")
        if (currentGroupId.isNullOrEmpty()) {
            Log.e("HomeViewModel", "그룹 ID가 null이거나 비어있음")
            _errorMessage.value = "그룹 정보를 찾을 수 없습니다."
            _isLoading.value = false
            isCreatingQuestion = false
            return
        }

        val todayStartTimestamp =
            LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        Log.d("HomeViewModel", "오늘 시작 타임스탬프: $todayStartTimestamp")

        val questionRecordResult = questionRepository
            .getQuestionForGroupAndDate(currentGroupId, todayStartTimestamp)
            .filter { it !is DataResourceResult.Loading }
            .firstOrNull()

        Log.d("HomeViewModel", "질문 레코드 조회 결과: $questionRecordResult")
        var finalQuestionRecord: Question? = null
        when (questionRecordResult) {
            is DataResourceResult.Success -> {
                finalQuestionRecord = questionRecordResult.data
                Log.d("HomeViewModel", "기존 질문 레코드: $finalQuestionRecord")
                if (finalQuestionRecord == null) {
                    Log.d("HomeViewModel", "기존 질문 레코드가 없음, 새로 생성 시도")
                    finalQuestionRecord = createNewQuestionRecordForToday(currentGroupId, todayStartTimestamp)
                }
            }
            is DataResourceResult.Failure -> {
                Log.e("HomeViewModel", "질문 레코드 조회 실패: ${questionRecordResult.exception.message}")
                finalQuestionRecord = createNewQuestionRecordForToday(currentGroupId, todayStartTimestamp)
                if (finalQuestionRecord == null) {
                    _errorMessage.value = "오늘의 질문 정보를 가져오거나 생성하는데 실패했습니다."
                }
            }
            null -> {
                Log.w("HomeViewModel", "질문 레코드 조회 결과가 null")
                finalQuestionRecord = createNewQuestionRecordForToday(currentGroupId, todayStartTimestamp)
                if (finalQuestionRecord == null) {
                    _errorMessage.value = "오늘의 질문 정보를 가져오지 못했습니다(null result)."
                }
            }
            else -> {
                Log.w("HomeViewModel", "예상치 못한 질문 레코드 결과: $questionRecordResult")
                finalQuestionRecord = createNewQuestionRecordForToday(currentGroupId, todayStartTimestamp)
            }
        }

        _todayQuestionRecord.value = finalQuestionRecord
        _isLoading.value = false
        isCreatingQuestion = false // 플래그 해제

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
        Log.d("HomeViewModel", "createNewQuestionRecordForToday 시작")
        
        // 생성 전에 다시 한 번 확인 (동시성 문제 방지)
        val existingQuestionResult = questionRepository
            .getQuestionForGroupAndDate(groupId, timestamp)
            .filter { it !is DataResourceResult.Loading }
            .firstOrNull()
            
        when (existingQuestionResult) {
            is DataResourceResult.Success -> {
                if (existingQuestionResult.data != null) {
                    Log.d("HomeViewModel", "이미 오늘의 질문이 존재함: ${existingQuestionResult.data}")
                    return existingQuestionResult.data
                }
            }
            else -> {
                Log.d("HomeViewModel", "기존 질문 확인 중 오류 또는 null, 새로 생성 진행")
            }
        }
        
        val allLists = _allQuestionLists.value
        Log.d("HomeViewModel", "사용 가능한 질문 목록 수: ${allLists.size}")
        if (allLists.isEmpty()) {
            Log.e("HomeViewModel", "질문 목록이 비어있음 - 새 질문 레코드 생성 불가")
            return null
        }
        val selectedQuestionList = selectQuestionForTheDayLogic(allLists) ?: return null
        Log.d("HomeViewModel", "선택된 질문 목록: ${selectedQuestionList.number}")
        val questionListIdToStore = selectedQuestionList.number.toString()

        val newQuestionRecord = Question(
            questionCreatedTime = timestamp,
            questionGroupDocumentId = groupId,
            questionListDocumentId = questionListIdToStore
        )
        Log.d("HomeViewModel", "새 질문 레코드 생성: $newQuestionRecord")

        val creationResult = questionRepository.create(newQuestionRecord)
            .filter { it !is DataResourceResult.Loading }
            .firstOrNull()

        Log.d("HomeViewModel", "질문 레코드 생성 결과: $creationResult")
        return when (creationResult) {
            is DataResourceResult.Success -> {
                val createdQuestionWithId = creationResult.data
                Log.d("HomeViewModel", "질문 레코드 생성 성공: $createdQuestionWithId")
                createdQuestionWithId
            }
            is DataResourceResult.Failure -> {
                Log.e("HomeViewModel", "질문 레코드 생성 실패: ${creationResult.exception.message}")
                _errorMessage.value = "오늘의 질문을 생성하는데 실패했습니다."
                null
            }

            else -> { 
                Log.w("HomeViewModel", "예상치 못한 생성 결과: $creationResult")
                null 
            }
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
            _isLoading.value = true
            _errorMessage.value = null
            
            val currentGroupId = sessionManager.currentUserGroupIdFlow.filterNotNull().firstOrNull()
            if (currentGroupId.isNullOrEmpty()) {
                _errorMessage.value = "그룹 정보를 찾을 수 없습니다."
                _isLoading.value = false
                return@launch
            }
            
            requestRepository.read(currentGroupId).collect { result ->
                when (result) {
                    is DataResourceResult.Loading -> {
                        _isLoading.value = true
                    }
                    is DataResourceResult.Success -> {
                        _requests.value = result.data ?: emptyList()
                        _isLoading.value = false
                    }
                    is DataResourceResult.Failure -> {
                        _errorMessage.value = "질문을 불러오는데 실패하였습니다."
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

    // 현재 사용자가 특정 Request에 응답했는지 확인
    suspend fun hasUserResponded(requestId: String): Boolean {
        return try {
            val currentUser = currentUser.first()
            if (currentUser?.userId == null) return false
            
            Log.d("HomeViewModel", "Checking if user ${currentUser.userId} responded to request $requestId")
            
            // ResponseRepository에서 해당 Request의 모든 Response 조회
            val responsesResult = responseRepository.read(requestId)
                .filter { it !is DataResourceResult.Loading }
                .first()
            
            when (responsesResult) {
                is DataResourceResult.Success -> {
                    val hasResponded = responsesResult.data.any { response ->
                        response.responseUserDocumentID == currentUser.userId
                    }
                    Log.d("HomeViewModel", "User ${currentUser.userId} has responded to request $requestId: $hasResponded")
                    hasResponded
                }
                is DataResourceResult.Failure -> {
                    Log.e("HomeViewModel", "Error loading responses for request $requestId", responsesResult.exception)
                    false
                }
                else -> {
                    Log.w("HomeViewModel", "Unexpected result when checking user response")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error checking user response", e)
            false
        }
    }

}