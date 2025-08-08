package com.ahn.ggriggri.screen.archive.questionanswer

import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 답변 데이터 모델
data class Answer(
    val id: Int,
    val userName: String,
    val answerText: String,
    val userProfileImage: Int // Drawable 리소스 ID
)

// XML의 스타일을 대체하기 위한 TextStyle
val customToolbarTitleStyle = TextStyle(fontSize = 22.sp)
val customTextRegularStyle = TextStyle(fontSize = 18.sp)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun QuestionAnswerScreen() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "질문",
                        style = customToolbarTitleStyle
                    )
                },
            )
        }
    ) { paddingValues ->
        // NestedScrollView와 RecyclerView를 LazyColumn으로 대체
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 이모지 이미지
            item {
                Spacer(modifier = Modifier.height(70.dp))
//                Image(
//                    painter = painterResource(id = ),
//                    contentDescription = "질문 이모지",
//                    modifier = Modifier.size(120.dp),
//                    contentScale = ContentScale.Fit // scaleType="fitCenter"
//                )
            }

            // 질문 내용
            item {
                Spacer(modifier = Modifier.height(100.dp))
                Text(
                    text = "dkdkdkdkdkdk",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    textAlign = TextAlign.Center, // textAlignment="center"
                    style = customTextRegularStyle
                )
                Spacer(modifier = Modifier.height(70.dp))
            }

            // 답변 목록
//            items(answers) { answer ->
//                AnswerItem(answer = answer)
//            }
        }
    }
}

/**
 * 답변 목록의 각 항목을 표시하는 Composable
 */
@Composable
fun AnswerItem(answer: Answer) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = answer.userName, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = answer.answerText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        Image(
            painter = painterResource(id = answer.userProfileImage),
            contentDescription = "${answer.userName} 프로필 이미지",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape), // 원형 이미지
            contentScale = ContentScale.Crop
        )
    }
}

