package github.com.nicolasmaldonadogomez.solochat.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta Base (Material Default)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Dark Theme specific
val DarkBackground = Color(0xFF1C1C1C)
val DarkBubbleBlue = Color(0xFF004D40) // Using a dark teal/blue
val DarkGreyBackground = Color(0xFF121212)
val DarkBlueBubbles = Color(0xFF1565C0)

// WhatsApp Theme
val WhatsAppGreen = Color(0xFF25D366)
val WhatsAppGreenDark = Color(0xFF075E54)
val WhatsAppLightGreen = Color(0xFFDCF8C6)
val WhatsAppBackground = Color(0xFFE5DDD5)

// Telegram Theme
val TelegramBlue = Color(0xFF0088CC)
val TelegramLightBlue = Color(0xFF64B5F6)
val TelegramBubbleBlue = Color(0xFFB3E5FC)

// Matrix Theme
val MatrixGreen = Color(0xFF00FF41)
val MatrixDark = Color(0xFF000000)
val MatrixDarkGreen = Color(0xFF003B00)

enum class AppTheme {
    SYSTEM, LIGHT, DARK, WHATSAPP, TELEGRAM, MATRIX
}
