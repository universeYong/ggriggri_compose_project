package com.ahn.ggriggri.screen.archive.questionlist

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahn.ggriggri.screen.ui.archive.viewmodel.questionlist.QuestionListViewModel
import com.ahn.ggriggri.screen.ui.archive.viewmodel.questionlist.ArchivedQuestionItem

@Composable
fun QuestionListScreen(
    onNavigateToQuestionAnswer: (questionDataId: String) -> Unit = {}
) {
    val archiveViewModel: QuestionListViewModel = hiltViewModel()

    val archivedQuestions by archiveViewModel.archivedQuestions.collectAsState()
    val isLoading by archiveViewModel.isLoading.collectAsState()
    val error by archiveViewModel.error.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "오류: $error", modifier = Modifier.padding(16.dp))
            }
        } else if (archivedQuestions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "아직 생성된 질문이 없습니다.", modifier = Modifier.padding(16.dp))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(archivedQuestions) { index, item: ArchivedQuestionItem -> // 타입 명시
                    ListItem(
                        headlineContent = { Text(item.content) }, // 실제 질문 내용
                        supportingContent = { Text(item.questionNumberText) }, // 질문 번호
                        trailingContent = { Text(item.date) }, // 날짜
                        modifier = Modifier.clickable {
                            Log.d("QuestionListScreen", "Navigating with ID: ${item.questionRecordId}")
                            if (item.questionRecordId.isNotBlank()) {
                                onNavigateToQuestionAnswer(item.questionRecordId) // 클릭된 아이템의 ID 전달
                            } else {
                                Log.e("QuestionListScreen", "questionRecordId is blank for item: ${item.content}")
                                // 사용자에게 알림 또는 다른 처리 (예: Toast 메시지)
                            }
                        },
                    )
                    if (index < archivedQuestions.size - 1) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                    }
                }
            }
        }
    }
}