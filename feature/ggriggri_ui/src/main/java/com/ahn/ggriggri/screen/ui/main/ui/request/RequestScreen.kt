package com.ahn.ggriggri.screen.ui.main.ui.request

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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahn.ggriggri.screen.ui.main.ui.request.component.ImageUploadSection
import com.ahn.ggriggri.screen.ui.main.ui.request.component.MessageInputSection
import com.ahn.ggriggri.screen.ui.main.viewmodel.RequestViewModel
import com.ahn.common_ui.theme.GgriggriTheme
import com.ahn.common_ui.theme.NanumSquareBold


@Composable
fun RequestScreen(
    onNavigateBack: () -> Unit,
    viewModel: RequestViewModel = hiltViewModel(),
) {

    val selectedImageUri by viewModel.selectedImageUri.collectAsStateWithLifecycle()
    val requestMessage by viewModel.requestMessage.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val isRequestButtonEnabled by viewModel.isRequestButtonEnabled.collectAsStateWithLifecycle()
    val currentMessageLength by viewModel.currentMessageLength.collectAsStateWithLifecycle()

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
                    containerColor = if (isRequestButtonEnabled) MaterialTheme.colorScheme.primaryContainer else GgriggriTheme.colors.disabledContainer,
                    contentColor = if (isRequestButtonEnabled) MaterialTheme.colorScheme.onPrimaryContainer else GgriggriTheme.colors.onDisabledContainer
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = GgriggriTheme.colors.onDisabledContainer
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

