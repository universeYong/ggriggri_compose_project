package com.ahn.ggriggri.screen.setting.settinggroup

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theme.GgriggriTheme
import theme.NanumSquareRegular

@Composable
fun SettingGroupScreen() {
    GgriggriTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 그룹 코드 섹션
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "그룹 코드 : ",
                    fontSize = 20.sp,
                    fontFamily = NanumSquareRegular // nanumsquareregular
                )
                Text(
                    text = "123456",
                    fontSize = 20.sp,
                    fontFamily = NanumSquareRegular // nanumsquarelight
                )
            }

            // 구분선 (View)
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp)
                    .background(Color(0xFFF0F0F0)) // gray_100에 해당하는 색상
            )

            // 설정 메뉴 항목들
            SettingMenuItem(
                text = "그룹 비밀번호 변경",
                onClick = {}
            )
            SettingMenuItem(
                text = "그룹명 변경",
                onClick = {}
            )
            SettingMenuItem(
                text = "그룹 나가기",
                onClick = {}
            )
        }
    }
}


