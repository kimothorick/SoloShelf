package com.kimothorick.soloshelf.ui.theme

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.StyleScope
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalFoundationStyleApi::class)
val StyleScope.theme: SoloShelfTheme
    get() = SoloShelfTheme.LocalSoloShelfTheme.currentValue

@OptIn(ExperimentalFoundationStyleApi::class)
val StyleScope.colorScheme: ColorScheme
    get() = theme.colorScheme

@OptIn(ExperimentalFoundationStyleApi::class)
val StyleScope.typography: androidx.compose.material3.Typography
    get() = theme.typography

object SoloShelfStyles {
    @OptIn(ExperimentalFoundationStyleApi::class)
    val segmentedListItem: Style = Style {
        background(colorScheme.surfaceContainer)
    }
}

object SoloShelfTextStyles {
    val segmentedListItemHeaderText: TextStyle
        @Composable get() = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
        )
}
