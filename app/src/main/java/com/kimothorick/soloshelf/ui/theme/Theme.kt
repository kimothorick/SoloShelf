package com.kimothorick.soloshelf.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.kimothorick.soloshelf.data.preferences.ColorTheme
import com.kimothorick.soloshelf.data.preferences.ThemeMode
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme

@Immutable
data class ExtendedColors(
    val errorColor: Color,
    val successColor: Color,
)

@Immutable
class SoloShelfTheme(
    val colorScheme: ColorScheme,
    val typography: androidx.compose.material3.Typography = SoloShelfTypography,
) {
    companion object {
        val colorScheme: ColorScheme
            @Composable @ReadOnlyComposable get() = LocalSoloShelfThemeInstance.current.colorScheme

        val typography: androidx.compose.material3.Typography
            @Composable @ReadOnlyComposable get() = LocalSoloShelfThemeInstance.current.typography

        val styles: SoloShelfStyles = SoloShelfStyles

        val LocalSoloShelfTheme: ProvidableCompositionLocal<SoloShelfTheme>
            get() = LocalSoloShelfThemeInstance
    }
}

internal val LocalSoloShelfThemeInstance = staticCompositionLocalOf {
    SoloShelfTheme(
        colorScheme = ColorScheme(
            primary = Color.Unspecified,
            onPrimary = Color.Unspecified,
            primaryContainer = Color.Unspecified,
            onPrimaryContainer = Color.Unspecified,
            inversePrimary = Color.Unspecified,
            secondary = Color.Unspecified,
            onSecondary = Color.Unspecified,
            secondaryContainer = Color.Unspecified,
            onSecondaryContainer = Color.Unspecified,
            tertiary = Color.Unspecified,
            onTertiary = Color.Unspecified,
            tertiaryContainer = Color.Unspecified,
            onTertiaryContainer = Color.Unspecified,
            background = Color.Unspecified,
            onBackground = Color.Unspecified,
            surface = Color.Unspecified,
            onSurface = Color.Unspecified,
            surfaceVariant = Color.Unspecified,
            onSurfaceVariant = Color.Unspecified,
            surfaceTint = Color.Unspecified,
            inverseSurface = Color.Unspecified,
            inverseOnSurface = Color.Unspecified,
            error = Color.Unspecified,
            onError = Color.Unspecified,
            errorContainer = Color.Unspecified,
            onErrorContainer = Color.Unspecified,
            outline = Color.Unspecified,
            outlineVariant = Color.Unspecified,
            scrim = Color.Unspecified,
            surfaceBright = Color.Unspecified,
            surfaceDim = Color.Unspecified,
            surfaceContainer = Color.Unspecified,
            surfaceContainerHigh = Color.Unspecified,
            surfaceContainerHighest = Color.Unspecified,
            surfaceContainerLow = Color.Unspecified,
            surfaceContainerLowest = Color.Unspecified,
        ),
    )
}

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
    amoledBlack: Boolean = false,
    seedColor: Color? = null,
    themeMode: ThemeMode = ThemeMode.MATERIAL_YOU,
    colorTheme: ColorTheme = ColorTheme.MONO,
    customSeedColor: Color = Color.Blue,
    paletteStyle: PaletteStyle = PaletteStyle.Vibrant,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val currentExtendedColors = if (darkTheme) darkExtendedColors else lightExtendedColors

    val targetSeedColor = remember(seedColor, themeMode, colorTheme, customSeedColor, darkTheme) {
        seedColor ?: if (themeMode == ThemeMode.MATERIAL_YOU && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (darkTheme) dynamicDarkColorScheme(context).primary else dynamicLightColorScheme(context).primary
        } else {
            when (colorTheme) {
                ColorTheme.MONO -> if (darkTheme) Color.Black else Color.White
                ColorTheme.CYAN_TEAL -> CYAN_TEAL
                ColorTheme.TERRACOTTA -> TERRACOTTA
                ColorTheme.IRIS -> IRIS
                ColorTheme.OATMEAL -> OATMEAL
                ColorTheme.CRIMSON -> CRIMSON
                ColorTheme.MINT -> MINT
                ColorTheme.AMBER -> AMBER
                ColorTheme.PLUM -> PLUM
                ColorTheme.CUSTOM -> customSeedColor
            }
        }
    }

    val animatedSeedColor by animateColorAsState(
        targetValue = targetSeedColor,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "ThemeSeedColor",
    )

    var colorScheme = rememberDynamicColorScheme(
        seedColor = animatedSeedColor,
        isDark = darkTheme,
        isAmoled = darkTheme && amoledBlack,
        style = when {
            colorTheme == ColorTheme.MONO -> {
                PaletteStyle.Monochrome
            }

            themeMode == ThemeMode.MATERIAL_YOU -> {
                PaletteStyle.Vibrant
            }

            else -> {
                paletteStyle
            }
        },
    )

    if (darkTheme && amoledBlack) {
        colorScheme = colorScheme.copy(
            surface = Color.Black,
            background = Color.Black,
            surfaceContainer = Color.Black,
            surfaceContainerLow = Color.Black,
            surfaceContainerLowest = Color.Black,
            surfaceContainerHigh = Color.Black,
            surfaceContainerHighest = Color.Black,
        )
    }

    val theme = SoloShelfTheme(
        colorScheme = colorScheme,
        typography = SoloShelfTypography,
    )

    CompositionLocalProvider(
        LocalExtendedColors provides currentExtendedColors,
        SoloShelfTheme.LocalSoloShelfTheme provides theme,
    ) {
        MaterialTheme(
            colorScheme = SoloShelfTheme.colorScheme,
            typography = SoloShelfTheme.typography,
            content = content,
        )
    }
}

val extendedColors: ExtendedColors
    @Composable @ReadOnlyComposable get() = LocalExtendedColors.current
