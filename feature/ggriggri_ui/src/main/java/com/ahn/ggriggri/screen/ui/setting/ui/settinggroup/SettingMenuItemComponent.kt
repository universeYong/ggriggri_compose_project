package com.ahn.ggriggri.screen.setting.settinggroup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theme.NanumSquareRegular


/**
 * @param text 메뉴에 표시될 텍스트
 * @param onClick 메뉴 클릭 시 실행될 람다 함수
 */
@Composable
fun SettingMenuItem(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // clickable과 selectableItemBackground 효과
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // 텍스트와 아이콘을 양 끝으로 분리
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontFamily = NanumSquareRegular // nanumsquareregular
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null, // 장식용 아이콘
            modifier = Modifier.size(18.dp),
            tint = Color.Gray // gray_500
        )
    }
}