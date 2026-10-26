package com.ahn.ggriggri.screen.ui.archive.ui.requestdetail

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ahn.common_ui.theme.GgriggriTheme
import com.ahn.common_ui.theme.NanumSquareBold
import com.ahn.common_ui.theme.NanumSquareRegular
import com.ahn.common_ui.theme.OnMainColorAvatarPlaceholder
import com.ahn.common_ui.theme.OnMainColorSecondary
import com.ahn.ggriggri.screen.ui.archive.viewmodel.requestdetail.RequestDetailItem
import com.ahn.ggriggri.screen.ui.archive.viewmodel.requestdetail.RequestDetailUiState
import com.ahn.ggriggri.screen.ui.archive.viewmodel.requestdetail.RequestDetailViewModel
import com.ahn.ggriggri.screen.ui.archive.viewmodel.requestdetail.ResponseItem

/** 진입 Composable: ViewModel 상태를 수집해 stateless 화면에 값만 넘긴다. */
@Composable
fun RequestDetailScreen(
    requestId: String,
    onNavigateBack: () -> Unit,
    viewModel: RequestDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(requestId) {
        if (requestId.isNotBlank()) {
            viewModel.loadRequest(requestId)
        }
    }

    RequestDetailContent(
        state = uiState,
        isRequestIdMissing = requestId.isBlank(),
    )
}

@Composable
fun RequestDetailContent(
    state: RequestDetailUiState,
    isRequestIdMissing: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = GgriggriTheme.colors.surfaceSubtle
    ) {
        val request = state.request
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> CenterMessage("오류: ${state.error}")

            request != null -> RequestDetailList(request = request, responses = state.responses)

            isRequestIdMissing -> CenterMessage("요청 ID가 없습니다.")

            else -> CenterMessage("요청을 찾을 수 없습니다.")
        }
    }
}

@Composable
private fun CenterMessage(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/** 요청 본문과 응답 목록을 하나의 LazyColumn(스크롤 하나)으로 보여준다. */
@Composable
private fun RequestDetailList(
    request: RequestDetailItem,
    responses: List<ResponseItem>,
) {
    val itemPadding = Modifier.padding(horizontal = 20.dp)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item(key = "request_info") {
            RequestInfoCard(request = request, modifier = itemPadding)
        }

        // 구분선은 좌우 여백 없이 화면 폭 전체
        item(key = "divider") {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp)
                    .background(GgriggriTheme.colors.sectionDivider)
            )
        }

        if (responses.isNotEmpty()) {
            items(responses, key = { it.key }) { response ->
                ResponseCard(response = response, modifier = itemPadding)
            }
        } else {
            item(key = "empty_responses") {
                Card(
                    modifier = itemPadding
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = GgriggriTheme.colors.surfaceSubtle),
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
                            color = GgriggriTheme.colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RequestInfoCard(
    request: RequestDetailItem,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
                if (!request.authorProfileImage.isNullOrEmpty()) {
                    AsyncImage(
                        model = request.authorProfileImage,
                        contentDescription = "프로필 이미지",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .border(2.dp, OnMainColorAvatarPlaceholder, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(OnMainColorAvatarPlaceholder),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "프로필 이미지",
                            modifier = Modifier.size(24.dp),
                            tint = OnMainColorSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = request.authorName ?: "알 수 없음",
                        fontSize = 16.sp,
                        fontFamily = NanumSquareBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = request.timeText,
                        fontSize = 12.sp,
                        fontFamily = NanumSquareRegular,
                        color = OnMainColorSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 요청 메시지
            Text(
                text = request.message,
                fontSize = 15.sp,
                fontFamily = NanumSquareRegular,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                lineHeight = 22.sp
            )

            // 이미지가 있는 경우
            if (request.imageUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = request.imageUrl,
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
    response: ResponseItem,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(containerColor = GgriggriTheme.colors.surfaceRaised),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 응답자 정보 (프로필 이미지 + 이름)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                if (response.authorProfileImage.isNotEmpty()) {
                    AsyncImage(
                        model = response.authorProfileImage,
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
                            .background(GgriggriTheme.colors.placeholder)
                            .border(1.dp, GgriggriTheme.colors.borderLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "기본 프로필",
                            tint = GgriggriTheme.colors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = response.authorName,
                        fontSize = 16.sp,
                        fontFamily = NanumSquareBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = response.timeText,
                        fontSize = 12.sp,
                        fontFamily = NanumSquareRegular,
                        color = GgriggriTheme.colors.textTertiary
                    )
                }
            }

            // 응답 메시지
            if (response.message.isNotEmpty()) {
                Text(
                    text = response.message,
                    fontSize = 14.sp,
                    fontFamily = NanumSquareRegular,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // 응답 이미지
            if (response.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = response.imageUrl,
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
