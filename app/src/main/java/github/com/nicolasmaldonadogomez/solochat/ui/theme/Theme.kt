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
        typography = Typography,
        content = content
    )
}
