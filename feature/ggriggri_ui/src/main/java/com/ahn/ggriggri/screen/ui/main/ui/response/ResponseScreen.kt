package com.ahn.ggriggri.screen.main.response

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahn.ggriggri.screen.ui.main.ui.response.component.ImageUploadSection
import com.ahn.ggriggri.screen.ui.main.ui.response.component.MessageInputSection
import com.ahn.ggriggri.screen.ui.main.viewmodel.ResponseViewModel
import theme.GgriggriTheme

@Composable
fun ResponseScreen(
    requestDocumentId: String,
    onNavigateBack: () -> Unit,
    viewModel: ResponseViewModel = hiltViewModel(),
) {

    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val responseMessage by viewModel.responseMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val isResponseButtonEnabled by viewModel.isResponseButtonEnabled.collectAsState()
    val currentMessageLength by viewModel.currentMessageLength.collectAsState()

    // requestDocumentId를 ViewModel에 설정
    LaunchedEffect(requestDocumentId) {
        Log.d("ResponseScreen", "requestDocumentId 받음: $requestDocumentId")
        viewModel.setRequestDocumentId(requestDocumentId)
    }

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

                // 이미지 업로드
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
                    message = responseMessage,
                    onMessageChanged = viewModel::updateMessage,
                    currentLength = currentMessageLength,
                    maxLength = 100
                )

                Spacer(modifier = Modifier.height(30.dp))

                // 응답하기 버튼
                Button(
                    onClick = { viewModel.createResponse() },
                    enabled = isResponseButtonEnabled && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("응답하기")
                    }
                }
            }
        }
    }
}
