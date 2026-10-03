package com.kimothorick.soloshelf.ui.screens.reader.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kimothorick.soloshelf.ui.screens.reader.ReaderViewModel
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.CombinedPreviews
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.preferences.FontFamily

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ReaderActionsPanel(
    viewModel: ReaderViewModel,
    modifier: Modifier = Modifier,
) {
    val showSettings by viewModel.showSettings.collectAsState()
    val showBars by viewModel.showBars.collectAsState()
    val preferences by viewModel.readerPreferences.collectAsState()
    val brightness by viewModel.readerBrightness.collectAsState()

    ReaderActionsPanelContent(
        showBars = showBars,
        showSettings = showSettings,
        preferences = preferences,
        brightness = brightness,
        onToggleSettings = { viewModel.toggleSettings() },
        onTocClick = { viewModel.toggleToc() },
        onBookmarkClick = { /* TODO */ },
        updateTheme = viewModel::updateReaderTheme,
        updateFontFamily = viewModel::updateReaderFontFamily,
        updateBrightness = viewModel::updateReaderBrightness,
        updateFontSize = viewModel::updateReaderFontSize,
        updatePageMargins = viewModel::updateReaderPageMargins,
        updateLineHeight = viewModel::updateReaderLineHeight,
        modifier = modifier,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ReaderActionsPanelContent(
    showBars: Boolean,
    showSettings: Boolean,
    preferences: EpubPreferences,
    brightness: Int?,
    onToggleSettings: () -> Unit,
    onTocClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    updateTheme: (String) -> Unit,
    updateFontFamily: (String?) -> Unit,
    updateBrightness: (Int) -> Unit,
    updateFontSize: (Int) -> Unit,
    updatePageMargins: (Int) -> Unit,
    updateLineHeight: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = showBars,
        enter = fadeIn() + slideInVertically { height -> height },
        exit = fadeOut() + slideOutVertically { height -> height },
        modifier = modifier.fillMaxSize(),
    ) {
        LookaheadScope {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                // THE SINGLE LAYER CONTAINER (Based on PolishedPillToPanelDemo)
                Box(
                    modifier = Modifier
                        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                        .navigationBarsPadding()
                        .widthIn(max = 640.dp)
                        // 1. Listen to inner layout shifts and animate the structural frame smoothly
                        .animateBounds(
                            lookaheadScope = this@LookaheadScope,
                            boundsTransform = { _, _ ->
                                spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                )
                            },
                        )
                        // 2. Stable backgrounds & shapes applied to the single wrapping element
                        .clip(RoundedCornerShape(36.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .graphicsLayer { clip = true }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}, // Consume click to prevent propagation
                        ),
                ) {
                    // 3. Swap the views internally inside a single AnimatedContent window
                    AnimatedContent(
                        targetState = showSettings,
                        label = "ContentSwitch",
                        contentAlignment = Alignment.BottomCenter,
                        transitionSpec = {
                            if (targetState) {
                                (slideInVertically { it } + fadeIn())
                                    .togetherWith(slideOutVertically { -it } + fadeOut())
                            } else {
                                (slideInVertically { -it } + fadeIn())
                                    .togetherWith(slideOutVertically { it } + fadeOut())
                            }.using(SizeTransform(clip = false))
                        },
                    ) { isSettingsVisible ->
                        if (isSettingsVisible) {
                            // --- EXPANDED PANEL UI CONTENT ---
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                            ) {
                                ReaderSettingsContent(
                                    preferences = preferences,
                                    brightness = brightness,
                                    onDismiss = onToggleSettings,
                                    updateTheme = updateTheme,
                                    updateFontFamily = updateFontFamily,
                                    updateBrightness = updateBrightness,
                                    updateFontSize = updateFontSize,
                                    updatePageMargins = updatePageMargins,
                                    updateLineHeight = updateLineHeight,
                                )
                            }
                        } else {
                            // --- COMPACT PILL UI CONTENT ---
                            ReaderViewBottomBar(
                                onTocClick = onTocClick,
                                onBookmarkClick = onBookmarkClick,
                                onSettingsClick = onToggleSettings,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderViewBottomBar(
    onTocClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.List,
            contentDescription = "Table of Contents",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier
                .size(32.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onTocClick,
                ),
        )

        Icon(
            imageVector = Icons.Rounded.BookmarkBorder,
            contentDescription = "Add Bookmark",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier
                .size(32.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBookmarkClick,
                ),
        )

        Icon(
            imageVector = Icons.Rounded.Settings,
            contentDescription = "Settings",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier
                .size(32.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSettingsClick,
                ),
        )
    }
}

@CombinedPreviews
@Composable
private fun ReaderActionsPanelPillPreview() {
    SoloShelfTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.BottomCenter,
        ) {
            ReaderActionsPanelContent(
                showBars = true,
                showSettings = false,
                preferences = EpubPreferences(),
                brightness = 50,
                onToggleSettings = {},
                onTocClick = {},
                onBookmarkClick = {},
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

@CombinedPreviews
@Composable
private fun ReaderActionsPanelSettingsPreview() {
    SoloShelfTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.BottomCenter,
        ) {
            ReaderActionsPanelContent(
                showBars = true,
                showSettings = true,
                preferences = EpubPreferences(
                    fontFamily = FontFamily("Google Sans"),
                    fontSize = 1.2,
                ),
                brightness = 75,
                onToggleSettings = {},
                onTocClick = {},
                onBookmarkClick = {},
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Preview
fun PolishedPillToPanelDemo() {
    var showReaderCustomisations by remember { mutableStateOf(false) }
    val backgroundShape = RoundedCornerShape(36.dp)

    LookaheadScope {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    enabled = showReaderCustomisations,
                    onClick = { showReaderCustomisations = false },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            // THE SINGLE LAYER CONTAINER
            Box(
                modifier = Modifier
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                    .widthIn(max = 640.dp)
                    // 1. Listen to inner layout shifts and animate the structural frame smoothly
                    .animateBounds(
                        lookaheadScope = this@LookaheadScope,
                        boundsTransform = { _, _ ->
                            spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            )
                        },
                    )
                    // 2. Stable backgrounds & shapes applied to the single wrapping element
                    .clip(backgroundShape)
                    .background(Color.Black)
                    .graphicsLayer { clip = true }
                    .clickable(
                        onClick = {},
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ),
            ) {
                // 3. Swap the views internally inside a single AnimatedContent window
                AnimatedContent(
                    targetState = showReaderCustomisations,
                    label = "ContentSwitch",
                    contentAlignment = Alignment.BottomCenter,
                ) { targetState ->
                    if (targetState) {
                        // --- EXPANDED PANEL UI CONTENT ---
                        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("Color scheme", color = Color.White)
                                Text(
                                    "Close",
                                    color = Color.LightGray,
                                    modifier = Modifier.clickable { showReaderCustomisations = false },
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            // Your color swatches and text layouts go here...
                            Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(Color.DarkGray, RoundedCornerShape(12.dp)))
                        }
                    } else {
                        // --- COMPACT PILL UI CONTENT ---
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(32.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.TextFormat, "Text", tint = Color.LightGray)
                            Icon(Icons.Default.Palette, "Theme", tint = Color.LightGray)
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White,
                                modifier = Modifier.clickable { showReaderCustomisations = true },
                            )
                        }
                    }
                }
            }
        }
    }
}
