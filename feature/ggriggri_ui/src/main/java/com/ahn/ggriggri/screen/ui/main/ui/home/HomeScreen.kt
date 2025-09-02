package com.ahn.ggriggri.screen.main.home

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.ahn.common_ui.R
import com.ahn.ggriggri.screen.ui.main.ui.home.component.bottomsheet.AllProfilesSheetContent
import com.ahn.ggriggri.screen.ui.main.viewmodel.HomeViewModel
import com.ahn.ggriggri.screen.ui.main.viewmodel.Profile
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import theme.GgriggriTheme
import theme.NanumSquareBold
import theme.NanumSquareRegular


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewmodel: HomeViewModel,
) {
    val profiles by homeViewmodel.profiles.collectAsState()

    // BottomSheet
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    GgriggriTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // ScrollView + ConstraintLayout -> LazyColumn으로 대체
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 프로필 목록 카드
                    item {
                        ProfileSummaryCard(
                            profiles = profiles,
                            onSeeAllClick = { showBottomSheet = true }
                        )
                    }
                // 오늘의 질문 카드
                item {
                    TodayQuestionCard(
                        question = question,
                        onAnswerClick = {}
                    )
                }
//                // 요청 카드
//                item {
//                    RequestCard(
//                        request = request,
//                        onRespondClick = {}
//                    )
//                }
//                // 추억 캐러셀
//                item {
//                    MemoriesCarousel(memories = memories)
//                }
                }
                // ModalBottomSheet
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            showBottomSheet = false
                        },
                        sheetState = sheetState
                    ) {
                        AllProfilesSheetContent(
                            profiles = profiles,
                            onCloseSheet = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 프로필 목록을 보여주는 카드 (cvHomeProfileList)
 */
@OptIn(ExperimentalLayoutApi::class)
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
                        fontFamily = NanumSquareBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = question.content,
                        modifier = Modifier.width(200.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontFamily = NanumSquareRegular,
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
                Text(text = question.buttonText, fontSize = 16.sp, fontFamily = NanumSquareBold)
            }
        }
    }
}
//
///**
// * 요청 카드 (cvHomeRequest)
// */
//@Composable
//fun RequestCard(request: Request, onRespondClick: () -> Unit) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 20.dp),
//        shape = RoundedCornerShape(15.dp),
//        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)) // mainColor 예시
//    ) {
//        Column(
//            modifier = Modifier.padding(15.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Box(modifier = Modifier.fillMaxWidth()) {
//                if(request.isActive) {
//                    Box(
//                        modifier = Modifier
//                            .size(10.dp)
//                            .background(Color.Green, CircleShape)
//                            .align(Alignment.TopStart)
//                    )
//                }
//                Text(
//                    text = "요청",
//                    modifier = Modifier.align(Alignment.TopCenter),
//                    fontSize = 16.sp,
//                    fontFamily = NanumSquareBold,
//                    color = Color.Black
//                )
//            }
//            Spacer(modifier = Modifier.height(28.dp))
//            Text(
//                text = request.content,
//                fontSize = 14.sp,
//                fontFamily = NanumSquareRegular,
//                color = Color.Black
//            )
//            Spacer(modifier = Modifier.height(30.dp))
//            Button(
//                onClick = onRespondClick,
//                modifier = Modifier.fillMaxWidth(),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color.White,
//                    contentColor = Color.Black
//                ),
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Text(text = request.buttonText, fontSize = 16.sp, fontFamily = NanumSquareBold)
//            }
//        }
//    }
//}
//
///**
// * 추억 캐러셀 (recyclerViewCarousel)
// */
//@Composable
//fun MemoriesCarousel(memories: List<Memory>) {
//    LazyRow(
//        contentPadding = PaddingValues(horizontal = 20.dp),
//        horizontalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        items(memories) { memory ->
//            Card(
//                modifier = Modifier.size(width = 180.dp, height = 240.dp),
//                shape = RoundedCornerShape(15.dp),
//                colors = CardDefaults.cardColors(containerColor = Color.LightGray)
//            ) {
//                // 캐러셀 아이템 내부 UI (예시)
//                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    Image(
//                        painter = painterResource(id = memory.imageRes),
//                        contentDescription = "추억 ${memory.id}",
//                        modifier = Modifier.fillMaxSize(),
//                        contentScale = ContentScale.Crop
//                    )
//                }
//            }
//        }
//    }
//}

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

