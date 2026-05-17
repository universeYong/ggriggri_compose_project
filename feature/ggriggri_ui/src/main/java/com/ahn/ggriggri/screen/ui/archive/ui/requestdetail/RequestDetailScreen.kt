package com.ahn.ggriggri.screen.archive.requestdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.ahn.domain.model.Request
import com.ahn.domain.model.Response
import com.ahn.ggriggri.screen.ui.archive.viewmodel.requestdetail.RequestDetailViewModel
import theme.GgriggriTheme
import theme.MainColor
import theme.NanumSquareBold
import theme.NanumSquareRegular
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RequestDetailScreen(
    requestId: String,
    onNavigateBack: () -> Unit,
    viewModel: RequestDetailViewModel = hiltViewModel(),
) {
    val request by viewModel.request.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(requestId) {
        if (requestId.isNotBlank()) {
            viewModel.loadRequest(requestId)
        }
    }

    GgriggriTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F5)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                // 메인 콘텐츠
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "오류: $error",
                                color = Color.Red,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    request != null -> {
                        RequestDetailContent(
                            request = request!!,
                            viewModel = viewModel
                        )
                    }

                    requestId.isBlank() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "요청 ID가 없습니다.",
                                color = Color.Red,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "요청을 찾을 수 없습니다.",
                                color = Color.Red,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestDetailContent(
    request: Request,
    viewModel: RequestDetailViewModel,
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userProfileImage by viewModel.userProfileImage.collectAsStateWithLifecycle()
    val responses by viewModel.responses.collectAsStateWithLifecycle()
    val responseUserInfo by viewModel.responseUserInfo.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 요청 정보 카드
            item {
                RequestInfoCard(
                    request = request,
                    userName = userName,
                    userProfileImage = userProfileImage
                )
            }
        }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp)
                    .background(Color(0xFFF0F0F0))
            )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 응답 목록
            if (responses.isNotEmpty()) {

                items(responses) { response ->
                    ResponseCard(
                        response = response,
                        viewModel = viewModel,
                        responseUserInfo = responseUserInfo
                    )
                }
            } else {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "아직 답변이 없습니다.",
                                fontSize = 14.sp,
                                fontFamily = NanumSquareRegular,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestInfoCard(
    request: Request,
    userName: String?,
    userProfileImage: String?,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MainColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 사용자 정보
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (userProfileImage != null) {
                    AsyncImage(
                        model = userProfileImage,
                        contentDescription = "프로필 이미지",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFFE0E0E0), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "프로필 이미지",
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF666666)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = userName ?: "알 수 없음",
                        fontSize = 16.sp,
                        fontFamily = NanumSquareBold,
                        color = Color.Black
                    )
                    Text(
                        text = formatTime(request.requestTime),
                        fontSize = 12.sp,
                        fontFamily = NanumSquareRegular,
                        color = Color(0xFF666666)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 요청 메시지
            Text(
                text = request.requestMessage,
                fontSize = 15.sp,
                fontFamily = NanumSquareRegular,
                color = Color.Black,
                lineHeight = 22.sp
            )

            // 이미지가 있는 경우
            if (request.requestImage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = request.requestImage,
                    contentDescription = "요청 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun ResponseCard(
    response: Response,
    viewModel: RequestDetailViewModel,
    responseUserInfo: Map<String, Pair<String, String>>,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 응답자 정보 (프로필 이미지 + 이름)
            val userInfo = responseUserInfo[response.responseUserDocumentID]
            val (userName, userProfileImage) = userInfo ?: Pair("알 수 없는 사용자", "")

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                // 프로필 이미지
                if (userProfileImage.isNotEmpty()) {
                    AsyncImage(
                        model = userProfileImage,
                        contentDescription = "프로필 이미지",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // 기본 프로필 아이콘
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0))
                            .border(1.dp, Color(0xFFCCCCCC), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "기본 프로필",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    // 사용자 이름
                    Text(
                        text = userName,
                        fontSize = 16.sp,
                        fontFamily = NanumSquareBold,
                        color = Color.Black
                    )
                    // 응답 시간
                    Text(
                        text = formatTime(response.responseTime),
                        fontSize = 12.sp,
                        fontFamily = NanumSquareRegular,
                        color = Color(0xFF888888)
                    )
                }
            }

            // 응답 메시지
            if (response.responseMessage.isNotEmpty()) {
                Text(
                    text = response.responseMessage,
                    fontSize = 14.sp,
                    fontFamily = NanumSquareRegular,
                    color = Color.Black,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // 응답 이미지
            if (response.responseImage.isNotEmpty()) {
                AsyncImage(
                    model = response.responseImage,
                    contentDescription = "응답 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(8.dp))
            }


        }
    }
}

private fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("MM월 dd일 HH:mm", Locale.getDefault())
    return format.format(date)
}