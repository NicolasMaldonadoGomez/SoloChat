package github.com.nicolasmaldonadogomez.solochat.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme( )

private val LightColorScheme = lightColorScheme( )

private val WhatsAppColorScheme = lightColorScheme(
    primary            = WhatsAppGreenDark,
    secondary          = WhatsAppGreen,
    onPrimary          = Color.White,
    surface            = WhatsAppBackground,
    background         = WhatsAppBackground,
    primaryContainer   = WhatsAppGreen,
    onPrimaryContainer = Color.Black
)

private val TelegramColorScheme = lightColorScheme(
    primary            = TelegramBlue,
    secondary          = TelegramLightBlue,
    onPrimary          = Color.White,
    background         = TelegramBlue,
    surface            = TelegramBlue,
    primaryContainer   = TelegramBubbleBlue,
    onPrimaryContainer = Color.Black
)

private val MatrixColorScheme = darkColorScheme(
    primary            = MatrixGreen,
    background         = Color.Black,
    surface            = Color.Black,
    primaryContainer   = MatrixDarkGreen,
    onPrimary          = Color.Black,
    onBackground       = MatrixGreen,
    onSurface          = MatrixGreen,
    onPrimaryContainer = MatrixGreen
)

@Composable
fun SoloChatTheme(
    theme: AppTheme = AppTheme.SYSTEM,
    fontSizeScale: Float = 1.0f,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    
    val colorScheme = when (theme) {
        AppTheme.SYSTEM -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) DarkColorScheme else LightColorScheme
            }
        }
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.WHATSAPP -> WhatsAppColorScheme
        AppTheme.TELEGRAM -> TelegramColorScheme
        AppTheme.MATRIX -> MatrixColorScheme
    }

    val baseTypography = Typography
    val scaledTypography = Typography(
        displayLarge = baseTypography.displayLarge.copy(fontSize = baseTypography.displayLarge.fontSize * fontSizeScale),
        displayMedium = baseTypography.displayMedium.copy(fontSize = baseTypography.displayMedium.fontSize * fontSizeScale),
        displaySmall = baseTypography.displaySmall.copy(fontSize = baseTypography.displaySmall.fontSize * fontSizeScale),
        headlineLarge = baseTypography.headlineLarge.copy(fontSize = baseTypography.headlineLarge.fontSize * fontSizeScale),
        headlineMedium = baseTypography.headlineMedium.copy(fontSize = baseTypography.headlineMedium.fontSize * fontSizeScale),
        headlineSmall = baseTypography.headlineSmall.copy(fontSize = baseTypography.headlineSmall.fontSize * fontSizeScale),
        bodyLarge = baseTypography.bodyLarge.copy(fontSize = baseTypography.bodyLarge.fontSize * fontSizeScale),
        bodyMedium = baseTypography.bodyMedium.copy(fontSize = baseTypography.bodyMedium.fontSize * fontSizeScale),
        bodySmall = baseTypography.bodySmall.copy(fontSize = baseTypography.bodySmall.fontSize * fontSizeScale),
        titleLarge = baseTypography.titleLarge.copy(fontSize = baseTypography.titleLarge.fontSize * fontSizeScale),
        titleMedium = baseTypography.titleMedium.copy(fontSize = baseTypography.titleMedium.fontSize * fontSizeScale),
        titleSmall = baseTypography.titleSmall.copy(fontSize = baseTypography.titleSmall.fontSize * fontSizeScale),
        labelLarge = baseTypography.labelLarge.copy(fontSize = baseTypography.labelLarge.fontSize * fontSizeScale),
        labelMedium = baseTypography.labelMedium.copy(fontSize = baseTypography.labelMedium.fontSize * fontSizeScale),
        labelSmall = baseTypography.labelSmall.copy(fontSize = baseTypography.labelSmall.fontSize * fontSizeScale)
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme && theme != AppTheme.MATRIX && theme != AppTheme.DARK
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}
