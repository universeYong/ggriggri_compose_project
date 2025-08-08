package com.ahn.ggriggri.screen.archive.requestlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RequestListScreen() {
    // 샘플 데이터
    val requestList = listOf(
        Triple("오늘 점심 뭐 먹음?", "차승범님", "2025.02.07 12:20"),
        Triple("얘들아 하늘 사진 좀 찍어봐", "안성원님", "2025.02.07 09:54"),
        Triple("지금 뭐하는지 인증 고고", "차승범님", "2025.02.06 23:40"),
        Triple("집에 있는 강아지 사진 실시간으로 올려봐", "정재현님", "2025.02.06 19:22"),
        Triple("다들 어디? 뭐해?", "차승범님", "2025.02.06 15:20")
    )

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(requestList) { index, (title, user, data) ->
            ListItem(
                headlineContent = {Text(title)},
                supportingContent = {Text(user)},
                trailingContent = {Text(data)},
                modifier = Modifier.clickable{}
            )
            if (index < requestList.size - 1) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }
        }
    }
}