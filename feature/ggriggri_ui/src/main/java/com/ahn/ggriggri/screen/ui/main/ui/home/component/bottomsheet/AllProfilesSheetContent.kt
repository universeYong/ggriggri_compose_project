package com.ahn.ggriggri.screen.ui.main.ui.home.component.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ahn.common_ui.R
import com.ahn.ggriggri.screen.ui.main.viewmodel.Profile
import theme.NanumSquareRegular

@Composable
fun AllProfilesSheetContent(
    profiles: List<Profile>,
    onCloseSheet: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp) // 시스템 네비게이션 바와의 간격 등을 고려
    ) {
        // 닫기 버튼과 타이틀
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "전체 그룹원",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = NanumSquareRegular // 폰트 적용
            )
            IconButton(onClick = onCloseSheet) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "닫기"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2열 프로필 목록
        if (profiles.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // 2열로 고정
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp), // 아이템 간 가로 간격
                verticalArrangement = Arrangement.spacedBy(16.dp)     // 아이템 간 세로 간격
            ) {
                items(profiles) { profile ->
                    ProfileGridItem(profile = profile)
                }
            }
        } else {
            Text(
                text = "표시할 그룹원이 없습니다.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                fontFamily = NanumSquareRegular
            )
        }
        // BottomSheet의 높이가 너무 커지는 것을 방지하기 위해
        // LazyVerticalGrid에 적절한 height Modifier를 추가하거나,
        // Column에 height 제한을 둘 수 있습니다.
        // 예: Modifier.heightIn(max = 400.dp)
    }
}

@Composable
fun ProfileGridItem(profile: Profile) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        AsyncImage(
            model = profile.profileImageUrl,
            contentDescription = "프로필 ${profile.name}",
            modifier = Modifier
                .size(80.dp) // 이미지 크기 조절
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.outline_account_circle_24), // 기본 이미지 리소스
            error = painterResource(id = R.drawable.outline_account_circle_24)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = profile.name,
            fontSize = 14.sp,
            fontFamily = NanumSquareRegular, // 폰트 적용
            maxLines = 1,
            overflow = TextOverflow.Ellipsis // 이름이 길 경우 ... 처리
        )
    }
}