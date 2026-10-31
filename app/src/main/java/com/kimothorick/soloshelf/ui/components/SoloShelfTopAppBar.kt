package com.kimothorick.soloshelf.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.ui.screens.settings.LocalSettingsPane
import com.kimothorick.soloshelf.ui.screens.settings.SettingsPane
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.ThemePreviews

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SoloShelfTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    showNavigationIcon: Boolean = LocalSettingsPane.current != SettingsPane.DETAIL,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors? = null,
) {
    LargeTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        },
        navigationIcon = {
            if (showNavigationIcon) {
                CircularIconButton(
                    modifier = Modifier.padding(start = 12.dp),
                    icon = R.drawable.ic_arrow_back,
                    label = stringResource(R.string.back),
                    onClick = onBackClick,
                )
            }
        },
        scrollBehavior = scrollBehavior,
        actions = actions,
        windowInsets = windowInsets,
        colors = colors ?: TopAppBarDefaults.topAppBarColors(
            containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
            scrolledContainerColor = SoloShelfTheme.colorScheme.surfaceContainer,
            navigationIconContentColor = SoloShelfTheme.colorScheme.onSurface,
            titleContentColor = SoloShelfTheme.colorScheme.onSurface,
            actionIconContentColor = SoloShelfTheme.colorScheme.onSurface,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreviews
@Composable
private fun SoloShelfTopAppBarNoActionsPreview() {
    SoloShelfTheme {
        SoloShelfTopAppBar(
            title = "Settings",
            onBackClick = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreviews
@Composable
private fun SoloShelfTopAppBarCollapsedPreview() {
    SoloShelfTheme {
        SoloShelfTopAppBar(
            title = "Settings",
            onBackClick = {},
            scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
                rememberTopAppBarState(initialHeightOffset = -Float.MAX_VALUE),
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreviews
@Composable
private fun SoloShelfTopAppBarWithActionsPreview() {
    SoloShelfTheme {
        SoloShelfTopAppBar(
            title = "Library",
            onBackClick = {},
            actions = {
                CircularIconButton(
                    icon = R.drawable.ic_search,
                    label = "Search",
                    onClick = {},
                )
                Spacer(Modifier.width(8.dp))
                CircularIconButton(
                    icon = R.drawable.ic_more_vert,
                    label = "More",
                    onClick = {},
                )
                Spacer(Modifier.width(12.dp))
            },
        )
    }
}
