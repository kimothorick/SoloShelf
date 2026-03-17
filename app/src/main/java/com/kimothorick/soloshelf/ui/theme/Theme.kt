package com.kimothorick.soloshelf.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

@Immutable
data class ExtendedColors(
    val errorColor: Color,
    val successColor: Color,
)

private val lightExtendedColors = ExtendedColors(
    errorColor = errorColor,
    successColor = successColor,
)

private val darkExtendedColors = ExtendedColors(
    errorColor = errorColor,
    successColor = successColor,
)

internal val LocalExtendedColors = staticCompositionLocalOf {
    lightExtendedColors
}

@Composable
fun SoloShelfTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val currentExtendedColors = if (darkTheme) darkExtendedColors else lightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides currentExtendedColors) {
        val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            val seedColor = if (darkTheme) Color.Black else Color.White
            rememberDynamicColorScheme(
                seedColor = seedColor,
                isDark = darkTheme,
                style = PaletteStyle.Monochrome,
            )
        }

        MaterialTheme(
            colorScheme = colorScheme,
            typography = SoloShelfTypography,
            content = content,
        )
    }
}

val extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
