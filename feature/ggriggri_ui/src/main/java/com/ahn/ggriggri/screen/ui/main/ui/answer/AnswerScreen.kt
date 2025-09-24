package com.ahn.ggriggri.screen.main.answer

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }

                // 로딩 상태 또는 질문 정보 부재 시 처리
                if (currentQuestionDetails == null && isLoading) {
                    item {
                        Spacer(modifier = Modifier.height(50.dp))
                    }
                    item {
                        CircularProgressIndicator()
                    }
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                    item {
                        Text("질문 정보를 불러오는 중...")
                    }
                } else if (currentQuestionDetails == null && error != null) {
                    item {
                        Spacer(modifier = Modifier.height(50.dp))
                    }
                    item {
                        Text(
                            text = error ?: "질문 정보를 불러올 수 없습니다.",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (currentQuestionDetails != null) {
                    // 질문 정보가 있을 때 UI 표시
                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }

                    item {
                        currentQuestionDetails?.imageUrl?.let {
                            LoadAnimatedApngFromUrlComposable(
                                imageUrl = it,
                                modifier = Modifier.size(200.dp)
                            )
                        } ?: Box(
                            modifier = Modifier.size(200.dp).padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("이미지 없음")
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(50.dp))
                    }

                    item {
                        Text(
                            text = currentQuestionDetails?.content ?: "질문 내용 없음",
                            modifier = Modifier.widthIn(max = 300.dp),
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontFamily = NanumSquareBold,
                            textAlign = TextAlign.Center
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // 답변 로딩 또는 제출 로딩 시
                    if (isLoading && answerText.isNotBlank()) {
                        item {
                            CircularProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))
                        }
                    }

                    // 에러 메시지
                    if (error != null && currentQuestionDetails != null) {
                        item {
                            Text(
                                text = error!!,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    item {
                        CommonOutlinedTextField(
                            value = answerText,
                            onValueChange = { answerText = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    item {
                        CommonButton(
                            onClick = {
                                if (currentQuestionDetails != null) {
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
                    }
                } else {
                    item {
                        Spacer(modifier = Modifier.height(50.dp))
                    }
                    item {
                        Text("질문 정보를 표시할 수 없습니다.")
                    }
                }
            }
        }
    }
}



