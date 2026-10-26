package com.ahn.ggriggri.screen.ui.main.ui.response

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahn.ggriggri.screen.ui.main.ui.response.component.ImageUploadSection
import com.ahn.ggriggri.screen.ui.main.ui.response.component.MessageInputSection
import com.ahn.ggriggri.screen.ui.main.viewmodel.ResponseViewModel

@Composable
fun ResponseScreen(
    requestDocumentId: String,
    onNavigateBack: () -> Unit,
    viewModel: ResponseViewModel = hiltViewModel(),
) {

    val selectedImageUri by viewModel.selectedImageUri.collectAsStateWithLifecycle()
    val responseMessage by viewModel.responseMessage.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val isResponseButtonEnabled by viewModel.isResponseButtonEnabled.collectAsStateWithLifecycle()
    val currentMessageLength by viewModel.currentMessageLength.collectAsStateWithLifecycle()

    // requestDocumentId를 ViewModel에 설정
    LaunchedEffect(requestDocumentId) {
        Log.d("ResponseScreen", "requestDocumentId 받음: $requestDocumentId")
        viewModel.setRequestDocumentId(requestDocumentId)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // 실패: 화면에 머무르며 메시지를 보여준 뒤 clear 한다.
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    // 성공: 메시지를 보여준 뒤에만 화면을 벗어난다.
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
            onNavigateBack()
        }
    }

    // 이미지 선택을 위한 ActivityResultLauncher 정의
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> 
            uri?.let { viewModel.selectImage(it) }
        }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
