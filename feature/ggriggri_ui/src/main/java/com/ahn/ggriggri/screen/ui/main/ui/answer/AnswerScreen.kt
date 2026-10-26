package com.ahn.ggriggri.screen.ui.main.ui.answer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahn.common_ui.components.CommonButton
import com.ahn.common_ui.components.CommonOutlinedTextField
import com.ahn.ggriggri.screen.ui.main.ui.home.component.Apng.LoadAnimatedApngFromUrlComposable
import com.ahn.ggriggri.screen.ui.main.viewmodel.AnswerViewModel
import com.ahn.ggriggri.screen.ui.main.viewmodel.CurrentQuestionDetails
import com.ahn.common_ui.theme.GgriggriTheme
import com.ahn.common_ui.theme.NanumSquareBold

/**
 * 오늘의 질문 답변 화면의 Route.
 * ViewModel 상태 수집과 사이드 이펙트(스낵바 표시, 저장 성공 후 이동)를 담당하고
 * UI는 stateless [AnswerContent]에 위임한다.
 */
@Composable
fun AnswerScreen(
    onNavigateBack: () -> Unit,
    answerViewModel: AnswerViewModel = hiltViewModel(),
) {
    var answerText by rememberSaveable { mutableStateOf("") }

    val currentQuestionDetails by answerViewModel.currentQuestionDetails.collectAsStateWithLifecycle()
    val isLoading by answerViewModel.isLoading.collectAsStateWithLifecycle()
    val error by answerViewModel.error.collectAsStateWithLifecycle()
    val isSubmitting by answerViewModel.isSubmitting.collectAsStateWithLifecycle()
    val submitSuccessMessage by answerViewModel.submitSuccessMessage.collectAsStateWithLifecycle()
    val submitErrorMessage by answerViewModel.submitErrorMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // 제출 실패: 화면에 머무르며 메시지를 보여준다. 입력한 답변은 그대로 남는다.
    LaunchedEffect(submitErrorMessage) {
        submitErrorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            answerViewModel.clearSubmitErrorMessage()
        }
    }

    // 제출 성공: 메시지를 보여준 뒤에만 화면을 벗어난다.
    LaunchedEffect(submitSuccessMessage) {
        submitSuccessMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            answerViewModel.clearSubmitSuccessMessage()
            onNavigateBack()
        }
    }

    AnswerContent(
        questionDetails = currentQuestionDetails,
        isLoading = isLoading,
        isSubmitting = isSubmitting,
        error = error,
        answerText = answerText,
        onAnswerTextChange = { answerText = it },
        onSubmitClick = { answerViewModel.submitAnswer(answerText) },
        snackbarHostState = snackbarHostState,
    )
}

/**
 * 답변 화면 (stateless).
 *
 * @param error 질문 정보/답변 목록 로드 에러. 제출 실패는 스낵바로 따로 표시된다.
 */
@Composable
fun AnswerContent(
    questionDetails: CurrentQuestionDetails?,
    isLoading: Boolean,
    isSubmitting: Boolean,
    error: String?,
    answerText: String,
    onAnswerTextChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val isButtonEnabled = answerText.isNotBlank() && !isLoading && !isSubmitting && questionDetails != null

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp), // 좌우 공통 여백
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            when {
                // 초기 로딩 (질문 정보 로딩)
                questionDetails == null && isLoading -> {
                    Spacer(modifier = Modifier.height(50.dp))
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("질문 정보를 불러오는 중...")
                }
                // 질문 정보 로드 실패
                questionDetails == null && error != null -> {
                    Spacer(modifier = Modifier.height(50.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
                questionDetails != null -> {
                    Spacer(modifier = Modifier.height(30.dp))

                    questionDetails.imageUrl?.let {
                        LoadAnimatedApngFromUrlComposable(
                            imageUrl = it,
                            modifier = Modifier.size(200.dp)
                        )
                    } ?: Box(
                        modifier = Modifier
                            .size(200.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("이미지 없음")
                    }

                    Spacer(modifier = Modifier.height(50.dp))

                    Text(
                        text = questionDetails.content,
                        modifier = Modifier.widthIn(max = 300.dp), // 최대 너비 제한
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontFamily = NanumSquareBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 답변 목록 로드 에러 (질문 정보 로드 에러는 위에서 처리)
                    if (error != null) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    CommonOutlinedTextField(
                        value = answerText,
                        onValueChange = onAnswerTextChange,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 버튼을 하단에 고정하기 위한 Spacer
                    Spacer(modifier = Modifier.weight(1f))

                    CommonButton(
                        onClick = onSubmitClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        enabled = isButtonEnabled,
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "답변하기",
                                fontSize = 16.sp,
                                fontFamily = NanumSquareBold
                            )
                        }
                    }
                }
                else -> {
                    // 질문 정보가 없고 로딩도 에러도 아닌 경우 (이론상 잘 안 옴)
                    Spacer(modifier = Modifier.height(50.dp))
                    Text("질문 정보를 표시할 수 없습니다.")
                }
            }
        }
    }
}

private val previewQuestion = CurrentQuestionDetails(
    id = "group_20261026",
    content = "요즘 가장 자주 듣는 노래는 무엇인가요?",
    imageUrl = null
)

@Preview(name = "질문 로딩", showBackground = true)
@Composable
private fun AnswerContentLoadingPreview() {
    GgriggriTheme {
        AnswerContent(
            questionDetails = null,
            isLoading = true,
            isSubmitting = false,
            error = null,
            answerText = "",
            onAnswerTextChange = {},
            onSubmitClick = {}
        )
    }
}

@Preview(name = "답변 입력", showBackground = true)
@Composable
private fun AnswerContentSuccessPreview() {
    GgriggriTheme {
        AnswerContent(
            questionDetails = previewQuestion,
            isLoading = false,
            isSubmitting = false,
            error = null,
            answerText = "아이유 노래요",
            onAnswerTextChange = {},
            onSubmitClick = {}
        )
    }
}

@Preview(name = "제출 중", showBackground = true)
@Composable
private fun AnswerContentSubmittingPreview() {
    GgriggriTheme {
        AnswerContent(
            questionDetails = previewQuestion,
            isLoading = false,
            isSubmitting = true,
            error = null,
            answerText = "아이유 노래요",
            onAnswerTextChange = {},
            onSubmitClick = {}
        )
    }
}

@Preview(name = "질문 로드 실패", showBackground = true)
@Composable
private fun AnswerContentErrorPreview() {
    GgriggriTheme {
        AnswerContent(
            questionDetails = null,
            isLoading = false,
            isSubmitting = false,
            error = "오늘의 질문 기록을 불러오는데 실패했습니다.",
            answerText = "",
            onAnswerTextChange = {},
            onSubmitClick = {}
        )
    }
}
