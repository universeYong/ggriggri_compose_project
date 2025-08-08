package com.ahn.ggriggri.screen.archive.questionlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuestionListScreen() {
    // 샘플 데이터
    val questionList = listOf(
        Triple("#005 이 그룹과 함께 하며 가장 화난 순간은?", "", "2025.02.07"),
        Triple("#004 가장 방구석에 있을 것 같은 사람은?", "", "2025.02.06"),
        Triple("#003 엄마 가장 말 안들을 것 같은 사람은?", "", "2025.02.05"),
        Triple("#002 각자의 첫 인상을 말해라", "", "2025.02.04"),
        Triple("#001 자기 소개하기", "", "2025.02.03")
    )

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(questionList) { index, (title, _, data) ->
            ListItem(
                headlineContent = {Text(title)},
                trailingContent = {Text(data)},
                modifier = Modifier.clickable{},
            )
            if (index < questionList.size - 1) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }
        }
    }

}