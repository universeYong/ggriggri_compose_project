package com.ahn.ggriggri.screen.ui.main.viewmodel.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.common.TodayQuestionPreferences
import com.ahn.domain.model.Question
import com.ahn.domain.model.QuestionList
import com.ahn.domain.repository.GroupRepository
import com.ahn.domain.repository.QuestionListRepository
import com.ahn.domain.repository.QuestionRepository
import com.ahn.domain.repository.UserRepository
import com.ahn.ggriggri.screen.ui.main.worker.DateChangeNotifier
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

data class Profile(
    val id: String, // 사용자 ID
    val name: String,
    val profileImageUrl: String?,
)


class HomeViewModel(
    application: Application,
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val questionListRepository: QuestionListRepository,
    private val questionRepository: QuestionRepository,
    private val todayQuestionPreferences: TodayQuestionPreferences,
) : AndroidViewModel(application) {

    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // --- 오늘의 질문 관련 상태 ---
    private val _isLoadingTodayQuestion = MutableStateFlow(true) // 초기에는 로딩 상태

    /** 오늘의 질문 로딩 상태 */
    val isLoadingTodayQuestion: StateFlow<Boolean> = _isLoadingTodayQuestion.asStateFlow()

    // Firestore에서 가져온 오늘 날짜의 Question 레코드 (선정 기록)
    // 이 값은 fetchOrGenerateTodayQuestionRecord() 함수를 통해 업데이트 됩니다.
    private val _todayQuestionRecord = MutableStateFlow<Question?>(null)

    // 모든 질문 후보 목록 (라이브러리)
    // 이 값은 loadAllQuestionLists() 함수를 통해 업데이트 됩니다.
    private val _allQuestionLists = MutableStateFlow<List<QuestionList>>(emptyList())

    private val baseDate = LocalDate.of(2025, 8, 15)

    init {
        viewModelScope.launch {
            // 1. 모든 질문 후보 목록 로드
            loadAllQuestionLists() // loadAllQuestionLists 함수 호출 추가

            // 2. 초기 그룹 ID로 프로필 로드
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

            // 3. DateChangeNotifier를 구독하여 날짜 변경 또는 새로고침 시 오늘의 질문 로직 트리거
            DateChangeNotifier.dateChangedEvents
                .onStart { emit(Unit) } // 초기 실행을 위해 Unit 발행
                .collectLatest { // 이전 로직이 있다면 취소하고 새로 시작
                    Log.d(
                        "HomeViewModel",
                        "Date changed or initial load/refresh event received. Fetching today's question."
                    )
                    fetchOrGenerateTodayQuestionRecord()
                }
        }
    }


    fun loadProfiles(groupId: String) {
        viewModelScope.launch {
            Log.d("HomeViewModel", "loadProfiles called with groupId: $groupId") // 로그 추가
            _error.value = null

            val groupMemberResult = groupRepository
                .getGroupMembers(groupId)
                .filter { it !is DataResourceResult.Loading }
                .first()

            Log.d("HomeViewModel", "groupMemberResult: $groupMemberResult") // 로그 추가

            when (groupMemberResult) {
                is DataResourceResult.Success -> {
                    Log.d("HomeViewModel", "getGroupMembers SUCCESS")
                    val userIds = groupMemberResult.data
                    Log.d("HomeViewModel", "User IDs: $userIds") // 로그 추가
                    if (userIds.isNullOrEmpty()) {
                        Log.w(
                            "HomeViewModel",
                            "User IDs list is null or empty. Returning from loadProfiles."
                        )
                        _profiles.value = emptyList()
                        return@launch
                    }
                    Log.d("HomeViewModel", "Entering runCatching block to fetch user details.")
                    runCatching {
                        val profileListDeferred = userIds.map { userId ->
                            Log.d("HomeViewModel", "Async call for userId: $userId")
                            async {
                                Log.d("HomeViewModel", "Calling getUserById for userId: $userId")

                                val userResult = userRepository
                                    .getUserById(userId)
                                    .filter { it !is DataResourceResult.Loading }
                                    .first()
                                Log.d(
                                    "HomeViewModel",
                                    "getUserById result for $userId: $userResult"
                                )
                                when (userResult) {
                                    is DataResourceResult.Success -> {
                                        userResult.data?.let { user ->
                                            val imageUrl = user.userProfileImage
                                            Log.d(
                                                "HomeViewModel",
                                                "User: ${user.userName}, Image URL: $imageUrl"
                                            ) // 로그 추가
                                            Profile(
                                                id = user.userId ?: userId,
                                                name = user.userName,
                                                profileImageUrl = user.userProfileImage
                                            )
                                        }
                                    }

                                    is DataResourceResult.Failure -> {
                                        Log.e(
                                            "HomeViewModel",
                                            "Failed to get user info for $userId"
                                        )
                                        null
                                    }

                                    else -> {
                                        null
                                    }
                                }
                            }
                        }
                        val fetchedProfiles = profileListDeferred.awaitAll().filterNotNull()
                        _profiles.value = fetchedProfiles
                    }
                }

                is DataResourceResult.Failure -> {
                    _error.value = "그룹 멤버 정보를 가져오는데 실패했습니다: ${groupMemberResult}"
                }

                else -> {
                    null
                }
            }
        }
    }


    fun setCurrentGroupIdAndLoad(newGroupId: String) {
        // TODO: SessionManager를 통해 현재 그룹 ID를 업데이트하는 로직 필요 (예: sessionManager.setCurrentGroupId(newGroupId))
        loadProfiles(newGroupId)
        viewModelScope.launch { // 오늘의 질문도 그룹 변경에 따라 새로고침
            fetchOrGenerateTodayQuestionRecord()
        }
    }


    fun clearError() {
        _error.value = null
    }


    // 모든 질문 목록 로드 함수 (init에서 호출됨)
    private suspend fun loadAllQuestionLists() {
        questionListRepository.read()
            .filter { it !is DataResourceResult.Loading }
            .firstOrNull()
            .let { result ->
                when (result) {
                    is DataResourceResult.Success -> {
                        _allQuestionLists.value = result.data ?: emptyList()
                        Log.d(
                            "HomeViewModel",
                            "Loaded ${_allQuestionLists.value.size} question lists."
                        )
                    }

                    is DataResourceResult.Failure -> {
                        Log.e("HomeViewModel", "Failed to load question lists", result.exception)
                        _error.value = "질문 목록을 불러오는데 실패했습니다."
                    }

                    is DataResourceResult.DummyConstructor -> {
                        Log.w("HomeViewModel", "Received DummyConstructor for question lists.")
                        _allQuestionLists.value = emptyList()
                    }

                    null -> {
                        Log.w("HomeViewModel", "loadAllQuestionLists returned null.")
                        _error.value = "질문 목록 정보를 가져오지 못했습니다."
                        _allQuestionLists.value = emptyList()
                    }

                    else -> {
                        Log.w("HomeViewModel", "Unknown DataResourceResult type: $result")
                    }
                    // DataResourceResult.Loading은 이미 필터링 되었으므로, 이 케이스는 이론적으로 오지 않음
                }
            }
    }

    private suspend fun fetchOrGenerateTodayQuestionRecord() {
        _isLoadingTodayQuestion.value = true
        _todayQuestionRecord.value = null

        val currentGroupId = sessionManager.currentUserGroupIdFlow.filterNotNull().firstOrNull()
        if (currentGroupId.isNullOrEmpty()) {
            Log.w(
                "HomeViewModel",
                "Current group ID is null or empty. Cannot fetch today's question."
            )
            _error.value = "그룹 정보를 찾을 수 없습니다."
            _isLoadingTodayQuestion.value = false
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
                if (finalQuestionRecord != null) {
                    Log.d(
                        "HomeViewModel_Fetch",
                        "Found existing question record for today. Firestore ID: ${finalQuestionRecord.questionId}, ListRefID: ${finalQuestionRecord.questionListDocumentId}"
                    )
                } else {
                    // 기존 기록이 없으면 새로 생성
                    Log.d("HomeViewModel_Fetch", "No existing question record for today. Attempting to create new one.")
                    finalQuestionRecord = createNewQuestionRecordForToday(currentGroupId, todayStartTimestamp)
                }
            }
            is DataResourceResult.Failure -> {
                Log.e("HomeViewModel_Fetch", "Error fetching today's question record, attempting to create new one.", questionRecordResult.exception)
                // 가져오기 실패 시에도 새로 생성을 시도해볼 수 있음 (정책에 따라 다름)
                finalQuestionRecord = createNewQuestionRecordForToday(currentGroupId, todayStartTimestamp)
                if (finalQuestionRecord == null) { // 생성도 실패하면 에러 처리
                    _error.value = "오늘의 질문 정보를 가져오거나 생성하는데 실패했습니다."
                }
            }
            null -> { // Flow에서 아무 값도 발행되지 않은 경우 (이론적으로는 firstOrNull 때문에 오기 어려움)
                Log.w("HomeViewModel_Fetch", "getQuestionForGroupAndDate returned null result. Attempting to create new one.")
                finalQuestionRecord = createNewQuestionRecordForToday(currentGroupId, todayStartTimestamp)
                if (finalQuestionRecord == null) {
                    _error.value = "오늘의 질문 정보를 가져오지 못했습니다(null result)."
                }
            }
            // DataResourceResult.Loading, DataResourceResult.DummyConstructor 등은 이미 필터링되거나 처리됨
            else -> {
                Log.w("HomeViewModel_Fetch", "Unhandled result type from getQuestionForGroupAndDate: $questionRecordResult. Attempting to create.")
                finalQuestionRecord = createNewQuestionRecordForToday(currentGroupId, todayStartTimestamp)
            }
        }

        _todayQuestionRecord.value = finalQuestionRecord
        _isLoadingTodayQuestion.value = false

        if (finalQuestionRecord != null) {
            val firestoreDocumentId = finalQuestionRecord.questionId // Question 모델의 Firestore 문서 ID 필드
            if (!firestoreDocumentId.isNullOrEmpty()) {
                Log.d("HomeViewModel_DataStore", "fetchOrGenerate: Attempting to save Firestore Document ID to DataStore: '$firestoreDocumentId'")
                todayQuestionPreferences.saveTodayQuestionId(firestoreDocumentId)
                Log.d("HomeViewModel_DataStore", "fetchOrGenerate: Call to saveTodayQuestionId completed for Firestore Document ID: '$firestoreDocumentId'")
            } else {
                Log.w("HomeViewModel_DataStore", "fetchOrGenerate: Firestore Document ID from finalQuestionRecord is null or empty.")
            }
        } else {
            Log.w("HomeViewModel_DataStore", "fetchOrGenerate: finalQuestionRecord is null. Cannot save to DataStore.")
        }
    }

    private suspend fun createNewQuestionRecordForToday(
        groupId: String,
        timestamp: Long,
    ): Question? {
        val allLists = _allQuestionLists.value
        if (allLists.isEmpty()) {
            Log.w("HomeViewModel", "Cannot create new question record: Question list is empty.")
            return null
        }
        val selectedQuestionList = selectQuestionForTheDayLogic(allLists) ?: return null
        Log.w(
            "HomeViewModel",
            "Cannot create new question record: No question selected for the day."
        )

        val questionListIdToStore = selectedQuestionList.number.toString()

        val newQuestionRecord = Question(
            questionCreatedTime = timestamp,
            questionGroupDocumentId = groupId,
            questionListDocumentId = questionListIdToStore
        )

        // QuestionRepository.create 함수가 생성된 Question 객체(ID 포함) 또는 최소한 ID를 반환하도록 하는 것이 좋습니다.
        // 여기서는 Firestore가 ID를 자동 생성하고, 생성된 객체를 반환한다고 가정합니다.
        // 또는, create 후 해당 레코드를 다시 읽어와야 할 수도 있습니다.
        val creationResult = questionRepository.create(newQuestionRecord)
            .filter { it !is DataResourceResult.Loading }
            .firstOrNull() // create가 DataResourceResult<Question> 또는 DataResourceResult<String> (ID)를 반환한다고 가정

        return when (creationResult) {
            is DataResourceResult.Success -> {
                val createdQuestionWithId = creationResult.data
                Log.d(
                    "HomeViewModel",
                    "Successfully created new question record (ViewModel): $newQuestionRecord"
                )
                createdQuestionWithId
            }

            is DataResourceResult.Failure -> {
                Log.e(
                    "HomeViewModel",
                    "Failed to create new question record (ViewModel)",
                    creationResult.exception
                )
                _error.value = "오늘의 질문을 생성하는데 실패했습니다."
                null
            }

            else -> {
                Log.w(
                    "HomeViewModel",
                    "Failed to create new question record or loading/dummy (ViewModel): $creationResult"
                )
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
            Log.d(
                "HomeViewModel_Today",
                "Record is null or allLists is empty. Record: $record, Lists empty: ${allLists.isEmpty()}"
            )
            null
        } else {
            Log.d(
                "HomeViewModel_Today",
                "Finding question. Record.questionListDocumentId: ${record.questionListDocumentId}"
            )
            allLists.forEach {
                Log.d(
                    "HomeViewModel_Today_List",
                    "List item id: ${it.number}, content: ${it.content}"
                )
            }
            // ★★★ 수정: QuestionList의 id 필드와 Question의 questionListDocumentId를 비교 ★★★
            allLists.find { it.number.toString() == record.questionListDocumentId }
        }
    }
        .onEach {
            Log.d(
                "HomeViewModel_Today",
                "Emitting todayQuestionContent: ${it?.content}"
            )
        } // 각 emit 시 로그
        .catch { e ->
            Log.e("HomeViewModel", "Error in todayQuestionContent combine/stateIn", e)
            emit(null) // combine 또는 stateIn 내부에서 오류 발생 시
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null // 초기값은 null로 시작
        )

    private fun selectQuestionForTheDayLogic(lists: List<QuestionList>): QuestionList? {
        if (lists.isEmpty()) return null
        val today = LocalDate.now()
        val daysSinceBase = ChronoUnit.DAYS.between(baseDate, today)
        if (lists.isEmpty()) return null
        val questionIndex = (daysSinceBase % lists.size).toInt()
        return lists[questionIndex]
    }


}