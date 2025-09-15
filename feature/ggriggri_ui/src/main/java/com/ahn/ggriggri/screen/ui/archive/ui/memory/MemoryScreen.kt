package com.ahn.ggriggri.screen.archive.memory

import theme.BtnContentColor
import theme.MainColor
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.ahn.ggriggri.screen.archive.questionlist.QuestionListScreen
import com.ahn.ggriggri.screen.archive.requestlist.RequestListScreen
import com.ahn.ggriggri.screen.ui.archive.viewmodel.questionlist.QuestionListViewModel
import kotlinx.coroutines.launch
import theme.GgriggriTheme
import theme.NanumSquareExtraBold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    onNavigateToQuestionAnswerActual: (questionDataId: String) -> Unit,
    onNavigateToRequestDetailActual: (requestDocumentId: String) -> Unit
) {
    val tabItems = listOf("요청","질문")
    val pagerState = rememberPagerState (pageCount = { tabItems.size })
    val coroutineScope = rememberCoroutineScope()

    GgriggriTheme {
        Column {
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
                when(page) {
                    0 -> RequestListScreen(
                        onNavigateToRequestDetail = onNavigateToRequestDetailActual
                    )
                    1 -> QuestionListScreen(
                        onNavigateToQuestionAnswer = onNavigateToQuestionAnswerActual
                    )
                }
            }
        }
    }
}