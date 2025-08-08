package com.ahn.ggrigggri.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ahn.common_ui.components.CommonButton

@Composable
fun BottomBarButton(
    buttonTitle: BottomBarButtonName,
    onClick: () -> Unit
) {
    CommonButton(
        onClick = onClick,
    ) {
        Text(text = buttonTitle.title)
    }
}