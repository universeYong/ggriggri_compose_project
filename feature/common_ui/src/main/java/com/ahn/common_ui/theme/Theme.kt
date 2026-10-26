package com.ahn.common_ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    // 브랜드 노랑 버튼/카드는 다크 모드에서도 그대로 유지한다(기본값인 보라 컨테이너로 바뀌지 않게).
    primaryContainer = MainColor, // CommonButton 배경색, 노랑 카드
    onPrimaryContainer = BtnContentColor // CommonButton 텍스트 색
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40, // 주요 버튼, 로고 등
    secondary = PurpleGrey40, // 주 색상 위에 표시될 콘텐츠(텍스트, 아이콘)의 색상
    tertiary = Pink40,
    primaryContainer = MainColor, // CommonButton 배경색, 노랑 카드
    onPrimaryContainer = BtnContentColor // CommonButton 텍스트 색

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/**
 * colorScheme에 대응 토큰이 없는 회색 계열 커스텀 색.
 * 라이트 값은 기존 하드코딩 색과 같고, 다크 값은 어두운 배경에서 읽히도록 정했다.
 * 사용: `GgriggriTheme.colors.textSecondary`
 */
@Immutable
data class GgriggriColors(
    /** 보조 텍스트/아이콘 (라이트 #666666) */
    val textSecondary: Color,
    /** 3차 텍스트/아이콘, 안내 문구 (라이트 #888888 = Color.Gray) */
    val textTertiary: Color,
    /** 힌트/부가 설명 텍스트 (라이트 #999999) */
    val textHint: Color,
    /** 입력창 포커스 테두리·라벨 (라이트 #444444 = Color.DarkGray) */
    val borderFocused: Color,
    /** 기본 테두리·구분선 (라이트 #888888 = Color.Gray) */
    val border: Color,
    /** 옅은 테두리 (라이트 #CCCCCC = Color.LightGray) */
    val borderLight: Color,
    /** 이미지 없음 자리·빈 카드 배경 (라이트 #E0E0E0) */
    val placeholder: Color,
    /** 섹션 사이 두꺼운 구분 띠 (라이트 #F0F0F0) */
    val sectionDivider: Color,
    /** 화면 전체 옅은 회색 배경 (라이트 #F5F5F5) */
    val surfaceSubtle: Color,
    /** 옅은 회색 카드 (라이트 #F8F9FA) */
    val surfaceRaised: Color,
    /** 비활성 버튼 배경/내용 (라이트 #CCCCCC / 흰색) */
    val disabledContainer: Color,
    val onDisabledContainer: Color,
    /** CommonButton 비활성 배경/내용 (라이트 #888888 / #CCCCCC) */
    val buttonDisabledContainer: Color,
    val buttonOnDisabledContainer: Color,
)

private val LightGgriggriColors = GgriggriColors(
    textSecondary = Gray666,
    textTertiary = Gray888,
    textHint = Gray999,
    borderFocused = Gray444,
    border = Gray888,
    borderLight = GrayCCC,
    placeholder = GrayE0,
    sectionDivider = GrayF0,
    surfaceSubtle = GrayF5,
    surfaceRaised = GrayF8,
    disabledContainer = GrayCCC,
    onDisabledContainer = Color(0xFFFFFFFF),
    buttonDisabledContainer = Gray888,
    buttonOnDisabledContainer = GrayCCC,
)

private val DarkGgriggriColors = GgriggriColors(
    textSecondary = Color(0xFFB3B3B3),
    textTertiary = Color(0xFF9E9E9E),
    textHint = Color(0xFF8C8C8C),
    borderFocused = Color(0xFFC4C4C4),
    border = Color(0xFF7A7A7A),
    borderLight = Color(0xFF4A4A4A),
    placeholder = Color(0xFF3A3A3A),
    sectionDivider = Color(0xFF2A2A2A),
    surfaceSubtle = Color(0xFF1C1B1F),
    surfaceRaised = Color(0xFF2B2930),
    disabledContainer = Color(0xFF3A3A3A),
    onDisabledContainer = Color(0xFF8C8C8C),
    buttonDisabledContainer = Color(0xFF3A3A3A),
    buttonOnDisabledContainer = Color(0xFF7A7A7A),
)

// 테마 밖(Preview 등)에서도 라이트 값으로 동작하도록 기본값을 둔다.
private val LocalGgriggriColors = staticCompositionLocalOf { LightGgriggriColors }

/**
 * 앱 전체 테마. MainActivity에서 한 번만 감싼다(각 Screen은 다시 감싸지 않고, Preview에서만 감싼다).
 * 브랜드 색을 유지하기 위해 Android 12+ dynamic color는 사용하지 않는다.
 */
@Composable
fun GgriggriTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val ggriggriColors = if (darkTheme) DarkGgriggriColors else LightGgriggriColors

    CompositionLocalProvider(LocalGgriggriColors provides ggriggriColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/** `GgriggriTheme.colors`로 커스텀 색 토큰에 접근한다. */
object GgriggriTheme {
    val colors: GgriggriColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGgriggriColors.current
}

/**
 * primary	앱의 주요 색상(대표 색상)
 * onPrimary	primary 위에 배치되는 텍스트/아이콘 색상
 * primaryContainer	primary를 담는 컨테이너(버튼, 카드 등) 배경색
 * onPrimaryContainer	primaryContainer 위의 텍스트/아이콘 색상
 * inversePrimary	반전된(primary와 대비되는) 주요 색상
 * secondary	보조 색상(주로 강조, 구분용)
 * onSecondary	secondary 위의 텍스트/아이콘 색상
 * secondaryContainer	secondary를 담는 컨테이너 배경색
 * onSecondaryContainer	secondaryContainer 위의 텍스트/아이콘 색상
 * tertiary	3차 색상(추가 강조, 구분용)
 * onTertiary	tertiary 위의 텍스트/아이콘 색상
 * tertiaryContainer	tertiary를 담는 컨테이너 배경색
 * onTertiaryContainer	tertiaryContainer 위의 텍스트/아이콘 색상
 * background	전체 화면의 기본 배경색
 * onBackground	background 위의 텍스트/아이콘 색상
 * surface	카드, 시트 등 표면의 기본 색상
 * onSurface	surface 위의 텍스트/아이콘 색상
 * surfaceVariant	변형된 표면 색상(구분, 강조용)
 * onSurfaceVariant	surfaceVariant 위의 텍스트/아이콘 색상
 * surfaceTint	표면 효과(음영, 강조 등)에 사용되는 색상(보통 primary와 동일)
 * inverseSurface	반전된 표면 색상(다크/라이트 테마 전환 시 사용)
 * inverseOnSurface	inverseSurface 위의 텍스트/아이콘 색상
 * error	오류 상태(알림, 경고 등)에 사용하는 색상
 * onError	error 위의 텍스트/아이콘 색상
 * errorContainer	error를 담는 컨테이너 배경색
 * onErrorContainer	errorContainer 위의 텍스트/아이콘 색상
 * outline	테두리, 구분선 등에 사용하는 색상
 * outlineVariant	outline의 변형 색상(더 미묘한 구분선 등)
 * scrim	반투명 오버레이(예: 다이얼로그, 드로어 뒷배경)에 사용하는 색상
 * surfaceBright	밝은 표면 색상(특정 밝기 단계)
 * surfaceContainer	표면 컨테이너의 기본 색상
 * surfaceContainerHigh	표면 컨테이너의 높은 밝기 색상
 * surfaceContainerHighest	표면 컨테이너의 가장 밝은 색상
 * surfaceContainerLow	표면 컨테이너의 낮은 밝기 색상
 * surfaceContainerLowest	표면 컨테이너의 가장 어두운 색상
 * surfaceDim	어두운 표면 색상(특정 어둡기 단계)
 */
