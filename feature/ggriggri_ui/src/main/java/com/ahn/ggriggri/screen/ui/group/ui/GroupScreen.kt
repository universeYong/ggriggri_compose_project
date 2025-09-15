package com.ahn.ggriggri.screen.group

import theme.BtnContentColor
import theme.MainColor
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahn.ggriggri.screen.group.joingroup.JoinGroupScreen
import com.ahn.ggriggri.screen.group.makegroup.MakeGroupScreen
import com.ahn.ggriggri.screen.ui.auth.viewmodel.OAuthViewModel
import kotlinx.coroutines.launch
import theme.GgriggriTheme
import theme.NanumSquareExtraBold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    authViewModel: OAuthViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit = {}
) {

    val tabItems = listOf("그룹 만들기", "그룹 들어가기")
    val pagerState = rememberPagerState(pageCount = { tabItems.size })
    val coroutineScope = rememberCoroutineScope()

    val userId by authViewModel.currentUserId.collectAsStateWithLifecycle()
    Log.d("GroupScreen", "userId: $userId")


    GgriggriTheme {
        Column(

        ) {
            Spacer(Modifier.height(24.dp))
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                tabItems.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                fontFamily = NanumSquareExtraBold
                            )
                        },
                        selectedContentColor = MainColor,
                        unselectedContentColor = BtnContentColor
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        if (!userId.isNullOrBlank()) {
                            MakeGroupScreen(
                                userId = userId!!,
                                onNavigateToHome = onNavigateToHome
                            )
                        }
                    }
                    1 -> {
                        if (!userId.isNullOrBlank()) {
                            JoinGroupScreen(
                                userId = userId!!,
                                onNavigateToHome = onNavigateToHome
                            )
                        }
                    }
                }
            }
        }
    }

}

