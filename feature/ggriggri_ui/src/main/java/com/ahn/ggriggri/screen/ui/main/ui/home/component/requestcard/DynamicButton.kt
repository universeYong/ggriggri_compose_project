package com.ahn.ggriggri.screen.ui.main.ui.home.component.requestcard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.ahn.domain.model.Request
import com.ahn.ggriggri.screen.ui.main.viewmodel.HomeViewModel
import theme.NanumSquareBold

@Composable
fun DynamicButton(
    request: Request?,
    viewModel: HomeViewModel,
    onNavigateToRequest: () -> Unit = {},
    onNavigateToResponse: (String) -> Unit = {},
    onNavigateToRequestDetail: (Request) -> Unit = {},
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentUserId = currentUser?.userId

    // 현재 사용자가 응답했는지 확인하는 상태
    var hasUserResponded by remember { mutableStateOf(false) }
    
    // 실시간 시간 업데이트를 위한 상태
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // Request가 변경될 때마다 응답 여부 확인
    LaunchedEffect(request?.requestId) {
        if (request?.requestId != null && currentUserId != null) {
            hasUserResponded = viewModel.hasUserResponded(request.requestId)
        }
    }
    
    // 실시간 시간 업데이트 (1초마다)
    LaunchedEffect(request?.requestId) {
        if (request?.isAnswerable() == true) {
            while (true) {
                currentTime = System.currentTimeMillis()
                delay(1000) // 1초마다 업데이트
            }
        }
    }

    // 실시간 남은 시간 계산 함수
    fun getRealTimeRemainingText(): String {
        if (request == null) return ""
        val remaining = maxOf(0, request.answerDeadline - currentTime)
        val remainingMinutes = (remaining / (60 * 1000)).toInt()
        val remainingSeconds = ((remaining % (60 * 1000)) / 1000).toInt()
        
        return when {
            remaining <= 0 -> "답변 마감"
            remainingMinutes < 60 -> "${remainingMinutes}:${remainingSeconds.toString().padStart(2, '0')}"
            else -> "${remainingMinutes / 60}:${(remainingMinutes % 60).toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}"
        }
    }

    // 실시간으로 답변 가능 여부 확인
    val isCurrentlyAnswerable = request?.let { 
        currentTime < it.answerDeadline 
    } ?: false

    val buttonColor = when {
        request == null -> Color.White
        request.isQuestioner(currentUserId) -> Color.Blue
        hasUserResponded -> Color.Green
        isCurrentlyAnswerable -> Color.White
        else -> Color.Gray
    }

    val isEnabled = when {
        request == null -> true
        request.isQuestioner(currentUserId) -> true
        else -> isCurrentlyAnswerable || hasUserResponded
    }

    val buttonText = when {
        request == null -> "요청하기"
        // 질문자이면
        request.isQuestioner(currentUserId) -> {
            "응답보기"
        }
        // 답변자이면
        else -> {
            if (hasUserResponded) {
                "응답보기"
            } else if (isCurrentlyAnswerable) {
                "응답하기 (${getRealTimeRemainingText()})"
            } else {
                "답변 마감"
            }
        }
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

                hasUserResponded -> {
                    viewModel.showResponse(request, onNavigateToRequestDetail)
                }

                isCurrentlyAnswerable -> {
                    onNavigateToResponse(request.requestId)
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