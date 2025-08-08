package com.ahn.ggriggri.screen.main.home

import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.ahn.common_ui.R
import com.bumptech.glide.Glide

// 데이터 모델 정의
data class Profile(val id: Int, val imageRes: Int)
data class Question(val content: String, val emojiUrl: String, val buttonText: String)
data class Request(val content: String, val buttonText: String, val isActive: Boolean)
data class Memory(val id: Int, val imageRes: Int)

// XML의 폰트 스타일을 대체하기 위한 변수 (필요시 FontFamily 정의 추가)
val nanumSquareRegular = FontWeight.Normal
val nanumSquareBold = FontWeight.Bold

val imageUrl = "https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/refs/heads/master/Emojis/People/Baby%20Angel.png"
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
//    // ViewModel 등에서 전달받을 상태
//    profiles: List<Profile>,
//    question: Question,
//    request: Request,
//    memories: List<Memory>,
//    onSeeAllProfilesClick: () -> Unit,
//    onAnswerClick: () -> Unit,
//    onRespondClick: () -> Unit
) {
    val dummyProfiles = listOf(
        Profile(1, R.drawable.google),
        Profile(2, R.drawable.naver),
        Profile(3, R.drawable.kakao),
        Profile(4, R.drawable.main_logo_background)
    )
    val dummyQuestion = Question(
        content = "이 그룹과 함께 하며 가장 화난 순간은?",
        emojiUrl = imageUrl,
        buttonText = "답변하기"
    )
    val dummyRequest = Request(
        content = "야 오늘 월요일인데 뭐하고있냐 난...",
        buttonText = "응답하기",
        isActive = true
    )
    val dummyMemories = listOf(
        Memory(1, R.drawable.kakao),
        Memory(2, R.drawable.naver),
        Memory(3, R.drawable.google)
    )

    val profiles = dummyProfiles
    val question = dummyQuestion
    val request = dummyRequest
    val memories = dummyMemories
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("홈", style = MaterialTheme.typography.titleLarge) }, // 타이틀 예시
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        // ScrollView + ConstraintLayout -> LazyColumn으로 대체
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 프로필 목록 카드
            item {
                ProfileSummaryCard(
                    profiles = profiles,
                    onSeeAllClick = {}
                )
            }
            // 오늘의 질문 카드
            item {
                TodayQuestionCard(
                    question = question,
                    onAnswerClick = {}
                )
            }
            // 요청 카드
            item {
                RequestCard(
                    request = request,
                    onRespondClick = {}
                )
            }
            // 추억 캐러셀
            item {
                MemoriesCarousel(memories = memories)
            }
        }
    }
}

/**
 * 프로필 목록을 보여주는 카드 (cvHomeProfileList)
 */
@Composable
fun ProfileSummaryCard(profiles: List<Profile>, onSeeAllClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)) // mainColor 예시
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ConstraintLayout의 Flow -> FlowRow로 대체
            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 4 // 예시, 필요에 따라 조절
            ) {
                profiles.take(4).forEach { profile ->
                    Image(
                        painter = painterResource(id = profile.imageRes),
                        contentDescription = "프로필 ${profile.id}",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Text(
                text = "전체보기",
                modifier = Modifier.clickable(onClick = onSeeAllClick),
                fontWeight = nanumSquareRegular,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}

/**
 * 오늘의 질문 카드 (cvHomeQuestion)
 */
@Composable
fun TodayQuestionCard(question: Question, onAnswerClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)) // 예시 색상
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "오늘의 질문",
                        fontSize = 16.sp,
                        fontWeight = nanumSquareBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = question.content,
                        modifier = Modifier.width(200.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = nanumSquareRegular,
                        color = Color.Black
                    )
                }
                LoadAnimatedApngFromUrl(imageUrl = question.emojiUrl)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onAnswerClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = question.buttonText, fontSize = 16.sp, fontWeight = nanumSquareBold)
            }
        }
    }
}

/**
 * 요청 카드 (cvHomeRequest)
 */
@Composable
fun RequestCard(request: Request, onRespondClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)) // mainColor 예시
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if(request.isActive) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.Green, CircleShape)
                            .align(Alignment.TopStart)
                    )
                }
                Text(
                    text = "요청",
                    modifier = Modifier.align(Alignment.TopCenter),
                    fontSize = 16.sp,
                    fontWeight = nanumSquareBold,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = request.content,
                fontSize = 14.sp,
                fontWeight = nanumSquareRegular,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = onRespondClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = request.buttonText, fontSize = 16.sp, fontWeight = nanumSquareBold)
            }
        }
    }
}

/**
 * 추억 캐러셀 (recyclerViewCarousel)
 */
@Composable
fun MemoriesCarousel(memories: List<Memory>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(memories) { memory ->
            Card(
                modifier = Modifier.size(width = 180.dp, height = 240.dp),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray)
            ) {
                // 캐러셀 아이템 내부 UI (예시)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = memory.imageRes),
                        contentDescription = "추억 ${memory.id}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun LoadAnimatedApngFromUrl(imageUrl: String) {
    AndroidView(
        modifier = Modifier.size(100.dp),
        factory = { context ->
            ImageView(context).apply {
                // Glide를 사용해 URL로부터 APNG 이미지를 로드
                Glide.with(context)
                    .load(imageUrl)
                    .into(this)
            }
        }
    )
}

