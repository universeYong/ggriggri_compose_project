package com.ahn.ggriggri.screen.ui.archive.viewmodel.questionanswer


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.repository.AnswerRepository
import com.ahn.domain.repository.QuestionListRepository
import com.ahn.domain.repository.QuestionRepository
import com.ahn.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


// 화면에 표시될 답변 아이템
data class DisplayableAnswerItem(
    val answerId: String,
    val userName: String,
    val userProfileImageUrl: String?,
    val answerText: String,
    val answerTimeFormatted: String, // 예: "오후 3:30" 또는 "2023.10.27"
)

// QuestionAnswerScreen 전체 데이터 모델
data class QuestionAnswerDetails(
    val questionContent: String = "",
    val questionImageUrl: String? = null,
    val answers: List<DisplayableAnswerItem> = emptyList(),
)


class QuestionAnswerViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val questionRepository: QuestionRepository,
    private val questionListRepository: QuestionListRepository,
    private val answerRepository: AnswerRepository,
    private val userRepository: UserRepository,
) : AndroidViewModel(application) {

    // QuestionListScreen에서 전달받은 question_data 문서 ID
    private val questionDataId: String = savedStateHandle.get<String>("questionDataId")
        ?: throw IllegalArgumentException("questionDataId is required.")

    private val _uiState = MutableStateFlow<QuestionAnswerUiState>(QuestionAnswerUiState.Loading)
    val uiState: StateFlow<QuestionAnswerUiState> = _uiState.asStateFlow()

    init {
        loadQuestionAndAnswers()
    }

    fun loadQuestionAndAnswers() {
        viewModelScope.launch {
            _uiState.value = QuestionAnswerUiState.Loading

            try {
                // 1. Question (메타데이터) 가져오기
                val questionRecordResult = questionRepository.getQuestionRecordById(questionDataId)
                    .filter { it !is DataResourceResult.Loading }.firstOrNull()

                val questionRecord = (questionRecordResult as? DataResourceResult.Success)?.data
                if (questionRecord == null || questionRecord.questionListDocumentId.isNullOrEmpty()) {
                    _uiState.value = QuestionAnswerUiState.Error("질문 기록을 찾을 수 없습니다.")
                    return@launch
                }

                // 2. QuestionList (질문 내용, 이미지) 가져오기
                //    QuestionListRepository에 getById와 같은 함수가 있거나, read() 후 find 사용
                val allQuestionListsResult =
                    questionListRepository.read() // 또는 getById(questionRecord.questionListDocumentId)
                        .filter { it !is DataResourceResult.Loading }.firstOrNull()
                val questionList = (allQuestionListsResult as? DataResourceResult.Success)?.data
                    ?.find { it.number.toString() == questionRecord.questionListDocumentId }

                if (questionList == null) {
                    _uiState.value = QuestionAnswerUiState.Error("질문 내용을 찾을 수 없습니다.")
                    return@launch
                }

                // 3. 해당 질문에 대한 모든 답변(Answer) 가져오기
                val answersResult = answerRepository.getAllAnswersForQuestion(questionDataId)
                    .filter { it !is DataResourceResult.Loading }.firstOrNull()
                val answerRecords =
                    (answersResult as? DataResourceResult.Success)?.data ?: emptyList()

                if (answersResult !is DataResourceResult.Success) {
                    Log.e("QAVM_Answers", "Failed to get answers. Result: $answersResult")
                }
                Log.d("QAVM_Answers", "Number of answer records fetched: ${answerRecords.size}")
                if (answerRecords.isEmpty() && answersResult is DataResourceResult.Success) {
                    Log.w(
                        "QAVM_Answers",
                        "Successfully fetched answers, but the list is empty for question ID: $questionDataId"
                    )
                }
                answerRecords.forEachIndexed { index, answer ->
                    Log.d(
                        "QAVM_Answers_Detail",
                        "Answer $index: ID=${answer.answerId}, UserID=${answer.answerUserDocumentID}, Msg=${answer.answerMessage}"
                    )
                }

                // 4. 각 답변에 대한 사용자 정보(User) 가져오기 (병렬 처리)
                val displayableAnswersDeferred = answerRecords.map { answer ->
                    async {
                        Log.d(
                            "QAVM_UserFetch",
                            "Fetching user for answerId: ${answer.answerId}, userId: '${answer.answerUserDocumentID}'"
                        ) // ★★★ (2) 로그 추가 ★★★
                        if (answer.answerUserDocumentID.isBlank()) {
                            Log.e(
                                "QAVM_UserFetch",
                                "User ID is BLANK for answerId: ${answer.answerId}. Cannot fetch user."
                            )
                            // UserID가 비어있으면 null 반환 또는 기본값 처리
                            null // 또는 기본 DisplayableAnswerItem 반환
                        } else {
                            val userResult = userRepository.getUserById(answer.answerUserDocumentID)
                                .filter { it !is DataResourceResult.Loading }.firstOrNull()
                            val user = (userResult as? DataResourceResult.Success)?.data

                            // ★★★ (3) userResult 및 user 객체 확인 로그 추가 ★★★
                            if (userResult !is DataResourceResult.Success) {
                                Log.e(
                                    "QAVM_UserFetch",
                                    "Failed to get user info for UserID: '${answer.answerUserDocumentID}'. Result: $userResult"
                                )
                            }
                            if (user == null && userResult is DataResourceResult.Success) {
                                Log.w(
                                    "QAVM_UserFetch",
                                    "Successfully fetched user data for UserID: '${answer.answerUserDocumentID}', but user object is null (likely no such user or DTO mapping issue)."
                                )
                            } else if (user != null) {
                                Log.d(
                                    "QAVM_UserFetch",
                                    "Fetched user: Name='${user.userName}', ProfileImg='${user.userProfileImage}' for UserID: '${answer.answerUserDocumentID}'"
                                )
                            }

                            if (user != null) { // user가 null이 아닐 때만 DisplayableAnswerItem 생성
                                DisplayableAnswerItem(
                                    answerId = answer.answerId,
                                    userName = user.userName.takeIf { it.isNotBlank() }
                                        ?: "사용자 정보 없음",
                                    userProfileImageUrl = user.userProfileImage,
                                    answerText = answer.answerMessage,
                                    answerTimeFormatted = formatAnswerTime(answer.answerResponseTime)
                                )
                            } else {
                                DisplayableAnswerItem(
                                    answerId = answer.answerId,
                                    userName = user?.userName ?: "알 수 없는 사용자",
                                    userProfileImageUrl = user?.userProfileImage,
                                    answerText = answer.answerMessage,
                                    answerTimeFormatted = formatAnswerTime(answer.answerResponseTime)
                                )
                            }
                        }
                    }
                }
                val displayableAnswers = displayableAnswersDeferred.awaitAll().filterNotNull()
                    .sortedBy { it.answerTimeFormatted } // 또는 다른 정렬 기준
                Log.d(
                    "QAVM_DisplayAnswers",
                    "Number of displayable answers: ${displayableAnswers.size}"
                )
                if (answerRecords.isNotEmpty() && displayableAnswers.isEmpty()) {
                    Log.w(
                        "QAVM_DisplayAnswers",
                        "Fetched answer records but displayableAnswers is empty. Check user fetching logic."
                    )
                }
                _uiState.value = QuestionAnswerUiState.Success(
                    QuestionAnswerDetails(
                        questionContent = questionList.content,
                        questionImageUrl = questionList.imgUrl,
                        answers = displayableAnswers
                    )
                )

            } catch (e: Exception) {
                _uiState.value =
                    QuestionAnswerUiState.Error("데이터 로드 중 오류 발생: ${e.localizedMessage}")
            }
        }
    }

    private fun formatAnswerTime(timestamp: Long): String {
        // 예시: "오후 3:30" 또는 "2023.10.27" 등
        return try {
            val instant = Instant.ofEpochMilli(timestamp)
            val zonedDateTime = instant.atZone(ZoneId.systemDefault())
            // 간단하게 날짜만 표시하거나, 더 자세한 포맷팅 가능
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm").format(zonedDateTime)
        } catch (e: Exception) {
            "시간 정보 없음"
        }
    }
}

// UI 상태 정의
sealed interface QuestionAnswerUiState {
    object Loading : QuestionAnswerUiState
    data class Success(val details: QuestionAnswerDetails) : QuestionAnswerUiState
    data class Error(val message: String) : QuestionAnswerUiState
}
