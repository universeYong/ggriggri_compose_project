package com.ahn.ggriggri.screen.ui.main.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.common.TodayQuestionPreferences
import com.ahn.domain.model.Answer
import com.ahn.domain.repository.AnswerRepository
import com.ahn.domain.repository.QuestionListRepository
import com.ahn.domain.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class CurrentQuestionDetails(
    val id: String,
    val content: String,
    val imageUrl: String?
)

@HiltViewModel
class AnswerViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val questionRepository: QuestionRepository,
    private val questionListRepository: QuestionListRepository,
    private val answerRepository: AnswerRepository,
    private val todayQuestionPreferences: TodayQuestionPreferences
) : ViewModel() {

    private val _answers = MutableStateFlow<List<Answer>>(emptyList())
    val answers: StateFlow<List<Answer>> = _answers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentQuestionDetails = MutableStateFlow<CurrentQuestionDetails?>(null)
    val currentQuestionDetails: StateFlow<CurrentQuestionDetails?> = _currentQuestionDetails.asStateFlow()

    private var _currentLoadedQuestionId: String? = null

    init {
        viewModelScope.launch {
            todayQuestionPreferences.todayQuestionIdFlow
                .collectLatest { questionIdFromDataStore ->
                    if (!questionIdFromDataStore.isNullOrEmpty()) {
                        Log.d(
                            "AnswerViewModel",
                            "Received today_question_id from DataStore: $questionIdFromDataStore"
                        )
                        // ViewModel 내부의 loadQuestionDetailsAndAnswers 함수 호출
                        loadQuestionDetailsAndAnswers(questionIdFromDataStore)
                    } else {
                        // DataStore에 ID가 없는 경우의 처리
                        _error.value = "오늘의 질문 정보를 찾을 수 없습니다. (DataStore)"
                        _currentQuestionDetails.value = null // 이전 정보 클리어
                        _answers.value = emptyList() // 이전 답변 클리어
                        _isLoading.value = false
                        Log.w(
                            "AnswerViewModel",
                            "Today's question ID is null or empty in DataStore."
                        )
                    }
                }
        }
    }

    fun loadQuestionDetailsAndAnswers(questionDataDocumentId: String) {
        Log.d(
            "AnswerViewModel_Load",
            "loadQuestionDetailsAndAnswers called with QuestionData_DocumentID: '$questionDataDocumentId'"
        )
        if (questionDataDocumentId.isEmpty() || _currentLoadedQuestionId == questionDataDocumentId) {
            // ... (중복 로드 방지)
            return
        }
        _currentLoadedQuestionId = questionDataDocumentId
        _isLoading.value = true
        _error.value = null
        _currentQuestionDetails.value = null // 이전 정보 클리어

        viewModelScope.launch {
            // 1. question_data 문서 ID를 사용하여 Question 객체 가져오기
            //    (QuestionRepository에 getQuestionByDocumentId 와 같은 함수 필요)
            //    이 예시에서는 questionRepository.getQuestionRecordById(id)가 DataResourceResult<Question>을 반환한다고 가정
            val questionRecordResult = questionRepository.getQuestionRecordById(questionDataDocumentId) // ★★★ 이 함수 구현 필요 ★★★
                .filter { it !is DataResourceResult.Loading }
                .firstOrNull()

            when (questionRecordResult) {
                is DataResourceResult.Success -> {
                    val questionRecord = questionRecordResult.data
                    if (questionRecord == null || questionRecord.questionListDocumentId.isNullOrEmpty()) {
                        _error.value = "오늘의 질문 기록 또는 질문 목록 참조 ID를 찾을 수 없습니다."
                        Log.w("AnswerViewModel_Load", "QuestionRecord is null or questionListDocumentId is empty for ID: $questionDataDocumentId")
                        _isLoading.value = false
                        return@launch
                    }

                    val questionListRefId = questionRecord.questionListDocumentId // 예: "2"

                    // 2. 모든 QuestionList 가져오기 (기존 방식 유지 또는 최적화 가능)
                    //    HomeViewModel에서 이미 로드했다면, AnswerViewModel도 동일한 인스턴스를 공유하거나,
                    //    필요시 다시 로드 (캐싱 여부에 따라). 여기서는 다시 로드한다고 가정.
                    val allQuestionListsResult = questionListRepository.read()
                        .filter { it !is DataResourceResult.Loading }
                        .firstOrNull()

                    if (allQuestionListsResult is DataResourceResult.Success) {
                        val questionList = allQuestionListsResult.data?.find { it.number.toString() == questionListRefId }
                        if (questionList != null) {
                            _currentQuestionDetails.value = CurrentQuestionDetails(
                                id = questionDataDocumentId, // Firestore 문서 ID (답변 제출 시 사용)
                                content = questionList.content,
                                imageUrl = questionList.imgUrl
                            )
                            Log.d("AnswerViewModel_Load", "Question details loaded for Firestore ID '$questionDataDocumentId': ${questionList.content}")
                        } else {
                            _error.value = "질문 목록에서 해당 질문 내용을 찾을 수 없습니다. (Ref ID: $questionListRefId)"
                            Log.w("AnswerViewModel_Load", "QuestionList not found for Ref ID: $questionListRefId (from Firestore ID: $questionDataDocumentId)")
                        }
                    } else if (allQuestionListsResult is DataResourceResult.Failure) {
                        _error.value = "질문 목록(라이브러리)을 불러오는데 실패했습니다."
                        Log.e("AnswerViewModel_Load", "Failed to load allQuestionLists", allQuestionListsResult.exception)
                    } else {
                        _error.value = "질문 목록(라이브러리) 정보를 가져올 수 없습니다."
                    }
                }
                is DataResourceResult.Failure -> {
                    _error.value = "오늘의 질문 기록을 불러오는데 실패했습니다: ${questionRecordResult.exception?.message}"
                    Log.e("AnswerViewModel_Load", "Failed to load question record by ID: $questionDataDocumentId", questionRecordResult.exception)
                }
                else -> {
                    _error.value = "오늘의 질문 기록 정보를 가져올 수 없습니다."
                }
            }
            _isLoading.value = false

            // 3. 해당 질문에 대한 답변 로드 (questionDataDocumentId 사용)
            if (_currentQuestionDetails.value != null) {
                loadAnswersForQuestionInternal(questionDataDocumentId) // 실제 Firestore 문서 ID 사용
            }
        }
    }


    // 기존 loadAnswersForQuestion은 내부용으로 변경 (또는 이름 유지하고 로직 통합)
    private fun loadAnswersForQuestionInternal(questionId: String) {
        viewModelScope.launch {
            answerRepository.read(questionId)
                .collectLatest { result ->
                    _isLoading.value = result is DataResourceResult.Loading
                    when (result) {
                        is DataResourceResult.Success -> {
                            _answers.value = result.data ?: emptyList()
                            // _error.value = null // 답변 로드 성공 시 에러 초기화 (선택적)
                            Log.d("AnswerViewModel", "Answers loaded for $questionId, count: ${_answers.value.size}")
                        }
                        is DataResourceResult.Failure -> {
                            Log.e("AnswerViewModel", "Failed to load answers for $questionId", result.exception)
                            _error.value = result.exception?.message ?: "답변 로드 실패"
                            _answers.value = emptyList()
                        }
                        DataResourceResult.Loading -> { /* _isLoading.value = true 이미 처리 */ }
                        else -> { /* DummyConstructor 등 기타 처리 */ }
                    }
                }
        }
    }


    fun submitAnswer(message: String) {
        val currentDetails = _currentQuestionDetails.value
        Log.d("AnswerViewModel_ID_Check", "submitAnswer called. currentDetails.id: ${currentDetails?.id}") // ★★★ 이 로그 확인 ★★★


        if (currentDetails == null) {
            _error.value = "질문 ID를 알 수 없어 답변을 제출할 수 없습니다."
            return
        }


        viewModelScope.launch {
            val userId = sessionManager.getCurrentUserIdOnce()

            if (userId.isNullOrEmpty()) {
                _error.value = "사용자 정보를 알 수 없어 답변을 제출할 수 없습니다."
                // 여기서 return@launch 를 사용하여 코루틴 실행을 중단할 수 있습니다.
                return@launch
            }

            val newAnswer = Answer(
                answerMessage = message,
                answerResponseTime = System.currentTimeMillis(),
                answerRequestState = 1,
                answerUserDocumentID = userId
            )
            answerRepository.create(currentDetails.id, newAnswer).collectLatest { result ->
                when (result) {
                    is DataResourceResult.Success -> {
                        val createdAnswerId = result.data
                        Log.d("AnswerViewModel", "Answer submitted successfully. ID: $createdAnswerId")
                        loadAnswersForQuestionInternal(currentDetails.id) // 새 답변 포함 목록 다시 로드
                    }
                    is DataResourceResult.Failure -> {
                        Log.e("AnswerViewModel", "Failed to submit answer", result.exception)
                        _error.value = result.exception?.message ?: "답변 제출 실패"
                    }
                    DataResourceResult.Loading -> {
                        _isLoading.value = result is DataResourceResult.Loading
                    }

                    is DataResourceResult.DummyConstructor -> {}
                }
            }
        }
    }
}