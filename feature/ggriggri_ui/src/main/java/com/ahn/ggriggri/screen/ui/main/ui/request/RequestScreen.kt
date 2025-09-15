package com.ahn.ggriggri.screen.main.request

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.ahn.ggriggri.screen.ui.main.ui.request.component.ImageUploadSection
import com.ahn.ggriggri.screen.ui.main.ui.request.component.MessageInputSection
import com.ahn.ggriggri.screen.ui.main.viewmodel.RequestViewModel
import theme.GgriggriTheme
import theme.NanumSquareBold
import theme.NanumSquareLight
import theme.NanumSquareRegular


@Composable
fun RequestScreen(
    onNavigateBack: () -> Unit,
    viewModel: RequestViewModel = hiltViewModel(),
) {

    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val requestMessage by viewModel.requestMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val isRequestButtonEnabled by viewModel.isRequestButtonEnabled.collectAsState()
    val currentMessageLength by viewModel.currentMessageLength.collectAsState()

    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // 추후 스낵바
            viewModel.clearErrorMessage()
        }
    }

    successMessage?.let { message ->
        LaunchedEffect(message) {
            // 추후 스낵바
            onNavigateBack()
            viewModel.clearSuccessMessage()
        }
    }

    // 이미지 선택을 위한 ActivityResultLauncher 정의
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> 
            uri?.let { viewModel.selectImage(it) }
        }
    )

    GgriggriTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(70.dp))

                //이미지 업로드
//                ImageUploadBox(
//                    imageUri = selectedImageUri,
//                    onClick = {
//                        // 이미지 선택기 실행
//                        photoPickerLauncher.launch(
//                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
//                        )
//                    }
//                )
                ImageUploadSection(
                    selectedImageUri = selectedImageUri,
                    onImageSelected = { 
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onImageRemoved = viewModel::removeImage
                )

                Spacer(modifier = Modifier.height(20.dp))

                MessageInputSection(
                    message = requestMessage,
                    onMessageChanged = viewModel::updateMessage,
                    currentLength = currentMessageLength,
                    maxLength = 100
                )


                Spacer(modifier = Modifier.weight(1f))


                Button(
                    onClick = viewModel::createRequest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = isRequestButtonEnabled && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRequestButtonEnabled) Color.Gray else Color.LightGray
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "요청하기",
                            fontSize = 16.sp,
                            fontFamily = NanumSquareBold
                        )
                    }
                }
            }
        }
    }
}

    @Composable
    fun ImageUploadBox(
        imageUri: Uri?,
        onClick: () -> Unit) {
        // 점선 효과 정의
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 2.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri == null) {
                // 이미지가 없을 때: 플레이스홀더 UI
                ImageUploadPlaceholder(onUploadClick = onClick)
            } else {
                // 이미지가 있을 때: 선택된 이미지 표시
                AsyncImage(
                    model = imageUri,
                    contentDescription = "업로드된 이미지",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }

    @Composable
    fun ImageUploadPlaceholder(onUploadClick: () -> Unit) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = "이미지 아이콘",
                modifier = Modifier.size(50.dp),
                tint = Color(0xFFBDBDBD) // gray_400
            )
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = "업로드 된 이미지가 없습니다.",
                color = Color(0xFF9E9E9E), // gray_500
                fontSize = 14.sp,
                fontFamily = NanumSquareRegular
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "등록할 이미지를 업로드해주세요.",
                color = Color(0xFFBDBDBD), // gray_400
                fontSize = 12.sp,
                fontFamily = NanumSquareLight
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onUploadClick,
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null, // 장식용 아이콘
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text("이미지 업로드")
            }
        }
    }

