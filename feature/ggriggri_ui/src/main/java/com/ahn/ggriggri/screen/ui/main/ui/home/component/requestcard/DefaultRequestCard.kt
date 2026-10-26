package com.ahn.ggriggri.screen.ui.main.ui.home.component.requestcard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import theme.MainColor
import theme.NanumSquareBold
import theme.NanumSquareRegular

/**
 * 답변 가능한 요청이 없을 때의 요청 카드 (stateless).
 */
@Composable
fun DefaultRequestCard(
    onNavigateToRequest: () -> Unit = {},
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
            Text(
                text = "요청",
                fontSize = 16.sp,
                fontFamily = NanumSquareBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "요청이 없습니다!!!",
                fontSize = 14.sp,
                fontFamily = NanumSquareRegular,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(30.dp))

            DynamicButton(
                request = null,
                isQuestioner = false,
                hasUserResponded = false,
                isCheckingResponse = false,
                onCreateRequestClick = onNavigateToRequest
            )
        }
    }
}

@Preview(name = "요청 없음", showBackground = true)
@Composable
private fun DefaultRequestCardPreview() {
    DefaultRequestCard()
}
