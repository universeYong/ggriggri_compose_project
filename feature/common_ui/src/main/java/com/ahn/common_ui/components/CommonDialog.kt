package com.ahn.common_ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun CommonDialog(
    // 다이얼로그 닫는 요청
    onDismissRequest: () -> Unit,
    // 확인버튼 눌렀을 때
    onConfirmation: () -> Unit,
    // 제목
    title: String,
    // 본문 설명. 제목만으로 부족할 때만 전달한다.
    message: String? = null,
    // 아이콘
    icon: ImageVector = Icons.Default.Info,
    confirmButtonText: String = "확인",
    dismissButtonText: String? = "취소",
    onDismissClick: (() -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
        ){
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                if (message != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(24.dp))

                if (onDismissClick != null && dismissButtonText != null) {
                    // two button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ){
                        CommonButton(
                            onClick = onDismissClick,
                            modifier = Modifier.weight(1f)
                        ) { Text(dismissButtonText)}
                        CommonButton(
                            onClick = onConfirmation,
                            modifier = Modifier.weight(1f)
                        ) { Text(confirmButtonText) }
                    }
                } else {
                    // one button
                   CommonButton(
                       onClick = onConfirmation,
                       modifier = Modifier.fillMaxWidth()
                   ) {
                       Text(confirmButtonText)
                   }
                }
            }
        }
    }
}

/**
 * two Button
 * CommonDialog(
 *     onDismissRequest = { /* 다이얼로그 닫기 */ },
 *     onConfirmation = { /* 사용하기 로직 */ },
 *     title = "사용 가능한 아이디 입니다",
 *     confirmButtonText = "사용하기",
 *     // 아래 두 파라미터를 모두 전달하면 버튼이 2개 표시됨
 *     dismissButtonText = "취소",
 *     onDismissClick = { /* 다이얼로그 닫기 */ }
 * )
 *
 *
 *
 * one Button
 * CommonDialog(
 *     onDismissRequest = { /* 다이얼로그 닫기 */ },
 *     onConfirmation = { /* 확인 버튼 로직 (다이얼로그 닫기) */ },
 *     title = "해당정보를 가진 사용자를 찾을 수 없습니다",
 *     confirmButtonText = "확인"
 *     // onDismissClick 파라미터를 전달하지 않음
 * )
 *
 *
 *
 * 되돌릴 수 없는 동작을 확인받는 경우 — message로 결과를 설명한다
 * CommonDialog(
 *     onDismissRequest = { showDialog = false },
 *     onConfirmation = { showDialog = false; viewModel.doSomething() },
 *     title = "그룹 비밀번호를 변경할까요?",
 *     message = "변경 후에는 새 비밀번호로만 그룹에 참여할 수 있습니다.",
 *     confirmButtonText = "변경",
 *     dismissButtonText = "취소",
 *     onDismissClick = { showDialog = false }
 * )
 */