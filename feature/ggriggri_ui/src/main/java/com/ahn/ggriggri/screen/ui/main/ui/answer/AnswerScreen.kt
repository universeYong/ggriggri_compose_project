package com.ahn.ggriggri.screen.main.answer

import androidx.compose.runtime.Composable
import android.widget.ImageView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide

// XML의 폰트 스타일을 대체하기 위한 변수 (실제 프로젝트의 FontFamily로 교체 필요)
val nanumSquareRegular = FontWeight.Normal
val nanumSquareBold = FontWeight.Bold

@Composable
fun AnswerScreen(
    questionText: String,
    emojiUrl: String,
    onNavigateBack: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var answerText by remember { mutableStateOf("") }
    val isButtonEnabled = answerText.isNotBlank()

    Scaffold(
        topBar = {
            AnswerTopBar(
                title = "답변하기",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp), // 좌우 공통 여백
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // 이전 대화에서 사용한 APNG 로딩 Composable
            LoadAnimatedApngFromUrl(
                imageUrl = emojiUrl,
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = questionText,
                modifier = Modifier.widthIn(max = 300.dp), // 최대 너비 제한
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = nanumSquareBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            AnswerInputField(
                value = answerText,
                onValueChange = { answerText = it },
                modifier = Modifier.fillMaxWidth()
            )

            // 버튼을 하단에 고정하기 위한 Spacer
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onSubmit(answerText) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                enabled = isButtonEnabled,
                shape = MaterialTheme.shapes.medium, // custom_btn_background 대체
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF0F0F0), // custom_btn_background 색상 예시
                    contentColor = Color.Black, // custom_btn_text 색상 예시
                    disabledContainerColor = Color.LightGray,
                    disabledContentColor = Color.Gray
                )
            ) {
                Text(
                    text = "답변하기",
                    fontSize = 16.sp,
                    fontWeight = nanumSquareBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswerTopBar(title: String, onNavigateBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                // style = MaterialTheme.typography.titleLarge // CustomToolbarTitle 대체
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로 가기"
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun AnswerInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxChars = 100
    Column(modifier = modifier, horizontalAlignment = Alignment.End) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (it.length <= maxChars) {
                    onValueChange(it)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "답변을 입력하세요.",
                    fontWeight = nanumSquareRegular,
                    fontSize = 14.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFFFFFDE7) // mainColor 예시
            ),
            singleLine = false // 여러 줄 입력 가능
        )
        Text(
            text = "${value.length} / $maxChars",
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

// 이전 대화에서 사용한 Composable (재사용)
@Composable
fun LoadAnimatedApngFromUrl(imageUrl: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply {
                Glide.with(context)
                    .load(imageUrl)
                    .into(this)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AnswerScreenPreview() {
    MaterialTheme {
        AnswerScreen(
            questionText = "이 그룹과 함께 하며 가장 화난 순간은?",
            emojiUrl = "https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Angry%20Face.png",
            onNavigateBack = {},
            onSubmit = {}
        )
    }
}