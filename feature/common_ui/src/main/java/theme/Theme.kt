package theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40, // 주요 버튼, 로고 등
    secondary = PurpleGrey40, // 주 색상 위에 표시될 콘텐츠(텍스트, 아이콘)의 색상
    tertiary = Pink40,
    primaryContainer = MainColor, // CommonButton 배경색
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

@Composable
fun GgriggriTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // dynamicColor 를 false 처리해서 시스템 색상이 우리 테마를 덮어쓰지 못하게 해야함
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
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