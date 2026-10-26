package com.ahn.ggriggri.screen.ui.main.ui.home.component.requestcard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahn.domain.model.Request
import com.ahn.ggriggri.screen.ui.main.viewmodel.RequestCardUiState
import theme.MainColor
import theme.NanumSquareBold
import theme.NanumSquareRegular

/**
 * 답변 가능한 요청이 있을 때의 요청 카드 (stateless).
 * 요청자 이름·응답 여부 등은 [RequestCardUiState]로 받는다.
 */
@Composable
fun ActiveRequestCard(
    state: RequestCardUiState,
    onNavigateToResponse: (String) -> Unit = {},
    onNavigateToRequestDetail: (Request) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = MainColor)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.Green, CircleShape)
                        .align(Alignment.TopStart)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                // 이름을 불러오기 전에는 "님의 요청"만 덩그러니 보이지 않게 한다.
                text = if (state.requesterName.isNotEmpty()) "${state.requesterName}님의 요청" else "요청",
                fontSize = 16.sp,
                fontFamily = NanumSquareBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = state.request.requestMessage,
                fontSize = 14.sp,
                fontFamily = NanumSquareRegular,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(30.dp))

            DynamicButton(
                request = state.request,
                isQuestioner = state.isQuestioner,
                hasUserResponded = state.hasUserResponded,
                isCheckingResponse = state.isCheckingResponse,
                onRespondClick = onNavigateToResponse,
                onShowResponsesClick = onNavigateToRequestDetail
            )
        }
    }
}

private val previewCardState = RequestCardUiState(
    request = Request(
        requestId = "preview_request",
        requestUserDocumentID = "user_a",
        requestMessage = "오늘 점심 사진 보여주세요!",
        answerDeadline = System.currentTimeMillis() + 25 * 60 * 1000L
    ),
    requesterName = "철수",
    isQuestioner = false,
    hasUserResponded = false,
    isCheckingResponse = false
)

@Preview(name = "응답 가능", showBackground = true)
@Composable
private fun ActiveRequestCardPreview() {
    ActiveRequestCard(state = previewCardState)
}

@Preview(name = "이름/응답 여부 로딩 중", showBackground = true)
@Composable
private fun ActiveRequestCardLoadingPreview() {
    ActiveRequestCard(state = previewCardState.copy(requesterName = "", isCheckingResponse = true))
}

@Preview(name = "이미 응답함", showBackground = true)
@Composable
private fun ActiveRequestCardRespondedPreview() {
    ActiveRequestCard(state = previewCardState.copy(hasUserResponded = true))
}
