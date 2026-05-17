package com.ahn.ggriggri.screen.main.answer

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahn.common_ui.components.CommonButton
import com.ahn.common_ui.components.CommonOutlinedTextField
import com.ahn.ggriggri.screen.ui.main.ui.home.component.Apng.LoadAnimatedApngFromUrlComposable
import com.ahn.ggriggri.screen.ui.main.viewmodel.AnswerViewModel
import theme.GgriggriTheme
import theme.NanumSquareBold


@Composable
fun AnswerScreen(
    onNavigateBack: () -> Unit
) {
    val answerViewModel: AnswerViewModel = hiltViewModel()
    var answerText by remember { mutableStateOf("") }

    val currentQuestionDetails by answerViewModel.currentQuestionDetails.collectAsStateWithLifecycle()
    val isLoading by answerViewModel.isLoading.collectAsStateWithLifecycle()
    val error by answerViewModel.error.collectAsStateWithLifecycle()

    val isButtonEnabled = answerText.isNotBlank() && !isLoading && currentQuestionDetails != null


    GgriggriTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp), // 좌우 공통 여백
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(50.dp))

                // 로딩 상태 또는 질문 정보 부재 시 처리
                if (currentQuestionDetails == null && isLoading) { // 초기 로딩 (질문 정보 로딩)
                    Spacer(modifier = Modifier.height(50.dp))
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("질문 정보를 불러오는 중...")
                } else if (currentQuestionDetails == null && error != null) { // 질문 정보 로드 실패
                    Spacer(modifier = Modifier.height(50.dp))
                    Text(
                        text = error ?: "질문 정보를 불러올 수 없습니다.",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                } else if (currentQuestionDetails != null) {
                    // 질문 정보가 있을 때 UI 표시
                    Spacer(modifier = Modifier.height(30.dp))

                    currentQuestionDetails?.imageUrl?.let {
                        LoadAnimatedApngFromUrlComposable(
                            imageUrl = it,
                            modifier = Modifier.size(200.dp)
                        )
                    } ?: Box(
                        modifier = Modifier.size(200.dp).padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("이미지 없음") // 이미지가 없을 경우
                    }

                    Spacer(modifier = Modifier.height(50.dp))

                    Text(
                        text = currentQuestionDetails?.content ?: "질문 내용 없음",
                        modifier = Modifier.widthIn(max = 300.dp), // 최대 너비 제한
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontFamily = NanumSquareBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 답변 로딩 또는 제출 로딩 시 (TextField 아래에 표시)
                    if (isLoading && answerText.isNotBlank()) { // 제출 시 isLoading
                        CircularProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))
                    }

                    // 에러 메시지 (답변 로딩/제출 관련)
                    if (error != null && currentQuestionDetails != null) { // 질문 정보 로드 에러는 위에서 이미 처리
                        Text(
                            text = error!!, // non-null 보장 (위 조건에서)
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    CommonOutlinedTextField(
                        value = answerText,
                        onValueChange = { answerText = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 버튼을 하단에 고정하기 위한 Spacer
                    Spacer(modifier = Modifier.weight(1f))

                    CommonButton(
                        onClick = {
                            if (currentQuestionDetails != null) { // null 체크
                                answerViewModel.submitAnswer(answerText)
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        enabled = isButtonEnabled,
                    ) {
                        Text(
                            text = "답변하기",
                            fontSize = 16.sp,
                            fontFamily = NanumSquareBold
                        )
                    }
                } else {
                    // currentQuestionDetails가 null이고 로딩도 아니고 에러도 아닌 경우 (이론상 잘 안옴)
                    Spacer(modifier = Modifier.height(50.dp))
                    Text("질문 정보를 표시할 수 없습니다.")
                }
            }
        }
    }
}



