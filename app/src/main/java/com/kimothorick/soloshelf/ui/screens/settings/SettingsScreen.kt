package com.kimothorick.soloshelf.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.preferences.AppTheme
import com.kimothorick.soloshelf.ui.components.SoloShelfSegmentedListItem
import com.kimothorick.soloshelf.ui.components.SoloShelfTopAppBar
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.DevicePreviews

@Composable
fun Settings(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToLibraryStorage: () -> Unit,
    onNavigateToEverythingElse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themePreference by viewModel.themePreference.collectAsStateWithLifecycle()
    val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle()

    SettingsScreenContent(
        themePreference = themePreference,
        dynamicColor = dynamicColor,
        onBack = onBack,
        onNavigateToAppearance = onNavigateToAppearance,
        onNavigateToLibraryStorage = onNavigateToLibraryStorage,
        onNavigateToEverythingElse = onNavigateToEverythingElse,
        onThemeChange = viewModel::setTheme,
        onDynamicColorChange = viewModel::setDynamicColor,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreenContent(
    themePreference: AppTheme,
    dynamicColor: Boolean,
    onBack: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToLibraryStorage: () -> Unit,
    onNavigateToEverythingElse: () -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SoloShelfTopAppBar(
                title = stringResource(R.string.settings_title),
                onBackClick = onBack,
                scrollBehavior = scrollBehavior,
            )
        },
        containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            generalSection(
                onNavigateToAppearance = onNavigateToAppearance,
                onNavigateToLibraryStorage = onNavigateToLibraryStorage,
            )
            otherSection(
                onNavigateToEverythingElse = onNavigateToEverythingElse,
            )

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.generalSection(
    onNavigateToAppearance: () -> Unit,
    onNavigateToLibraryStorage: () -> Unit,
) {
    item {
        SettingsSectionHeader(title = stringResource(R.string.settings_general))
    }

    item {
        Column(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            modifier = Modifier.clip(
                shape = MaterialTheme.shapes.extraLarge,
            ),
        ) {
            SoloShelfSegmentedListItem(
                onClick = onNavigateToAppearance,
                shapes = ListItemDefaults.segmentedShapes(0, 3),
                leadingContent = { Icon(painterResource(R.drawable.ic_palette), contentDescription = null) },
                headlineText = stringResource(R.string.appearance),
                supportingText = stringResource(R.string.appearance_desc),
            )
            SoloShelfSegmentedListItem(
                onClick = { /* TODO: Language Selection */ },
                shapes = ListItemDefaults.segmentedShapes(1, 3),
                leadingContent = { Icon(painterResource(R.drawable.ic_language), contentDescription = null) },
                headlineText = stringResource(R.string.language),
                supportingText = stringResource(R.string.language_english),
            )
            SoloShelfSegmentedListItem(
                onClick = onNavigateToLibraryStorage,
                shapes = ListItemDefaults.segmentedShapes(2, 3),
                leadingContent = { Icon(painterResource(R.drawable.ic_folder), contentDescription = null) },
                headlineText = stringResource(R.string.library_storage),
                supportingText = stringResource(R.string.library_storage_desc),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.otherSection(
    onNavigateToEverythingElse: () -> Unit,
) {
    item {
        SettingsSectionHeader(title = stringResource(R.string.settings_other))
    }

    item {
        Column(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            modifier = Modifier.clip(
                shape = MaterialTheme.shapes.extraLarge,
            ),
        ) {
            SoloShelfSegmentedListItem(
                onClick = { /* TODO: Privacy Policy */ },
                shapes = ListItemDefaults.segmentedShapes(0, 3),
                leadingContent = { Icon(painterResource(R.drawable.ic_privacy_tip), contentDescription = null) },
                headlineText = stringResource(R.string.privacy_policy),
                supportingText = "",
            )
            SoloShelfSegmentedListItem(
                onClick = { /* TODO: Feedback */ },
                shapes = ListItemDefaults.segmentedShapes(1, 3),
                leadingContent = { Icon(painterResource(R.drawable.ic_mail), contentDescription = null) },
                headlineText = stringResource(R.string.feedback),
                supportingText = stringResource(R.string.feedback_desc),
            )
            SoloShelfSegmentedListItem(
                onClick = onNavigateToEverythingElse,
                shapes = ListItemDefaults.segmentedShapes(2, 3),
                leadingContent = { Icon(painterResource(R.drawable.ic_info), contentDescription = null) },
                headlineText = stringResource(R.string.everything_else),
                supportingText = stringResource(R.string.everything_else_desc),
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(
    title: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp, top = 24.dp, bottom = 8.dp),
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@DevicePreviews
@Composable
fun SettingsScreenPreview() {
    SoloShelfTheme {
        SettingsScreenContent(
            themePreference = AppTheme.AUTO,
            dynamicColor = true,
            onBack = {},
            onNavigateToAppearance = {},
            onNavigateToLibraryStorage = {},
            onNavigateToEverythingElse = {},
            onThemeChange = {},
            onDynamicColorChange = {},
        )
    }
}
