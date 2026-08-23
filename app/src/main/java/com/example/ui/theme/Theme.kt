package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Purple80,
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color(0xFF80CBC4),
    secondary = PurpleGrey80,
    onSecondary = Color(0xFF4E2600),
    secondaryContainer = Color(0xFF6E3900),
    onSecondaryContainer = Color(0xFFFFCC80),
    tertiary = Pink80,
    onTertiary = Color(0xFF4A0072),
    tertiaryContainer = Color(0xFF6A0080),
    onTertiaryContainer = Color(0xFFE1BEE7),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2D2D2D),
    onBackground = Color(0xFFEDE7F6),
    onSurface = Color(0xFFEDE7F6),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF8A92A6)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ProvalinoMint,
    onPrimary = Color.White,
    primaryContainer = ProvalinoMintContainer,
    onPrimaryContainer = Color(0xFF004D40),
    secondary = ProvalinoOrange,
    onSecondary = Color.White,
    secondaryContainer = ProvalinoOrangeContainer,
    onSecondaryContainer = Color(0xFF7E3D00),
    tertiary = ProvalinoPurple,
    onTertiary = Color.White,
    tertiaryContainer = ProvalinoPurpleContainer,
    onTertiaryContainer = Color(0xFF4A0072),
    background = ProvalinoSoftBackground,
    surface = ProvalinoSurfaceWhite,
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = Color(0xFF1E293B),
    onSurface = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  // Disable dynamic color by default to preserve Provalino brand identity
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
