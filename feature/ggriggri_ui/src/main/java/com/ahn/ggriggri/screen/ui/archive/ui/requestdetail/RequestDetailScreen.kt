package com.ahn.ggriggri.screen.archive.requestdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahn.common_ui.R

// 데이터 모델 정의
data class RequestDetailData(
    val userName: String,
    val userProfileImage: Int,
    val requestTime: String,
    val messageImage: Int?, // 이미지가 없을 수도 있으므로 Nullable
    val messageText: String
)

data class Comment(
    val id: Int,
    val userName: String,
    val userProfileImage: Int,
    val commentText: String
)

// XML의 스타일을 대체하기 위한 TextStyle
val customTextRegularStyle = TextStyle(fontSize = 15.sp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailScreen(
    requestData: RequestDetailData,
    comments: List<Comment>,
    onNavigateUp: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("요청 상세") }, // 툴바 타이틀 (예시)
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        // NestedScrollView + RecyclerView -> LazyColumn으로 대체
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // 1. 요청 상세 카드
            item {
                RequestCard(requestData = requestData)
            }

            // 2. 구분선
            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            }

            // 3. 댓글 목록
            items(comments) { comment ->
                CommentItem(comment = comment)
            }
        }
    }
}

/**
 * 요청 상세 내용을 담는 카드 Composable
 */
@Composable
fun RequestCard(requestData: RequestDetailData) {
    // background_request_detail을 대체하는 Modifier
    val cardModifier = Modifier
        .padding(20.dp) // layout_margin="20dp"
        .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .background(MaterialTheme.colorScheme.surface)


    Column(modifier = cardModifier) {
        // 프로필 섹션
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = requestData.userProfileImage),
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape) // rounded_image
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = requestData.userName,
                style = customTextRegularStyle
            )
            Text(
                text = requestData.requestTime,
                modifier = Modifier.weight(1f), // layout_weight="1"
                textAlign = TextAlign.End, // textAlignment="viewEnd"
                style = customTextRegularStyle
            )
        }

        // 메시지 섹션
        Column {
            requestData.messageImage?.let { imageRes ->
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "메시지 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop // scaleType="centerCrop"
                )
            }
            Text(
                text = requestData.messageText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                style = customTextRegularStyle
            )
        }
    }
}

/**
 * 댓글 목록의 각 항목 Composable
 */
@Composable
fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = comment.userProfileImage),
            contentDescription = "${comment.userName} 프로필",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = comment.userName, style = MaterialTheme.typography.titleSmall)
            Text(
                text = comment.commentText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
        }
    }
}


@Preview(showSystemUi = true)
@Composable
fun RequestDetailScreenPreview() {
    val dummyRequest = RequestDetailData(
        userName = "정지은",
        userProfileImage = R.drawable.main_logo,
        requestTime = "1시간 전",
        messageImage = R.drawable.kakao, // 미리보기를 위한 샘플 이미지
        messageText = "이거 너무 재밌지 않아요? 다들 한 번씩 해보세요! 완전 추천합니다. 😀"
    )
    val dummyComments = listOf(
        Comment(1, "안성용", R.drawable.google, "오 재밌어 보이네요"),
        Comment(2, "차승환", R.drawable.naver, "저도 해볼래요!")
    )
    RequestDetailScreen(
        requestData = dummyRequest,
        comments = dummyComments,
        onNavigateUp = {}
    )
}