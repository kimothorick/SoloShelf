package com.kimothorick.soloshelf.ui.screens.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatLineSpacing
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.VerticalDistribute
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalSlider
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kimothorick.soloshelf.ui.screens.reader.ReaderViewModel
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.CombinedPreviews
import com.kimothorick.soloshelf.ui.tooling.ComponentPreviews
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.preferences.FontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettings(
    viewModel: ReaderViewModel,
    onDismiss: () -> Unit,
) {
    val preferences by viewModel.readerPreferences.collectAsState()
    val brightness by viewModel.readerBrightness.collectAsState()

    ReaderSettingsContent(
        preferences = preferences,
        brightness = brightness,
        onDismiss = onDismiss,
        updateTheme = viewModel::updateReaderTheme,
        updateFontFamily = viewModel::updateReaderFontFamily,
        updateBrightness = viewModel::updateReaderBrightness,
        updateFontSize = viewModel::updateReaderFontSize,
        updatePageMargins = viewModel::updateReaderPageMargins,
        updateLineHeight = viewModel::updateReaderLineHeight,
    )
}

@Composable
fun ReaderSettingsContent(
    preferences: EpubPreferences,
    brightness: Int?,
    onDismiss: () -> Unit,
    updateTheme: (String) -> Unit,
    updateFontFamily: (String?) -> Unit,
    updateBrightness: (Int) -> Unit,
    updateFontSize: (Int) -> Unit,
    updatePageMargins: (Int) -> Unit,
    updateLineHeight: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp), thickness = 0.5.dp)

        Text(
            text = "Screen mode",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        ThemeSelector(
            currentTheme = when (preferences.theme) {
                org.readium.r2.navigator.preferences.Theme.SEPIA -> "sepia"
                org.readium.r2.navigator.preferences.Theme.DARK -> "dark"
                else -> "light"
            },
            onThemeSelected = updateTheme,
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp), thickness = 0.5.dp)

        Text(
            text = "Font",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        FontSelector(
            currentFont = preferences.fontFamily?.name,
            onFontSelected = updateFontFamily,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            VerticalSettingSlider(
                label = "Brightness",
                icon = Icons.Rounded.LightMode,
                value = (brightness ?: 50).toFloat(),
                onValueChange = { updateBrightness(it.toInt()) },
            )
            VerticalSettingSlider(
                label = "Font Size",
                textIcon = "Aa",
                value = (preferences.fontSize?.times(100) ?: 100.0).toFloat(),
                range = 50f..300f,
                onValueChange = { updateFontSize(it.toInt()) },
            )
            VerticalSettingSlider(
                label = "Margin",
                icon = Icons.Rounded.VerticalDistribute,
                value = (preferences.pageMargins?.times(100) ?: 100.0).toFloat(),
                range = 50f..200f,
                onValueChange = { updatePageMargins(it.toInt()) },
            )
            VerticalSettingSlider(
                label = "Line spacing",
                icon = Icons.Rounded.FormatLineSpacing,
                value = (preferences.lineHeight?.times(100) ?: 100.0).toFloat(),
                range = 50f..200f,
                onValueChange = { updateLineHeight(it.toInt()) },
            )
        }
    }
}

@Composable
fun ThemeSelector(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        ThemeOption(Color.White, "light", currentTheme == "light", onThemeSelected)
        ThemeOption(Color(0xFFF5E6D3), "sepia", currentTheme == "sepia", onThemeSelected)
        ThemeOption(Color(0xFFD3D3D3), "grey", currentTheme == "grey", onThemeSelected)
        ThemeOption(Color.Black, "dark", currentTheme == "dark", onThemeSelected)
    }
}

@Composable
fun ThemeOption(
    color: Color,
    theme: String,
    isSelected: Boolean,
    onThemeSelected: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.3f,
                    )
                },
                shape = CircleShape,
            ).clickable { onThemeSelected(theme) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSelector(
    currentFont: String?,
    onFontSelected: (String?) -> Unit,
) {
    val fonts = listOf("Literata", "Merriweather", "Google Sans", "PT Serif", "Roboto")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(fonts) { font ->
            InputChip(
                selected = currentFont == font,
                onClick = { onFontSelected(font) },
                label = { Text(font) },
                shape = RoundedCornerShape(24.dp),
                colors = InputChipDefaults.inputChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
                border = InputChipDefaults.inputChipBorder(
                    enabled = true,
                    selected = currentFont == font,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    selectedBorderColor = Color.Transparent,
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VerticalSettingSlider(
    label: String,
    icon: ImageVector? = null,
    textIcon: String? = null,
    value: Float,
    range: ClosedFloatingPointRange<Float> = 0f..100f,
    onValueChange: (Float) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))

        val sliderState = rememberSliderState(
            value = value,
            trackRange = range,
        )

        LaunchedEffect(value) {
            if (sliderState.value != value) {
                sliderState.value = value
            }
        }

        VerticalSlider(
            state = sliderState,
            modifier =
                Modifier
                    .height(140.dp)
                    .align(Alignment.CenterHorizontally),
            track = {
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.width(56.dp),
                    trackCornerSize = 28.dp,
                )
            },
            thumb = {
            },
            onValueChange =onValueChange
        )

        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            } else if (textIcon != null) {
                Text(
                    text = textIcon,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun ReaderSettingsPreview() {
    SoloShelfTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(32.dp),
        ) {
            ReaderSettingsContent(
                preferences = EpubPreferences(
                    fontFamily = FontFamily("Google Sans"),
                    fontSize = 1.2,
                ),
                brightness = 70,
                onDismiss = {},
                updateTheme = {},
                updateFontFamily = {},
                updateBrightness = {},
                updateFontSize = {},
                updatePageMargins = {},
                updateLineHeight = {},
            )
        }
    }
}
