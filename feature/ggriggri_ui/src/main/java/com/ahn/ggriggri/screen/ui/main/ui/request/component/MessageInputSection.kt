package com.ahn.ggriggri.screen.ui.main.ui.request.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MessageInputSection(
    message: String,
    onMessageChanged: (String) -> Unit,
    currentLength: Int,
    maxLength: Int
) {
    Column {
        TextField(
            value = message,
            onValueChange = onMessageChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("요청을 입력하세요.") },
            maxLines = 4,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$currentLength/$maxLength",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.End)
        )
    }
}