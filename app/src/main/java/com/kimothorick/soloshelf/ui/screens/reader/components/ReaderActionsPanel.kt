package com.kimothorick.soloshelf.ui.screens.reader.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.kimothorick.soloshelf.ui.screens.reader.ReaderViewModel
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.CombinedPreviews
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.preferences.FontFamily

enum class PanelState {
    Collapsed,
    ExpandedOptions,
    Player,
}

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
    val density = LocalDensity.current
    val recoil = remember { Animatable(0f) }
    var hasSettledOnce by remember { mutableStateOf(false) }

    // Trigger initial spring recoil impulse when settings toggle
    LaunchedEffect(showSettings) {
        if (!hasSettledOnce) {
            hasSettledOnce = true
            return@LaunchedEffect
        }
        val impulse = with(density) { 16.dp.toPx() }
        recoil.snapTo(if (showSettings) impulse else -impulse)
        recoil.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer { translationY = recoil.value }
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                .navigationBarsPadding()
                .widthIn(max = 640.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .graphicsLayer { clip = true }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        ) {
            AnimatedContent(
                targetState = if (showSettings) PanelState.ExpandedOptions else PanelState.Collapsed,
                label = "BouncyPanelTransition",
                contentAlignment = Alignment.BottomCenter,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = 90)))
                        .togetherWith(fadeOut(animationSpec = tween(durationMillis = 90)))
                        .using(
                            SizeTransform { _, _ ->
                                spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                    visibilityThreshold = IntSize.VisibilityThreshold,
                                )
                            },
                        )
                },
            ) { state ->
                when (state) {
                    PanelState.Collapsed -> {
                        ReaderViewBottomBar(
                            onTocClick = onTocClick,
                            onBookmarkClick = onBookmarkClick,
                            onSettingsClick = onToggleSettings,
                        )
                    }

                    PanelState.ExpandedOptions -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
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
                    }

                    PanelState.Player -> {
                        // Placeholder
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
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.List,
            contentDescription = "Table of Contents",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier
                .padding(vertical = 10.dp)
                .requiredSize(28.dp)
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
                .padding(vertical = 10.dp)
                .requiredSize(28.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBookmarkClick,
                ),
        )

        Icon(
            painter = painterResource(com.kimothorick.soloshelf.R.drawable.ic_settings),
            contentDescription = "Settings",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier
                .padding(vertical = 10.dp)
                .requiredSize(28.dp)
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
