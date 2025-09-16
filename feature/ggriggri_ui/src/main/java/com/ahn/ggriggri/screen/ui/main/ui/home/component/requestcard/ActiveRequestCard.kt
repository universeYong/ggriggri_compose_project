package com.ahn.ggriggri.screen.ui.main.ui.home.component.requestcard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahn.domain.model.Request
import com.ahn.ggriggri.screen.ui.main.viewmodel.HomeViewModel
import theme.MainColor
import theme.NanumSquareBold
import theme.NanumSquareRegular

@Composable
fun ActiveRequestCard(
    request: Request,
    viewModel: HomeViewModel,
    onNavigateToResponse: (String) -> Unit = {},
    onNavigateToRequestDetail: (Request) -> Unit = {}
) {
    val userName by remember {
        derivedStateOf {
            viewModel.getUserName(request.requestUserDocumentID)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = MainColor)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.Green, CircleShape)
                        .align(Alignment.TopStart)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${userName}님의 요청",
                fontSize = 16.sp,
                fontFamily = NanumSquareBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = request.requestMessage,
                fontSize = 14.sp,
                fontFamily = NanumSquareRegular,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(30.dp))

            DynamicButton(
                request = request,
                viewModel = viewModel,
                onNavigateToResponse = onNavigateToResponse,
                onNavigateToRequestDetail = onNavigateToRequestDetail
            )
        }
    }
}