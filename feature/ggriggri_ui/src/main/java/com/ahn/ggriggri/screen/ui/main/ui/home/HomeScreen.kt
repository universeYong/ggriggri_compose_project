package com.ahn.ggriggri.screen.main.home

import android.util.Log
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.ahn.common_ui.R
import com.ahn.domain.model.QuestionList
import com.ahn.ggriggri.screen.ui.main.ui.home.component.bottomsheet.AllProfilesSheetContent
import com.ahn.ggriggri.screen.ui.main.viewmodel.home.HomeViewModel
import com.ahn.ggriggri.screen.ui.main.viewmodel.home.Profile
import kotlinx.coroutines.launch
import theme.GgriggriTheme
import theme.NanumSquareBold
import theme.NanumSquareRegular
import androidx.core.graphics.toColorInt
import com.github.penfeizhou.animation.apng.APNGDrawable
import com.github.penfeizhou.animation.loader.Loader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.net.URL


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewmodel: HomeViewModel,
    onNavigationToAnswer: () -> Unit
) {
    val profiles by homeViewmodel.profiles.collectAsState()

    val todayQuestion by homeViewmodel.todayQuestionContent.collectAsState() // 오늘의 질문 내용
    val isLoadingTodayQuestion by homeViewmodel.isLoadingTodayQuestion.collectAsState() // 질문 로딩 상태

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
                        if (isLoadingTodayQuestion) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 50.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            todayQuestion?.let { currentNonNullQuestion ->
                                TodayQuestionCard(
                                    question = currentNonNullQuestion,
                                    onAnswerClick = {
                                        Log.d("HomeScreen", "Answer button clicked for: ${currentNonNullQuestion.content}")
                                        onNavigationToAnswer()
                                    }
                                )
                            } ?: run {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                        .height(100.dp),
                                    shape = RoundedCornerShape(15.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("오늘의 질문을 불러오지 못했어요.", fontFamily = NanumSquareRegular)
                                    }
                                }
                            }
                        }
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
fun TodayQuestionCard(question: QuestionList, onAnswerClick: () -> Unit) {
    if (question == null) return

    val cardColor = runCatching {
        Color(question.color.toColorInt())
    }.getOrElse { Color(0xFFE0E0E0) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor) // 예시 색상
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "오늘의 질문",
                        fontSize = 16.sp,
                        fontFamily = NanumSquareBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = question.content,
                        modifier = Modifier.widthIn(max = 200.dp),
                        textAlign = TextAlign.Start,
                        fontSize = 14.sp,
                        fontFamily = NanumSquareRegular,
                        color = Color.Black,
                        lineHeight = 20.sp
                    )
                }
                if (!question.imgUrl.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    LoadAnimatedApngFromUrlComposable(
                        imageUrl = question.imgUrl,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAnswerClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.8f),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "답변하기", fontSize = 16.sp, fontFamily = NanumSquareBold)
            }
        }
    }
}
// Apng
@Composable
fun LoadAnimatedApngFromUrlComposable(imageUrl: String, modifier: Modifier = Modifier) {
    var apngDrawable by remember { mutableStateOf<APNGDrawable?>(null)}
    var isLoading by remember { mutableStateOf(true) }
    var errorOccurred by remember { mutableStateOf(false)}

    LaunchedEffect(imageUrl) {
        isLoading = true
        errorOccurred = false
        apngDrawable?.stop() // 이전 Drawable 정지
        apngDrawable = null // 초기화

        if (imageUrl.isBlank()) {
            isLoading = false
            errorOccurred = true
            return@LaunchedEffect
        }

        launch(Dispatchers.IO) { // IO 스레드에서 네트워크 작업
            runCatching {
                val url = URL(imageUrl)
                val connection = url.openConnection()
                // 타임아웃 설정
                // connection.connectTimeout = 5000 // 5초
                connection.connect()
                val inputStream = connection.getInputStream()
                val bytes = inputStream.readBytes()
                inputStream.close()
                // StreamReader가 InputStream을 받는다고 가정
                val customReader = com.github.penfeizhou.animation.io.StreamReader(ByteArrayInputStream(bytes))

                val loader = object : Loader {
                    override fun obtain(): com.github.penfeizhou.animation.io.Reader { // 반환 타입 일치
                        return customReader
                    }
                }
                // 또는, ByteBufferLoader가 아래와 같이 사용될 수 있다면:
                // (라이브러리 버전에 따라 다를 수 있으니 확인 필요)
                // val byteBuffer = ByteBuffer.wrap(bytes)
                // val loader = SomeSpecificLoaderImplementation(byteBuffer) // <- 이부분이 문제였음

                val drawable = APNGDrawable(loader)

                withContext(Dispatchers.Main) { // UI 스레드에서 상태 업데이트
                    apngDrawable = drawable
                    drawable.start() // 애니메이션 시작
                    isLoading = false
                }
            }.getOrElse {
                Log.e("LoadAPNG", "Error loading APNG: $imageUrl", it)
                withContext(Dispatchers.Main) {
                    errorOccurred = true
                    isLoading = false
                }
            }
        }
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(30.dp))
        }else if (errorOccurred || apngDrawable == null) {
            Image(
                painter = painterResource(id = R.drawable.baseline_error_24),
                contentDescription = "APNG ERROR",
                modifier = Modifier.fillMaxSize()
            )
        }else {
            AndroidView(
                factory = { ImageView(it) },
                modifier = Modifier.fillMaxSize(),
                update = { imageView ->
                    imageView.setImageDrawable(apngDrawable)
                }
            )
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



