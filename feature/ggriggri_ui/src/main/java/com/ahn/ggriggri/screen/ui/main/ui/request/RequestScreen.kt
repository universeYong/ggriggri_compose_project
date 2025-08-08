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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

// XML의 폰트 스타일을 대체하기 위한 변수 (실제 프로젝트의 FontFamily로 교체 필요)
val nanumSquareLight = FontWeight.Light
val nanumSquareRegular = FontWeight.Normal
val nanumSquareBold = FontWeight.Bold

@Composable
fun RequestScreen(
    onNavigateBack: () -> Unit,
    onSubmit: (Uri?, String) -> Unit
) {
    // 1. 상태 정의: 선택된 이미지 URI와 입력된 텍스트
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var requestText by remember { mutableStateOf("") }
    val maxChars = 100

    // 2. 이미지 선택을 위한 ActivityResultLauncher 정의
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    // 버튼 활성화 조건
    val isButtonEnabled = selectedImageUri != null && requestText.isNotBlank()

    Scaffold(
        topBar = {
            RequestTopBar(
                title = "요청하기",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(70.dp))

            // 3. 이미지 업로드 영역
            ImageUploadBox(
                imageUri = selectedImageUri,
                onClick = {
                    // 이미지 선택기 실행
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. 요청 입력 필드
            OutlinedTextField(
                value = requestText,
                onValueChange = {
                    if (it.length <= maxChars) {
                        requestText = it
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("요청을 입력하세요.", fontWeight = nanumSquareRegular) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    // cursorColor = mainColor
                )
            )

            // 5. 글자 수 카운터
            Text(
                text = "${requestText.length}/$maxChars",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                textAlign = TextAlign.End,
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = nanumSquareRegular
            )

            // 버튼을 하단에 고정
            Spacer(modifier = Modifier.weight(1f))

            // 6. 최종 제출 버튼
            Button(
                onClick = { onSubmit(selectedImageUri, requestText) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                enabled = isButtonEnabled,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF0F0F0),
                    contentColor = Color.Black,
                    disabledContainerColor = Color.LightGray,
                    disabledContentColor = Color.Gray
                )
            ) {
                Text(
                    text = "요청하기",
                    fontSize = 16.sp,
                    fontWeight = nanumSquareBold
                )
            }
        }
    }
}

@Composable
fun ImageUploadBox(imageUri: Uri?, onClick: () -> Unit) {
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
            .clickable(onClick = onClick), // Box 전체를 클릭 가능하게 만듦
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
            fontWeight = nanumSquareRegular
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = "등록할 이미지를 업로드해주세요.",
            color = Color(0xFFBDBDBD), // gray_400
            fontSize = 12.sp,
            fontWeight = nanumSquareLight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestTopBar(title: String, onNavigateBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로 가기"
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Preview(showBackground = true)
@Composable
fun RequestScreenPreview() {
    MaterialTheme {
        RequestScreen(onNavigateBack = {}, onSubmit = { _, _ -> })
    }
}