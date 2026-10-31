package com.kimothorick.soloshelf.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.ui.components.SoloShelfSegmentedListItem
import com.kimothorick.soloshelf.ui.components.SoloShelfTopAppBar
import com.kimothorick.soloshelf.ui.components.Switch
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.DevicePreviews

@Composable
fun LibraryStorageScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToLibraryFolders: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val autoScanOnStartup by viewModel.autoScanOnStartup.collectAsStateWithLifecycle()
    val cacheSize by viewModel.cacheSize.collectAsStateWithLifecycle()

    LibraryStorageScreenContent(
        autoScanOnStartup = autoScanOnStartup,
        onAutoScanOnStartupChange = viewModel::setAutoScanOnStartup,
        cacheSize = cacheSize,
        onClearCache = viewModel::clearCache,
        onNavigateToLibraryFolders = onNavigateToLibraryFolders,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun LibraryStorageScreenContent(
    autoScanOnStartup: Boolean,
    onAutoScanOnStartupChange: (Boolean) -> Unit,
    cacheSize: String,
    onClearCache: () -> Unit,
    onNavigateToLibraryFolders: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SoloShelfTopAppBar(
                title = stringResource(R.string.library_storage),
                onBackClick = onBack,
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp,
            ),
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                    modifier = Modifier.clip(
                        shape = MaterialTheme.shapes.extraLarge,
                    ),
                ) {
                    SoloShelfSegmentedListItem(
                        onClick = onNavigateToLibraryFolders,
                        shapes = ListItemDefaults.segmentedShapes(0, 3),
                        headlineText = stringResource(R.string.manage_library_folders),
                        supportingText = stringResource(R.string.manage_library_folders_desc),
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                            )
                        },
                    )

                    SoloShelfSegmentedListItem(
                        onClick = { /* TODO: Manage switch */ },
                        shapes = ListItemDefaults.segmentedShapes(1, 3),
                        headlineText = stringResource(R.string.auto_scan_on_startup),
                        supportingText = stringResource(R.string.auto_scan_on_startup_desc),
                        trailingContent = {
                            Switch(
                                checked = autoScanOnStartup,
                                onCheckedChange = onAutoScanOnStartupChange,
                            )
                        },
                    )

                    SoloShelfSegmentedListItem(
                        onClick = onClearCache,
                        shapes = ListItemDefaults.segmentedShapes(2, 3),
                        headlineText = stringResource(R.string.clear_image_cache),
                        supportingText = stringResource(R.string.clear_image_cache_desc, cacheSize),
                    )
                }
            }
        }
    }
}

@DevicePreviews
@Composable
private fun LibraryStorageScreenPreview() {
    SoloShelfTheme {
        LibraryStorageScreenContent(
            autoScanOnStartup = true,
            onAutoScanOnStartupChange = {},
            cacheSize = "140mb",
            onClearCache = {},
            onNavigateToLibraryFolders = {},
            onBack = {},
        )
    }
}
