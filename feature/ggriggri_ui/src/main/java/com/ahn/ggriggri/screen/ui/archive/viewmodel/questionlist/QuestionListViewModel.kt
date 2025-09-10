package com.ahn.ggriggri.screen.ui.archive.viewmodel.questionlist

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.repository.QuestionListRepository
import com.ahn.domain.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


// 화면에 표시될 아이템 모델 (Question과 QuestionList의 조합)
data class ArchivedQuestionItem(
    val questionRecordId: String,
    val questionNumberText: String,
    val content: String,
    val date: String,
    val imgUrl: String? = null,
)

@HiltViewModel
class QuestionListViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val questionRepository: QuestionRepository,
    private val questionListRepository: QuestionListRepository,
) : ViewModel() {

    private val _archivedQuestions = MutableStateFlow<List<ArchivedQuestionItem>>(emptyList())
    val archivedQuestions: StateFlow<List<ArchivedQuestionItem>> = _archivedQuestions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadArchivedQuestions()
    }

    fun loadArchivedQuestions() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val currentGroupId = sessionManager.currentUserGroupIdFlow.filterNotNull().firstOrNull()
            if (currentGroupId.isNullOrEmpty()) {
                _error.value = "그룹 정보를 찾을 수 없습니다."
                _isLoading.value = false
                return@launch
            }

            combine(
                questionRepository.getAllQuestionsForGroup(currentGroupId), // 이 함수 구현 필요
                questionListRepository.read() // 모든 질문 목록 가져오기 (기존 함수 활용)
            ) { questionRecordsResult, allQuestionListsResult ->
                // 두 Flow 모두 Success일 때만 처리
                if (questionRecordsResult is DataResourceResult.Success && allQuestionListsResult is DataResourceResult.Success) {
                    val questionRecords = questionRecordsResult.data ?: emptyList()
                    val allQuestionLists = allQuestionListsResult.data ?: emptyList()

                    // Question 기록을 questionListDocumentId를 사용하여 QuestionList와 매칭
                    val displayItems = questionRecords.mapNotNull { record ->
                        val matchingQuestionList = allQuestionLists.find { list ->
                            list.number.toString() == record.questionListDocumentId
                        }
                        if (matchingQuestionList != null) {
                            ArchivedQuestionItem(
                                questionRecordId = record.questionId ?: "", // Firestore 문서 ID
                                // questionNumberText는 QuestionList의 number를 사용하거나,
                                // Question 기록 자체에 순번이 있다면 그것을 사용
                                questionNumberText = "#${matchingQuestionList.number.toString().padStart(3, '0')}",
                                content = matchingQuestionList.content,
                                date = formatDate(record.questionCreatedTime) // 날짜 포맷팅 함수 필요
                            )
                        } else {
                            null // 매칭되는 QuestionList가 없으면 제외 (또는 기본값 표시)
                        }
                    }.sortedByDescending { it.date } // 최신 날짜 순으로 정렬 (또는 questionNumberText 역순)

                    Result.success(displayItems)
                } else if (questionRecordsResult is DataResourceResult.Failure) {
                    Result.failure(questionRecordsResult.exception ?: Exception("질문 기록 로드 실패"))
                } else if (allQuestionListsResult is DataResourceResult.Failure) {
                    Result.failure(allQuestionListsResult.exception ?: Exception("질문 라이브러리 로드 실패"))
                } else {
                    // 둘 중 하나라도 Loading이거나 다른 상태면, 여기서는 null을 반환하여
                    // collectLatest에서 처리하거나, 별도 StateFlow로 로딩 상태 관리
                    null
                }
            }.collectLatest { result ->
                _isLoading.value = false // combine 내부에서 로딩 상태 관리가 더 복잡하면, collect 진입/종료 시점으로 단순화
                result?.fold(
                    onSuccess = { items ->
                        _archivedQuestions.value = items
                        if (items.isEmpty()) {
                            // 데이터는 성공적으로 가져왔으나 목록이 비어있을 경우의 처리도 가능
                            Log.d("ArchiveViewModel", "No archived questions found for group $currentGroupId.")
                        }
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "데이터 로드 중 오류 발생"
                        Log.e("ArchiveViewModel", "Error loading archived questions", exception)
                    }
                )
            }
        }
    }

    private fun formatDate(timestamp: Long): String {
        return runCatching {
            val instant = Instant.ofEpochMilli(timestamp)
            val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
            val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd",
                Locale.getDefault())
            formatter.format(localDate)
        }.getOrElse {
            Log.e("ArchiveViewModel_FormatDate", "Error formatting timestamp: $timestamp", it)
            timestamp.toString() // 오류 시 임시로 원래 타임스탬프 문자열 반환
        }
    }
}