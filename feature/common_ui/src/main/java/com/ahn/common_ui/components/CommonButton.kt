package com.ahn.common_ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import theme.MainColor

@Composable
fun CommonButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp),
        enabled = enabled,
        content = content,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            // 버튼색을 테마에 정의
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = Color.Gray, // 예시 색상 (비활성)
            disabledContentColor = Color.LightGray
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,  // 기본 상태 그림자
            pressedElevation = 0.dp,   // 눌렸을 때 그림자
            disabledElevation = 0.dp // 비활성화 상태 그림자
        )
    )
}