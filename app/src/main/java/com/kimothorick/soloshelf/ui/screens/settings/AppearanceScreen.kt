package com.kimothorick.soloshelf.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.preferences.AppTheme
import com.kimothorick.soloshelf.data.preferences.ColorTheme
import com.kimothorick.soloshelf.data.preferences.ThemeMode
import com.kimothorick.soloshelf.ui.components.SoloShelfSegmentedListItem
import com.kimothorick.soloshelf.ui.components.SoloShelfSettingsItem
import com.kimothorick.soloshelf.ui.components.SoloShelfTopAppBar
import com.kimothorick.soloshelf.ui.components.Switch
import com.kimothorick.soloshelf.ui.components.ToggleableButtonGroup
import com.kimothorick.soloshelf.ui.components.ToggleableButtonItem
import com.kimothorick.soloshelf.ui.theme.AMBER
import com.kimothorick.soloshelf.ui.theme.CRIMSON
import com.kimothorick.soloshelf.ui.theme.CYAN_TEAL
import com.kimothorick.soloshelf.ui.theme.IRIS
import com.kimothorick.soloshelf.ui.theme.MINT
import com.kimothorick.soloshelf.ui.theme.OATMEAL
import com.kimothorick.soloshelf.ui.theme.PLUM
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.theme.TERRACOTTA
import com.kimothorick.soloshelf.ui.theme.WALNUT
import com.kimothorick.soloshelf.ui.tooling.ComponentPreviews
import com.materialkolor.PaletteStyle

@Composable
fun AppearanceScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themePreference by viewModel.themePreference.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val colorTheme by viewModel.colorTheme.collectAsStateWithLifecycle()
    val amoledBlack by viewModel.amoledBlack.collectAsStateWithLifecycle()
    val customSeedColor by viewModel.customSeedColor.collectAsStateWithLifecycle()
    val paletteStyle by viewModel.paletteStyle.collectAsStateWithLifecycle()

    AppearanceScreenContent(
        themePreference = themePreference,
        themeMode = themeMode,
        colorTheme = colorTheme,
        amoledBlack = amoledBlack,
        customSeedColor = customSeedColor,
        paletteStyle = paletteStyle,
        onBack = onBack,
        onThemePreferenceChange = viewModel::setTheme,
        onThemeModeChange = viewModel::setThemeMode,
        onColorThemeChange = viewModel::setColorTheme,
        onAmoledBlackChange = viewModel::setAmoledBlack,
        onCustomSeedColorChange = viewModel::setCustomSeedColor,
        onPaletteStyleChange = viewModel::setPaletteStyle,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppearanceScreenContent(
    themePreference: AppTheme,
    themeMode: ThemeMode,
    colorTheme: ColorTheme,
    amoledBlack: Boolean,
    customSeedColor: Color,
    paletteStyle: PaletteStyle,
    onBack: () -> Unit,
    onThemePreferenceChange: (AppTheme) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onColorThemeChange: (ColorTheme) -> Unit,
    onAmoledBlackChange: (Boolean) -> Unit,
    onCustomSeedColorChange: (Color) -> Unit,
    onPaletteStyleChange: (PaletteStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SoloShelfTopAppBar(
                title = stringResource(R.string.appearance),
                onBackClick = onBack,
                scrollBehavior = scrollBehavior,
            )
        },
        containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SettingsSectionHeader(title = stringResource(R.string.dark_theme))

            val themePreferenceItems = listOf(
                ToggleableButtonItem(
                    label = stringResource(R.string.theme_auto),
                    isSelected = themePreference == AppTheme.AUTO,
                    identifier = AppTheme.AUTO,
                ),
                ToggleableButtonItem(
                    label = stringResource(R.string.theme_light),
                    isSelected = themePreference == AppTheme.LIGHT,
                    identifier = AppTheme.LIGHT,
                ),
                ToggleableButtonItem(
                    label = stringResource(R.string.theme_dark),
                    isSelected = themePreference == AppTheme.DARK,
                    identifier = AppTheme.DARK,
                ),
            )

            ToggleableButtonGroup(
                items = themePreferenceItems,
                onItemSelected = { onThemePreferenceChange(it.identifier as AppTheme) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = SoloShelfTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.extraLarge,
                    ),
            )

            SoloShelfSettingsItem(
                headline = stringResource(R.string.amoled_black),
                supportingText = stringResource(R.string.amoled_black_desc),
                trailingContent = {
                    Switch(
                        checked = amoledBlack,
                        onCheckedChange = onAmoledBlackChange,
                    )
                },
            )

            SettingsSectionHeader(title = stringResource(R.string.theme))

            val themeModeItems = listOf(
                ToggleableButtonItem(
                    label = stringResource(R.string.theme_mode_material_you),
                    isSelected = themeMode == ThemeMode.MATERIAL_YOU,
                    identifier = ThemeMode.MATERIAL_YOU,
                ),
                ToggleableButtonItem(
                    label = stringResource(R.string.theme_mode_custom),
                    isSelected = themeMode == ThemeMode.CUSTOM,
                    identifier = ThemeMode.CUSTOM,
                ),
            )

            ToggleableButtonGroup(
                items = themeModeItems,
                onItemSelected = { onThemeModeChange(it.identifier as ThemeMode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = SoloShelfTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.extraLarge,
                    ),
            )

            if (themeMode == ThemeMode.CUSTOM) {
                SettingsSectionHeader(title = stringResource(R.string.color_theme))

                val isDark = when (themePreference) {
                    AppTheme.DARK -> true
                    AppTheme.LIGHT -> false
                    AppTheme.AUTO -> isSystemInDarkTheme()
                }

                val colorPresets = listOf(
                    ColorPreset(
                        stringResource(R.string.color_mono),
                        if (isDark) Color.Black else Color.White,
                        ColorTheme.MONO,
                    ),
                    ColorPreset(stringResource(R.string.color_cyan_teal), CYAN_TEAL, ColorTheme.CYAN_TEAL),
                    ColorPreset(stringResource(R.string.color_terracotta), TERRACOTTA, ColorTheme.TERRACOTTA),
                    ColorPreset(stringResource(R.string.color_iris), IRIS, ColorTheme.IRIS),
                    ColorPreset(stringResource(R.string.color_oatmeal), OATMEAL, ColorTheme.OATMEAL),
                    ColorPreset(stringResource(R.string.color_crimson), CRIMSON, ColorTheme.CRIMSON),
                    ColorPreset(stringResource(R.string.color_mint), MINT, ColorTheme.MINT),
                    ColorPreset(stringResource(R.string.color_amber), AMBER, ColorTheme.AMBER),
                    ColorPreset(stringResource(R.string.color_plum), PLUM, ColorTheme.PLUM),
                    ColorPreset(stringResource(R.string.color_walnut), WALNUT, ColorTheme.WALNUT),
                    ColorPreset(
                        stringResource(R.string.color_custom),
                        customSeedColor,
                        ColorTheme.CUSTOM,
                        isCustom = true,
                    ),
                )

                val regularPresets = colorPresets.filter { !it.isCustom }
                val customPreset = colorPresets.find { it.isCustom }

                Surface(
                    color = SoloShelfTheme.colorScheme.surfaceContainerHighest,
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            maxItemsInEachRow = 5,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            regularPresets.forEach { preset ->
                                val isSelected = colorTheme == preset.identifier
                                ColorThemeItem(
                                    preset = preset,
                                    isSelected = isSelected,
                                    onClick = {
                                        onColorThemeChange(preset.identifier)
                                        if (preset.identifier == ColorTheme.MONO) {
                                            onPaletteStyleChange(PaletteStyle.Monochrome)
                                        } else if (paletteStyle == PaletteStyle.Monochrome) {
                                            onPaletteStyleChange(PaletteStyle.Vibrant)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        if (customPreset != null) {
                            val isSelected = colorTheme == customPreset.identifier
                            ColorThemeItem(
                                preset = customPreset,
                                isSelected = isSelected,
                                isCustom = true,
                                onClick = {
                                    onColorThemeChange(customPreset.identifier)
                                    if (paletteStyle == PaletteStyle.Monochrome) {
                                        onPaletteStyleChange(PaletteStyle.Vibrant)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                if (colorTheme == ColorTheme.CUSTOM) {
                    CustomColorPicker(
                        customSeedColor = customSeedColor,
                        onCustomSeedColorChange = onCustomSeedColorChange,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            if (themeMode == ThemeMode.CUSTOM && colorTheme != ColorTheme.MONO) {
                PaletteStyleSection(
                    paletteStyle = paletteStyle,
                    colorTheme = colorTheme,
                    onPaletteStyleChange = onPaletteStyleChange,
                )
            }
            Spacer(
                modifier = Modifier.height(
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp,
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PaletteStyleSection(
    paletteStyle: PaletteStyle,
    colorTheme: ColorTheme,
    onPaletteStyleChange: (PaletteStyle) -> Unit,
) {
    Column {
        SettingsSectionHeader(title = stringResource(R.string.palette_style))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    shape = MaterialTheme.shapes.extraLarge,
                ),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        ) {
            val styles = listOfNotNull(
                Triple(
                    stringResource(R.string.palette_tonal_spot),
                    stringResource(R.string.palette_tonal_spot_desc),
                    PaletteStyle.TonalSpot,
                ),
                Triple(
                    stringResource(R.string.palette_neutral),
                    stringResource(R.string.palette_neutral_desc),
                    PaletteStyle.Neutral,
                ),
                Triple(
                    stringResource(R.string.palette_vibrant),
                    stringResource(R.string.palette_vibrant_desc),
                    PaletteStyle.Vibrant,
                ),
                Triple(
                    stringResource(R.string.palette_expressive),
                    stringResource(R.string.palette_expressive_desc),
                    PaletteStyle.Expressive,
                ),
                if (colorTheme == ColorTheme.CUSTOM) {
                    Triple(
                        stringResource(R.string.palette_monochrome),
                        stringResource(R.string.palette_monochrome_desc),
                        PaletteStyle.Monochrome,
                    )
                } else {
                    null
                },
            )

            styles.forEachIndexed { index, (title, desc, style) ->
                SoloShelfSegmentedListItem(
                    headlineText = title,
                    supportingText = desc,
                    shapes = ListItemDefaults.segmentedShapes(index, styles.size),
                    leadingContent = {
                        RadioButton(
                            selected = paletteStyle == style,
                            onClick = null,
                        )
                    },
                    onClick = { onPaletteStyleChange(style) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CustomColorPicker(
    customSeedColor: Color,
    onCustomSeedColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SettingsSectionHeader(title = stringResource(R.string.seed_color))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = MaterialTheme.shapes.extraLarge)
                .background(
                    color = SoloShelfTheme.colorScheme.surfaceContainerHighest,
                ).padding(16.dp),
        ) {
            SaturationValueArea(
                currentColor = customSeedColor,
                onColorChange = onCustomSeedColorChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(MaterialTheme.shapes.large),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Hue",
                    style = SoloShelfTheme.typography.labelLarge,
                    modifier = Modifier.width(48.dp),
                )
                val hsv = FloatArray(3)
                android.graphics.Color.colorToHSV(customSeedColor.toArgb(), hsv)
                Slider(
                    value = hsv[0],
                    onValueChange = { hue ->
                        hsv[0] = hue
                        onCustomSeedColorChange(Color(android.graphics.Color.HSVToColor(hsv)))
                    },
                    valueRange = 0f..360f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = SoloShelfTheme.colorScheme.onSurfaceVariant,
                        activeTrackColor = SoloShelfTheme.colorScheme.onSurfaceVariant,
                        inactiveTrackColor = SoloShelfTheme.colorScheme.surfaceContainerHigh,
                    ),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(
                    text = "Hex",
                    style = SoloShelfTheme.typography.labelLarge,
                    modifier = Modifier.width(48.dp),
                )
                HexEntry(
                    color = customSeedColor,
                    onColorChange = onCustomSeedColorChange,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun HexEntry(
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hexValue = rememberSaveable(color) {
        Integer.toHexString(color.toArgb()).uppercase().takeLast(6)
    }
    var textValue by rememberSaveable { mutableStateOf(hexValue) }

    LaunchedEffect(hexValue) {
        textValue = hexValue
    }

    Surface(
        color = SoloShelfTheme.colorScheme.surfaceContainer,
        shape = CircleShape,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color, CircleShape)
                    .border(1.dp, SoloShelfTheme.colorScheme.outlineVariant, CircleShape),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "#",
                style = SoloShelfTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = SoloShelfTheme.colorScheme.onSurfaceVariant,
            )
            BasicTextField(
                value = textValue,
                onValueChange = { newValue: String ->
                    val filtered = newValue.uppercase().filter { it in "0123456789ABCDEF" }.take(6)
                    textValue = filtered
                    if (filtered.length == 6) {
                        try {
                            val parsedColor = Color("#$filtered".toColorInt())
                            onColorChange(parsedColor)
                        } catch (_: Exception) {
                            // Invalid hex, ignore
                        }
                    }
                },
                textStyle = TextStyle(
                    color = SoloShelfTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = SoloShelfTheme.typography.bodyMedium.fontSize,
                ),
                cursorBrush = SolidColor(SoloShelfTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    autoCorrectEnabled = false,
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SaturationValueArea(
    currentColor: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hsv = remember(currentColor) {
        val array = FloatArray(3)
        android.graphics.Color.colorToHSV(currentColor.toArgb(), array)
        array
    }

    BoxWithConstraints(
        modifier = modifier
            .pointerInput(hsv[0]) {
                fun updateColor(
                    offset: Offset,
                ) {
                    val saturation = (offset.x / size.width).coerceIn(0f, 1f)
                    val value = 1f - (offset.y / size.height).coerceIn(0f, 1f)
                    onColorChange(Color(android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], saturation, value))))
                }

                detectTapGestures { offset ->
                    updateColor(offset)
                }
            }.pointerInput(hsv[0]) {
                detectDragGestures { change, _ ->
                    val saturation = (change.position.x / size.width).coerceIn(0f, 1f)
                    val value = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                    onColorChange(Color(android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], saturation, value))))
                }
            }.drawBehind {
                val saturationGradient = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White,
                        Color(android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], 1f, 1f))),
                    ),
                )
                val valueGradient = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                )

                drawRect(brush = saturationGradient)
                drawRect(brush = valueGradient)
            },
    ) {
        val density = LocalDensity.current
        val indicatorSize = 24.dp
        val indicatorSizePx = with(density) { indicatorSize.toPx() }

        val x = hsv[1] * constraints.maxWidth
        val y = (1f - hsv[2]) * constraints.maxHeight

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (x - indicatorSizePx / 2).toInt(),
                        y = (y - indicatorSizePx / 2).toInt(),
                    )
                }.size(indicatorSize)
                .drawBehind {
                    drawCircle(
                        color = Color.White,
                        radius = size.minDimension / 2,
                        style = Stroke(width = 2.dp.toPx()),
                    )
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.2f),
                        radius = size.minDimension / 2 + 1.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx()),
                    )
                },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColorThemeItem(
    modifier: Modifier = Modifier,
    preset: ColorPreset,
    isSelected: Boolean,
    isCustom: Boolean = false,
    onClick: () -> Unit,
) {
    if (isCustom) {
        Surface(
            onClick = onClick,
            color = if (isSelected) {
                SoloShelfTheme.colorScheme.surfaceContainer
            } else {
                SoloShelfTheme.colorScheme.surfaceContainer
            },
            shape = MaterialTheme.shapes.extraLarge,
            modifier = modifier,
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(if (isSelected) MaterialShapes.Clover8Leaf.toShape() else CircleShape)
                        .background(preset.color),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (preset.color.luminance() > 0.5f) Color.Black else Color.White,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = preset.label,
                    style = SoloShelfTheme.typography.labelLarge,
                    maxLines = 1,
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(if (isSelected) MaterialShapes.Clover8Leaf.toShape() else CircleShape)
                .clickable(onClick = onClick)
                .background(preset.color)
                .border(1.dp, SoloShelfTheme.colorScheme.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (preset.color.luminance() > 0.5f) Color.Black else Color.White,
                )
            }
        }
    }
}

private data class ColorPreset(
    val label: String,
    val color: Color,
    val identifier: ColorTheme,
    val isCustom: Boolean = false,
)

private fun Color.luminance(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue

@ComponentPreviews
@Composable
private fun AppearanceScreenPreview() {
    SoloShelfTheme(
        colorTheme = ColorTheme.CYAN_TEAL,
        paletteStyle = PaletteStyle.Vibrant,
        amoledBlack = false,
    ) {
        AppearanceScreenContent(
            themePreference = AppTheme.AUTO,
            themeMode = ThemeMode.CUSTOM,
            colorTheme = ColorTheme.CYAN_TEAL,
            amoledBlack = false,
            customSeedColor = Color(0xFF3B82F6),
            paletteStyle = PaletteStyle.Vibrant,
            onBack = {},
            onThemePreferenceChange = {},
            onThemeModeChange = {},
            onColorThemeChange = {},
            onAmoledBlackChange = {},
            onCustomSeedColorChange = {},
            onPaletteStyleChange = {},
        )
    }
}

@ComponentPreviews
@Composable
private fun CustomColorPickerPreview() {
    SoloShelfTheme {
        Surface {
            CustomColorPicker(
                customSeedColor = Color(0xFFFFFFFF),
                onCustomSeedColorChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
    }
}
