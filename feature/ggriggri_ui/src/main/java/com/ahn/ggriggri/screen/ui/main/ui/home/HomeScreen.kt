package com.ahn.ggriggri.screen.main.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ahn.ggriggri.screen.ui.main.ui.home.component.bottomsheet.AllProfilesSheetContent
import com.ahn.ggriggri.screen.ui.main.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import theme.GgriggriTheme
import theme.NanumSquareRegular
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahn.domain.model.Request
import com.ahn.ggriggri.screen.ui.main.ui.home.component.cardview.ProfileSummaryCard
import com.ahn.ggriggri.screen.ui.main.ui.home.component.cardview.TodayQuestionCard
import com.ahn.ggriggri.screen.ui.main.ui.home.component.requestcard.ActiveRequestCard
import com.ahn.ggriggri.screen.ui.main.ui.home.component.requestcard.DefaultRequestCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigationToAnswer: () -> Unit,
    onNavigateToRequest: () -> Unit = {},
    onNavigateToResponse: (Request) -> Unit = {},
    onNavigateToRequestDetail: (Request) -> Unit = {},
    homeViewModel: HomeViewModel = hiltViewModel()
) {

    val profiles by homeViewModel.profiles.collectAsState()
    val todayQuestion by homeViewModel.todayQuestionContent.collectAsState()
    val isLoadingTodayQuestion by homeViewModel.isLoading.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val requests by homeViewModel.requests.collectAsState()
    val hasActiveRequest = requests.any { it.isAnswerable() }
    val activeRequest = requests.firstOrNull { it.isAnswerable() }
    
    // 디버깅을 위한 로그
    LaunchedEffect(requests) {
        android.util.Log.d("HomeScreen", "요청 목록 업데이트: ${requests.size}개")
        requests.forEach { request ->
            android.util.Log.d("HomeScreen", "요청: ${request.requestMessage}, 답변 가능: ${request.isAnswerable()}, 마감 시간: ${request.answerDeadline}, 현재 시간: ${System.currentTimeMillis()}")
        }
        android.util.Log.d("HomeScreen", "활성 요청 있음: $hasActiveRequest, 활성 요청: $activeRequest")
    }

    LaunchedEffect(true) {
        homeViewModel.loadRequests()
    }
    
//    // 화면이 다시 포커스될 때 요청 목록 새로고침
//    LaunchedEffect(true) {
//        homeViewModel.refreshRequests()
//    }

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
                    item(key = "profile_summary_card_key") {
                        ProfileSummaryCard(
                            profiles = profiles,
                            onSeeAllClick = { showBottomSheet = true }
                        )
                    }
                    // 오늘의 질문 카드
                    item(
                        key = todayQuestion?.number?.toString() ?: "today_question_empty_key"
                    ) {
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
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(
                                            0xFFE0E0E0
                                        )
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "오늘의 질문을 불러오지 못했어요.",
                                            fontFamily = NanumSquareRegular
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item(key = "request_card_key") {
                        // 디버깅을 위한 로그
                        LaunchedEffect(hasActiveRequest, activeRequest) {
                            android.util.Log.d("HomeScreen", "UI 업데이트 - hasActiveRequest: $hasActiveRequest, activeRequest: $activeRequest")
                        }
                        
                        if (hasActiveRequest && activeRequest != null) {
                        // 활성화된 질문
                        ActiveRequestCard(
                            request = activeRequest,
                            viewModel = homeViewModel,
                            onNavigateToResponse = onNavigateToResponse,
                            onNavigateToRequestDetail = onNavigateToRequestDetail
                        )
                    } else {
                        // 비활성화된 질문
                        DefaultRequestCard(
                            viewModel = homeViewModel,
                            onNavigateToRequest = onNavigateToRequest
                        )
                    }
                    }
                }
//                // 추억 캐러셀
//                item {
//                    MemoriesCarousel(memories = memories)
//                }
//                }
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



