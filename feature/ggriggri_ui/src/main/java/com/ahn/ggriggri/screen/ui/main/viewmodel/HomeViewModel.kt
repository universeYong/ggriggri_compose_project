package com.ahn.ggriggri.screen.ui.main.viewmodel

import android.util.Log
import androidx.compose.runtime.Immutable
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class Profile(
    val id: String, // 사용자 ID
    val name: String,
    val profileImageUrl: String?,
)

/**
 * 홈 화면 활성 요청 카드의 표시 상태.
 *
 * @param requesterName 요청자 이름. 조회 전에는 빈 문자열.
 * @param isQuestioner 현재 사용자가 이 요청을 올린 사람인지.
 * @param hasUserResponded 현재 사용자가 이미 응답했는지.
 * @param isCheckingResponse 응답 여부를 아직 모르는 상태(사용자 정보 로딩 중 또는 조회 중).
 */
@Immutable
data class RequestCardUiState(
    val request: Request,
    val requesterName: String,
    val isQuestioner: Boolean,
    val hasUserResponded: Boolean,
    val isCheckingResponse: Boolean,
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

    // 오늘의 질문 로딩과 요청 목록 로딩은 따로 둔다.
    // (하나를 같이 쓰면 요청 목록을 다시 불러오는 동안 질문 카드에 스피너가 돈다.)
    private val _isQuestionLoading = MutableStateFlow(true)
    val isQuestionLoading: StateFlow<Boolean> = _isQuestionLoading.asStateFlow()

    private val _isRequestLoading = MutableStateFlow(false)
    val isRequestLoading: StateFlow<Boolean> = _isRequestLoading.asStateFlow()

    private val _todayQuestionRecord = MutableStateFlow<Question?>(null)
    private val _allQuestionLists = MutableStateFlow<List<QuestionList>>(emptyList())
    private val baseDate = LocalDate.of(2025, 8, 15)
    
    // 중복 생성 방지를 위한 플래그
    private var isCreatingQuestion = false

    private val _requests = MutableStateFlow<List<Request>>(emptyList())
    val requests: StateFlow<List<Request>> = _requests.asStateFlow()

    // 아래 상태들은 init 블록보다 먼저 선언해야 한다.
    // viewModelScope는 Main.immediate라 init의 launch가 곧바로 loadRequests()까지 실행될 수 있다.

    // requestRepository.read()는 Firestore 실시간 리스너라 끝나지 않는다.
    // 다시 호출될 때마다 이전 구독을 취소해 리스너가 쌓이지 않게 한다.
    private var requestsJob: Job? = null

    // 요청 카드의 응답 여부 재확인 트리거 (loadRequests 호출마다 증가)
    private val cardRefreshKey = MutableStateFlow(0)

    // 같은 요청을 다시 확인할 때 이전 결과를 유지해, 재확인 중 버튼이 "응답하기"로 깜빡이지 않게 한다.
    private var lastRequestCard: RequestCardUiState? = null

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
                        // 그룹이 없으면 오늘의 질문을 불러오지 않으므로 질문 스피너도 멈춘다.
                        _isQuestionLoading.value = false
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
        if (isCreatingQuestion) return
        try {
            fetchOrGenerateTodayQuestionRecordInternal()
        } finally {
            // 중간에 예외가 나도 플래그가 남아 이후 질문 로딩이 영원히 막히지 않게 한다.
            isCreatingQuestion = false
            _isQuestionLoading.value = false
        }
    }

    private suspend fun fetchOrGenerateTodayQuestionRecordInternal() {
        // 중복 생성 방지
        if (isCreatingQuestion) {
            Log.d("HomeViewModel", "이미 질문 생성 중이므로 중복 호출 방지")
            return
        }
        
        isCreatingQuestion = true
        Log.d("HomeViewModel", "fetchOrGenerateTodayQuestionRecord 시작")
        _isQuestionLoading.value = true
        _todayQuestionRecord.value = null

        val currentGroupId = sessionManager.currentUserGroupIdFlow.first()
        Log.d("HomeViewModel", "현재 그룹 ID: $currentGroupId")
        if (currentGroupId.isNullOrEmpty()) {
            Log.e("HomeViewModel", "그룹 ID가 null이거나 비어있음")
            _errorMessage.value = "그룹 정보를 찾을 수 없습니다."
            _isQuestionLoading.value = false
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
            // 조회에 실패했을 때는 오늘 질문이 이미 있는지 알 수 없으므로 새로 만들지 않는다.
            // (만들면 질문이 중복되고 그룹 전체에 푸시가 다시 나간다.)
            is DataResourceResult.Failure -> {
                Log.e("HomeViewModel", "질문 레코드 조회 실패: ${questionRecordResult.exception.message}")
                _errorMessage.value = "오늘의 질문 정보를 가져오는데 실패했습니다."
            }
            null -> {
                Log.w("HomeViewModel", "질문 레코드 조회 결과가 null")
                _errorMessage.value = "오늘의 질문 정보를 가져오지 못했습니다."
            }
            else -> {
                Log.w("HomeViewModel", "예상치 못한 질문 레코드 결과: $questionRecordResult")
            }
        }

        _todayQuestionRecord.value = finalQuestionRecord
        _isQuestionLoading.value = false
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
                Log.w("HomeViewModel", "기존 질문 확인 실패, 중복 생성을 피하기 위해 생성하지 않음")
                return null
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


    // 요청 카드가 보여줄 값(요청자 이름, 내가 질문자인지, 내가 응답했는지)을 한곳에서 만든다.
    // 카드/버튼 Composable은 이 값만 받고 ViewModel을 직접 호출하지 않는다.
    //
    // 현재 사용자 ID도 입력으로 combine 하므로, 사용자 정보가 요청 목록보다 늦게 로드되어도
    // 응답 여부를 다시 확인한다. responseRepository.read()는 1회성 조회라 화면 복귀 시
    // loadRequests()가 올리는 refreshKey로 재확인한다.
    private val currentUserIdFlow: Flow<String?> = sessionManager.currentUserFlow
        .map { it?.userId }
        .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeRequestCard: StateFlow<RequestCardUiState?> = combine(
        _requests,
        currentUserIdFlow,
        cardRefreshKey
    ) { requests, userId, refreshKey ->
        Triple(requests.firstOrNull { it.isAnswerable() }, userId, refreshKey)
    }
        .distinctUntilChanged()
        .flatMapLatest { (request, userId, _) ->
            if (request == null) {
                flowOf(null)
            } else {
                buildRequestCardState(request, userId)
            }
        }
        .catch { e ->
            Log.e("HomeViewModel", "요청 카드 상태 생성 실패", e)
            emit(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private fun buildRequestCardState(request: Request, userId: String?): Flow<RequestCardUiState> = flow {
        val isQuestioner = request.isQuestioner(userId)
        val previous = lastRequestCard?.takeIf { it.request.requestId == request.requestId }

        // 1) 먼저 알고 있는 값으로 카드를 그린다. 응답 여부를 모르는 동안에는 버튼을 막아 둔다.
        val pending = RequestCardUiState(
            request = request,
            requesterName = previous?.requesterName.orEmpty(),
            isQuestioner = isQuestioner,
            hasUserResponded = previous?.hasUserResponded ?: false,
            isCheckingResponse = !isQuestioner && previous == null
        )
        lastRequestCard = pending
        emit(pending)

        // 2) 요청자 이름과 응답 여부를 조회해 채운다.
        val requesterName = previous?.requesterName?.takeIf { it.isNotEmpty() }
            ?: getUserName(request.requestUserDocumentID)
        val hasResponded = when {
            isQuestioner -> false
            // 사용자 정보가 아직 없으면 판단을 미룬다. userId가 들어오면 combine이 다시 실행된다.
            userId == null -> null
            else -> hasUserResponded(request.requestId, userId)
        }
        val loaded = pending.copy(
            requesterName = requesterName,
            hasUserResponded = hasResponded ?: false,
            isCheckingResponse = !isQuestioner && hasResponded == null
        )
        lastRequestCard = loaded
        emit(loaded)
    }

    fun loadRequests() {
        // 화면에 다시 들어올 때마다 호출되므로, 그사이 바뀌었을 수 있는 응답 여부도 다시 확인한다.
        cardRefreshKey.update { it + 1 }
        requestsJob?.cancel()
        requestsJob = viewModelScope.launch {
            _isRequestLoading.value = true
            _errorMessage.value = null
            
            val currentGroupId = sessionManager.currentUserGroupIdFlow.filterNotNull().firstOrNull()
            if (currentGroupId.isNullOrEmpty()) {
                _errorMessage.value = "그룹 정보를 찾을 수 없습니다."
                _isRequestLoading.value = false
                return@launch
            }
            
            requestRepository.read(currentGroupId).collect { result ->
                when (result) {
                    is DataResourceResult.Loading -> {
                        _isRequestLoading.value = true
                    }
                    is DataResourceResult.Success -> {
                        _requests.value = result.data ?: emptyList()
                        _isRequestLoading.value = false
                    }
                    is DataResourceResult.Failure -> {
                        _errorMessage.value = "요청을 불러오는데 실패하였습니다."
                        _isRequestLoading.value = false
                    }
                    else -> {
                        _isRequestLoading.value = false
                    }
                }
            }
        }
    }

    private suspend fun getUserName(userId: String): String {
        return try {
            when (val result = userRepository.getUserByIdSync(userId)) {
                is DataResourceResult.Success -> result.data?.userName ?: "알 수 없음"
                else -> "알 수 없음"
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Failed to load user name for $userId", e)
            "알 수 없음"
        }
    }

    // 현재 사용자가 특정 Request에 응답했는지 확인
    private suspend fun hasUserResponded(requestId: String, userId: String): Boolean {
        return try {
            Log.d("HomeViewModel", "Checking if user $userId responded to request $requestId")

            // ResponseRepository에서 해당 Request의 모든 Response 조회
            val responsesResult = responseRepository.read(requestId)
                .filter { it !is DataResourceResult.Loading }
                .first()

            when (responsesResult) {
                is DataResourceResult.Success -> {
                    val hasResponded = responsesResult.data.any { response ->
                        response.responseUserDocumentID == userId
                    }
                    Log.d("HomeViewModel", "User $userId has responded to request $requestId: $hasResponded")
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error checking user response", e)
            false
        }
    }

}