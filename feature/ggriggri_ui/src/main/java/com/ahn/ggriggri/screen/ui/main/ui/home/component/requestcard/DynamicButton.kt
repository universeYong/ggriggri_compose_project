package com.ahn.ggriggri.screen.ui.main.ui.home.component.requestcard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahn.domain.model.Request
import com.ahn.ggriggri.screen.ui.main.viewmodel.HomeViewModel
import theme.NanumSquareBold

@Composable
fun DynamicButton(
    request: Request?,
    viewModel: HomeViewModel,
    onNavigateToRequest: () -> Unit = {},
    onNavigateToResponse: (Request) -> Unit = {},
    onNavigateToRequestDetail: (Request) -> Unit = {},
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentUserId = currentUser?.userId

    val buttonText = when {
        request == null -> "요청하기"
        // 질문자이면
        request.isQuestioner(currentUserId) -> {
            "응답보기"
        }
        // 답변자이면
        else -> {
            if (request.hasAnswer) {
                "응답보기"
            } else if (request.isAnswerable()) {
                "응답하기 (${request.getRemainingTimeText()})"
            } else {
                "답변 마감"
            }
        }
    }

    val buttonColor = when {
        request == null -> Color.Gray
        request.isQuestioner(currentUserId) -> Color.Blue
        request.hasAnswer -> Color.Green
        request.isAnswerable() -> Color.White
        else -> Color.Gray
    }

    val isEnabled = when {
        request == null -> true
        request.isQuestioner(currentUserId) -> true
        else -> request.isAnswerable() || request.hasAnswer
    }

    Button(
        onClick = {
            when {
                request == null -> {
                    viewModel.navigateToCreateRequest(onNavigateToRequest)
                }
                request.isQuestioner(currentUserId) -> {
                    viewModel.showResponse(request, onNavigateToRequestDetail)
                }

                request.hasAnswer -> {
                    viewModel.showResponse(request, onNavigateToRequestDetail)
                }

                request.isAnswerable() -> {
                    viewModel.startAnswering(request,onNavigateToResponse)
                }
                else -> {}
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = buttonText,
            fontSize = 16.sp,
            fontFamily = NanumSquareBold,
            color = if (buttonColor == Color.White) Color.Black else Color.White
        )
    }
}