package com.ahn.ggriggri.screen.archive.requestlist

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahn.ggriggri.screen.ui.archive.viewmodel.requestlist.RequestListViewModel
import com.ahn.ggriggri.screen.ui.archive.viewmodel.requestlist.ArchivedRequestItem

@Composable
fun RequestListScreen(
    onNavigateToRequestDetail: (requestId: String) -> Unit = {}
) {
    val requestListViewModel: RequestListViewModel = hiltViewModel()

    val archivedRequests by requestListViewModel.archivedRequests.collectAsStateWithLifecycle()
    val isLoading by requestListViewModel.isLoading.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(archivedRequests) { index, item: ArchivedRequestItem ->
                    ListItem(
                        headlineContent = { Text(item.content) }, // 요청 내용
                        supportingContent = { Text("${item.userName}님") }, // 사용자명
                        trailingContent = { Text(item.date) }, // 날짜
                        modifier = Modifier.clickable {
                            Log.d("RequestListScreen", "Navigating with ID: ${item.requestId}")
                            if (item.requestId.isNotBlank()) {
                                onNavigateToRequestDetail(item.requestId) // 클릭된 아이템의 ID 전달
                            } else {
                                Log.e("RequestListScreen", "requestId is blank for item: ${item.content}")
                                // 사용자에게 알림 또는 다른 처리 (예: Toast 메시지)
                            }
                        },
                    )
                    if (index < archivedRequests.size - 1) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                    }
                }
            }
        }
    }
}