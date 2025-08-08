package com.ahn.ggriggri.screen.auth.login.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit

@Composable
fun LoginText(
    text: String,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier
    ) {
    Text(
        text = text,
        color = color,
        style = style,
        fontFamily = fontFamily,
        modifier = modifier
    )
}