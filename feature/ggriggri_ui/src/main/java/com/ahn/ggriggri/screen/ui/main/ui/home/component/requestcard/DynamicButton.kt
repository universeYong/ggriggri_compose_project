package com.ahn.ggriggri.screen.ui.main.ui.home.component.requestcard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahn.domain.model.Request
import kotlinx.coroutines.delay
import com.ahn.common_ui.theme.NanumSquareBold

/**
 * 요청 카드 하단 버튼 (stateless).
 *
 * 요청이 없으면 "요청하기", 있으면 질문자/응답 여부/마감 여부에 따라 "응답보기"·"응답하기 (남은 시간)"·"답변 마감"을 보여준다.
 * 필요한 값은 모두 파라미터로 받고 ViewModel에 접근하지 않는다.
 *
 * @param isCheckingResponse 응답 여부를 아직 모르는 동안 true. 이때는 잘못된 이동을 막기 위해 버튼을 비활성화한다.
 */
@Composable
fun DynamicButton(
    request: Request?,
    isQuestioner: Boolean,
    hasUserResponded: Boolean,
    isCheckingResponse: Boolean,
    onCreateRequestClick: () -> Unit = {},
    onRespondClick: (String) -> Unit = {},
    onShowResponsesClick: (Request) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // 남은 시간 표시용 현재 시각. 1초마다 갱신하되 마감 시각에 도달하면 멈춘다.
    // 요청(마감 시각)이 바뀌면 다시 시작한다.
    val currentTime by produceState(
        initialValue = System.currentTimeMillis(),
        request?.requestId,
        request?.answerDeadline
    ) {
        val deadline = request?.answerDeadline ?: return@produceState
        while (true) {
            val now = System.currentTimeMillis()
            value = now
            if (now >= deadline) break
            // 다음 초 경계까지만 기다려 표시가 밀리지 않게 하고, 마감 직전에는 마감 시각까지만 기다린다.
            delay(minOf(1000L - now % 1000L, deadline - now))
        }
    }

    // 실시간으로 답변 가능 여부 확인
    val isCurrentlyAnswerable = request != null && currentTime < request.answerDeadline

    val buttonColor = when {
        request == null -> Color.White
        isQuestioner -> Color.Blue
        hasUserResponded -> Color.Green
        isCheckingResponse -> Color.Gray
        isCurrentlyAnswerable -> Color.White
        else -> Color.Gray
    }

    val isEnabled = when {
        request == null -> true
        isQuestioner -> true
        hasUserResponded -> true
        isCheckingResponse -> false
        else -> isCurrentlyAnswerable
    }

    val buttonText = when {
        request == null -> "요청하기"
        isQuestioner -> "응답보기"
        hasUserResponded -> "응답보기"
        isCheckingResponse -> "확인 중..."
        isCurrentlyAnswerable -> "응답하기 (${remainingTimeText(request.answerDeadline, currentTime)})"
        else -> "답변 마감"
    }

    Button(
        onClick = {
            when {
                request == null -> onCreateRequestClick()
                isQuestioner || hasUserResponded -> onShowResponsesClick(request)
                isCheckingResponse -> Unit
                isCurrentlyAnswerable -> onRespondClick(request.requestId)
                else -> Unit
            }
        },
        modifier = modifier.fillMaxWidth(),
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

/** 마감까지 남은 시간을 "m:ss" 또는 "h:mm:ss"로 만든다. */
private fun remainingTimeText(deadline: Long, now: Long): String {
    val remaining = maxOf(0L, deadline - now)
    val remainingMinutes = (remaining / (60 * 1000)).toInt()
    val remainingSeconds = ((remaining % (60 * 1000)) / 1000).toInt()

    return when {
        remaining <= 0 -> "답변 마감"
        remainingMinutes < 60 -> "${remainingMinutes}:${remainingSeconds.toString().padStart(2, '0')}"
        else -> "${remainingMinutes / 60}:${(remainingMinutes % 60).toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}"
    }
}

private fun previewRequest(deadlineOffsetMillis: Long) = Request(
    requestId = "preview_request",
    requestUserDocumentID = "user_a",
    requestMessage = "오늘 점심 사진 보여주세요!",
    answerDeadline = System.currentTimeMillis() + deadlineOffsetMillis
)

@Preview(name = "요청 없음", showBackground = true)
@Composable
private fun DynamicButtonNoRequestPreview() {
    DynamicButton(
        request = null,
        isQuestioner = false,
        hasUserResponded = false,
        isCheckingResponse = false
    )
}

@Preview(name = "응답하기(남은 시간)", showBackground = true)
@Composable
private fun DynamicButtonAnswerablePreview() {
    DynamicButton(
        request = previewRequest(25 * 60 * 1000L),
        isQuestioner = false,
        hasUserResponded = false,
        isCheckingResponse = false
    )
}

@Preview(name = "응답 여부 확인 중", showBackground = true)
@Composable
private fun DynamicButtonCheckingPreview() {
    DynamicButton(
        request = previewRequest(25 * 60 * 1000L),
        isQuestioner = false,
        hasUserResponded = false,
        isCheckingResponse = true
    )
}

@Preview(name = "질문자/응답 완료", showBackground = true)
@Composable
private fun DynamicButtonQuestionerPreview() {
    DynamicButton(
        request = previewRequest(25 * 60 * 1000L),
        isQuestioner = true,
        hasUserResponded = false,
        isCheckingResponse = false
    )
}

@Preview(name = "답변 마감", showBackground = true)
@Composable
private fun DynamicButtonClosedPreview() {
    DynamicButton(
        request = previewRequest(-60 * 1000L),
        isQuestioner = false,
        hasUserResponded = false,
        isCheckingResponse = false
    )
}
