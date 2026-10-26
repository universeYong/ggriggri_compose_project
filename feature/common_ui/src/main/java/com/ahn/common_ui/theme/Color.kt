package com.ahn.common_ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// MainColor (브랜드 노랑). 화면에서는 직접 쓰지 말고 colorScheme.primaryContainer로 쓴다.
val MainColor = Color(0xFFFDF497)

// ButtonContentColor. 화면에서는 colorScheme.onPrimaryContainer로 쓴다.
val BtnContentColor = Color(0xff000000)

/*
 * ---- 브랜드 노랑(MainColor) 카드 위에 고정으로 쓰는 색 ----
 * MainColor 카드는 라이트/다크 모두 같은 노랑이므로 그 위의 색도 모드와 무관하게 고정한다.
 */
// 노랑 카드 위 보조 텍스트/아이콘 (작성 시각, 기본 프로필 아이콘)
val OnMainColorSecondary = Color(0xFF666666)
// 노랑 카드 위 기본 프로필 원 배경/테두리
val OnMainColorAvatarPlaceholder = Color(0xFFE0E0E0)
// 요청 카드의 "진행 중" 표시 점
val RequestActiveDot = Color(0xFF00FF00)
// 요청 카드 하단 버튼(DynamicButton) 상태별 배경
val RequestButtonDefault = Color(0xFFFFFFFF)
val RequestButtonQuestioner = Color(0xFF0000FF)
val RequestButtonResponded = Color(0xFF00FF00)
val RequestButtonInactive = Color(0xFF888888)
// RequestButtonDefault(흰 배경) 위 텍스트 / 그 외 진한 배경 위 텍스트
val OnRequestButtonDefault = Color(0xFF000000)
val OnRequestButtonFilled = Color(0xFFFFFFFF)

/*
 * ---- 데이터/이미지 위에 고정으로 쓰는 색 ----
 */
// 오늘의 질문 카드: 배경색이 질문 데이터(question.color, 밝은 파스텔)에서 오므로 그 위 텍스트·버튼은 모드와 무관하게 고정한다.
val QuestionCardFallback = Color(0xFFE0E0E0)
val OnQuestionCard = Color(0xFF000000)
val QuestionCardButton = Color(0xFFFFFFFF)
// 사진 위에 올리는 아이콘(이미지 제거 X 버튼)
val OnImageOverlay = Color(0xFFFFFFFF)
// 이미지 업로드 버튼 (연노랑 브랜드 보조색, 모드와 무관)
val UploadButtonColor = Color(0xFFFFF59D)

/*
 * ---- GgriggriColors(라이트/다크 대응 커스텀 토큰)의 원본 값 ----
 * Material colorScheme에 정확히 대응하는 토큰이 없는 회색 계열.
 * 라이트 값은 기존 하드코딩 값과 동일하다.
 */
internal val Gray666 = Color(0xFF666666)
internal val Gray888 = Color(0xFF888888) // = Color.Gray
internal val Gray999 = Color(0xFF999999)
internal val Gray444 = Color(0xFF444444) // = Color.DarkGray
internal val GrayCCC = Color(0xFFCCCCCC) // = Color.LightGray
internal val GrayE0 = Color(0xFFE0E0E0)
internal val GrayF0 = Color(0xFFF0F0F0)
internal val GrayF5 = Color(0xFFF5F5F5)
internal val GrayF8 = Color(0xFFF8F9FA)
