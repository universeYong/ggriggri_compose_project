package com.ahn.ggriggri.screen.archive.questionanswer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.ahn.common_ui.R
import com.ahn.ggriggri.screen.ui.archive.viewmodel.questionanswer.DisplayableAnswerItem
import com.ahn.ggriggri.screen.ui.archive.viewmodel.questionanswer.QuestionAnswerDetails
import com.ahn.ggriggri.screen.ui.archive.viewmodel.questionanswer.QuestionAnswerUiState
import com.ahn.ggriggri.screen.ui.archive.viewmodel.questionanswer.QuestionAnswerViewModel
import com.ahn.ggriggri.screen.ui.main.ui.home.component.Apng.LoadAnimatedApngFromUrlComposable
import theme.GgriggriTheme
import theme.NanumSquareBold
import theme.NanumSquareRegular


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionAnswerScreen() {

    val  questionAnswerViewModel: QuestionAnswerViewModel = hiltViewModel()
    val uiState by questionAnswerViewModel.uiState.collectAsState()

    GgriggriTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val state = uiState) {
                is QuestionAnswerUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is QuestionAnswerUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "오류: ${state.message}", modifier = Modifier.padding(16.dp))
                    }
                }

                is QuestionAnswerUiState.Success -> {
                    val details = state.details
                    QuestionAnswerContent(details = details, paddingValues = PaddingValues(16.dp))
                }
            }
        }
    }
}

@Composable
fun QuestionAnswerContent(details: QuestionAnswerDetails, paddingValues: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp), // 전체 좌우 패딩
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. 질문 이미지 (있을 경우)
        if (!details.questionImageUrl.isNullOrEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                LoadAnimatedApngFromUrlComposable(
                    imageUrl = details.questionImageUrl, // ViewModel에서 전달받은 이미지 URL
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // 적절한 높이 설정
                        .clip(MaterialTheme.shapes.medium)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // 2. 질문 내용
        item {
            Spacer(modifier = Modifier.height(if (details.questionImageUrl.isNullOrEmpty()) 24.dp else 8.dp))
            Text(
                text = details.questionContent,
                fontFamily = NanumSquareBold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
            if (details.answers.isNotEmpty()) {
                HorizontalDivider() // 답변 목록 시작 전 구분선
            }
        }

        // 3. 답변 목록
        if (details.answers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxHeight(0.5f)
                        .fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
                    Text("아직 등록된 답변이 없습니다.")
                }
            }
        } else {
            items(details.answers, key = { it.answerId }) { answerItem ->
                AnswerItem(answer = answerItem)
                HorizontalDivider()
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) } // 하단 여백
    }
}


@Composable
fun AnswerItem(answer: DisplayableAnswerItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp), // 상하 패딩 증가
        verticalAlignment = Alignment.Top // 상단 정렬
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(answer.userProfileImageUrl)
                .crossfade(true)
                .error(R.drawable.outline_account_circle_24) // 프로필 에러 시 placeholder
                .placeholder(R.drawable.outline_account_circle_24) // 프로필 로딩 중 placeholder
                .build(),
            contentDescription = "${answer.userName} 프로필 이미지",
            modifier = Modifier
                .size(48.dp) // 프로필 이미지 크기 약간 증가
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = answer.userName, fontFamily = NanumSquareRegular)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = answer.answerText, fontFamily = NanumSquareRegular)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = answer.answerTimeFormatted, fontFamily = NanumSquareRegular)
        }
    }
}