package com.kimothorick.soloshelf.util

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

@Composable
fun isAtLeastMedium(): Boolean = currentWindowAdaptiveInfoV2().windowSizeClass.isAtLeastMedium

val WindowSizeClass.isAtLeastMedium: Boolean
    get() = isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
