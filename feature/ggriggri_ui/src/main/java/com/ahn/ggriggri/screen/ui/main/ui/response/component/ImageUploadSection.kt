package com.ahn.ggriggri.screen.ui.main.ui.response.component

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ahn.common_ui.theme.GgriggriTheme
import com.ahn.common_ui.theme.OnImageOverlay
import com.ahn.common_ui.theme.UploadButtonColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
fun ImageUploadSection(
    selectedImageUri: Uri?,
    onImageSelected: () -> Unit,
    onImageRemoved: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, GgriggriTheme.colors.border)
    ) {
        if (selectedImageUri != null) {
            // 선택된 이미지 표시
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "선택된 이미지",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onImageRemoved,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "이미지 제거",
                        tint = OnImageOverlay // 사진 위에 올라가는 아이콘이라 모드와 무관하게 흰색 고정
                    )
                }
            }
        } else {
            // 이미지 업로드 안내
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = "이미지 아이콘",
                    modifier = Modifier.size(48.dp),
                    tint = GgriggriTheme.colors.textTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "업로드 된 이미지가 없습니다.",
                    fontSize = 14.sp,
                    color = GgriggriTheme.colors.textTertiary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "등록할 이미지를 업로드해주세요",
                    fontSize = 12.sp,
                    color = GgriggriTheme.colors.textTertiary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onImageSelected,
                    colors = ButtonDefaults.buttonColors(containerColor = UploadButtonColor)
                ) {
                    Icon(
                        Icons.Default.Upload,
                        contentDescription = "업로드",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("이미지 업로드")
                }
            }
        }
    }
}








