package com.ahn.ggriggri.screen.ui.auth.ui.devlogin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahn.ggriggri.screen.ui.auth.viewmodel.OAuthViewModel
import theme.GgriggriTheme

@Composable
fun DevLoginScreen(
    onNavigationToGroup: () -> Unit = {},
    onNavigationToHome: () -> Unit = {},
    authViewModel: OAuthViewModel = hiltViewModel()
) {
    var userId by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var userProfileImage by remember { mutableStateOf("") }
    var userGroupDocumentId by remember { mutableStateOf("") }
    var userAutoLoginToken by remember { mutableStateOf("") }

    val currentUserId by authViewModel.currentUserId.collectAsStateWithLifecycle()

    // 로그인 성공 후 네비게이션 처리
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            authViewModel.checkUserGroupAndNavigate(
                onNavigationToGroup = onNavigationToGroup,
                onNavigationToHome = onNavigationToHome
            )
        }
    }

    GgriggriTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(100.dp))
                
                Text(
                    text = "개발용 로그인",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(Modifier.height(32.dp))

                // 사용자 ID 입력
                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it },
                    label = { Text("사용자 ID") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Spacer(Modifier.height(16.dp))

                // 사용자 이름 입력
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("사용자 이름") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Spacer(Modifier.height(16.dp))

                // 프로필 이미지 URL 입력
                OutlinedTextField(
                    value = userProfileImage,
                    onValueChange = { userProfileImage = it },
                    label = { Text("프로필 이미지 URL (선택사항)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )

                Spacer(Modifier.height(16.dp))

                // 그룹 문서 ID 입력
                OutlinedTextField(
                    value = userGroupDocumentId,
                    onValueChange = { userGroupDocumentId = it },
                    label = { Text("그룹 문서 ID (선택사항)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Spacer(Modifier.height(16.dp))

                // 자동 로그인 토큰 입력
                OutlinedTextField(
                    value = userAutoLoginToken,
                    onValueChange = { userAutoLoginToken = it },
                    label = { Text("자동 로그인 토큰") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Spacer(Modifier.height(32.dp))

                // 로그인 버튼
                Button(
                    onClick = {
                        if (userId.isNotBlank() && userName.isNotBlank() && userAutoLoginToken.isNotBlank()) {
                            authViewModel.handleDevLogin(
                                userId = userId,
                                userName = userName,
                                userProfileImage = userProfileImage.ifBlank { "" },
                                userGroupDocumentId = userGroupDocumentId.ifBlank { "" },
                                userAutoLoginToken = userAutoLoginToken
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = userId.isNotBlank() && userName.isNotBlank() && userAutoLoginToken.isNotBlank()
                ) {
                    Text("개발용 로그인")
                }

                Spacer(Modifier.height(16.dp))

                // 빠른 테스트용 버튼들
                Text(
                    text = "빠른 테스트용 계정",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            userId = "user1"
                            userName = "사용자1"
                            userProfileImage = ""
                            userGroupDocumentId = "4qET84qBUHZCRgroPcWB" // 그룹 ID 미리 설정
                            userAutoLoginToken = "dev_token_1"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("사용자1")
                    }
                    
                    Button(
                        onClick = {
                            userId = "user2"
                            userName = "사용자2"
                            userProfileImage = ""
                            userGroupDocumentId = "4qET84qBUHZCRgroPcWB" // 그룹 ID 미리 설정
                            userAutoLoginToken = "dev_token_2"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("사용자2")
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            userId = "user3"
                            userName = "사용자3"
                            userProfileImage = ""
                            userGroupDocumentId = "4qET84qBUHZCRgroPcWB" // 그룹 ID 미리 설정
                            userAutoLoginToken = "dev_token_3"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("사용자3")
                    }
                    
                    Button(
                        onClick = {
                            userId = "user4"
                            userName = "사용자4"
                            userProfileImage = ""
                            userGroupDocumentId = "4qET84qBUHZCRgroPcWB" // 그룹 ID 미리 설정
                            userAutoLoginToken = "dev_token_4"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("사용자4")
                    }
                }
            }
        }
    }
}
