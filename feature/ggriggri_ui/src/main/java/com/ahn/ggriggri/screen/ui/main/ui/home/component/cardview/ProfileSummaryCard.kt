package com.ahn.ggriggri.screen.ui.main.ui.home.component.cardview

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ahn.common_ui.R
import com.ahn.ggriggri.screen.ui.main.viewmodel.Profile
import theme.MainColor
import theme.NanumSquareRegular

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSummaryCard(profiles: List<Profile>, onSeeAllClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = MainColor)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                profiles.take(profiles.size).forEach { profile ->
                    Log.d(
                        "ProfileSummaryCard",
                        "Profile: ${profile.name}, URL: ${profile.profileImageUrl}"
                    ) // 로그 추가
                    AsyncImage(
                        model = profile.profileImageUrl,
                        contentDescription = "프로필 ${profile.name}",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.outline_account_circle_24),
                        error = painterResource(id = R.drawable.outline_account_circle_24),
                        onError = { errorResult ->
                            Log.e(
                                "AsyncImageError",
                                "Failed to load image: ${profile.profileImageUrl}",
                                errorResult.result.throwable
                            )
                        }
                    )
                }
            }
            Text(
                text = "전체보기",
                modifier = Modifier.clickable(onClick = onSeeAllClick),
                fontFamily = NanumSquareRegular,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}