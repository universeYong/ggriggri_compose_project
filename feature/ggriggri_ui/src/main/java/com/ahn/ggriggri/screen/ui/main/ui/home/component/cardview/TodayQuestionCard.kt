package com.ahn.ggriggri.screen.ui.main.ui.home.component.cardview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.ahn.common_ui.theme.OnQuestionCard
import com.ahn.common_ui.theme.QuestionCardButton
import com.ahn.common_ui.theme.QuestionCardFallback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.ahn.domain.model.QuestionList
import com.ahn.ggriggri.screen.ui.main.ui.home.component.Apng.LoadAnimatedApngFromUrlComposable
import com.ahn.common_ui.theme.NanumSquareBold
import com.ahn.common_ui.theme.NanumSquareRegular

@Composable
fun TodayQuestionCard(question: QuestionList, onAnswerClick: () -> Unit) {
    val stableImageUrl = remember(question.imgUrl) { question.imgUrl }

    // 카드 배경색은 질문 데이터(question.color, 밝은 파스텔)에서 오므로 그 위 텍스트·버튼 색도 모드와 무관하게 고정한다.
    val cardColor = runCatching {
        Color(question.color.toColorInt())
    }.getOrElse { QuestionCardFallback }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor) // 예시 색상
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "오늘의 질문",
                        fontSize = 16.sp,
                        fontFamily = NanumSquareBold,
                        color = OnQuestionCard
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = question.content,
                        modifier = Modifier.widthIn(max = 200.dp),
                        textAlign = TextAlign.Start,
                        fontSize = 14.sp,
                        fontFamily = NanumSquareRegular,
                        color = OnQuestionCard,
                        lineHeight = 20.sp
                    )
                }
                if (!stableImageUrl.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    LoadAnimatedApngFromUrlComposable(
                        imageUrl = stableImageUrl,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAnswerClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QuestionCardButton.copy(alpha = 0.8f),
                    contentColor = OnQuestionCard
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "답변하기", fontSize = 16.sp, fontFamily = NanumSquareBold)
            }
        }
    }
}