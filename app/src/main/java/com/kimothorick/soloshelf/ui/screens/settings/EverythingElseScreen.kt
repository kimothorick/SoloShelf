package com.kimothorick.soloshelf.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.ui.components.SoloShelfSegmentedListItem
import com.kimothorick.soloshelf.ui.components.SoloShelfTopAppBar
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.DevicePreviews
import com.kimothorick.soloshelf.util.Constants

@Composable
fun EverythingElseScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EverythingElseScreenContent(
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EverythingElseScreenContent(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SoloShelfTopAppBar(
                title = stringResource(R.string.everything_else),
                onBackClick = onBack,
                scrollBehavior = scrollBehavior,
            )
        },
        containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp),
        ) {
            item {
                SettingsSectionHeader(title = stringResource(R.string.app_info))
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                    modifier = Modifier.clip(
                        shape = MaterialTheme.shapes.extraLarge,
                    ),
                ) {
                    val context = LocalContext.current
                    SoloShelfSegmentedListItem(
                        onClick = { /* TODO: About */ },
                        shapes = ListItemDefaults.segmentedShapes(0, 3),
                        leadingContent = { Icon(painterResource(R.drawable.ic_info), contentDescription = null) },
                        headlineText = stringResource(R.string.about_soloshelf),
                        supportingText = "v1.0.0",
                    )

                    SoloShelfSegmentedListItem(
                        onClick = { /* TODO: Rate app */ },
                        shapes = ListItemDefaults.segmentedShapes(1, 3),
                        leadingContent = { Icon(painterResource(R.drawable.ic_thumb_up), contentDescription = null) },
                        headlineText = stringResource(R.string.rate_app),
                        supportingText = "",
                    )

                    SoloShelfSegmentedListItem(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Constants.SOLOSHELF_GITHUB_URL.toUri())
                            context.startActivity(intent)
                        },
                        shapes = ListItemDefaults.segmentedShapes(2, 3),
                        leadingContent = { Icon(painterResource(R.drawable.ic_github), contentDescription = null) },
                        headlineText = stringResource(R.string.github),
                        supportingText = "",
                    )
                }
            }

            item {
                SettingsSectionHeader(title = stringResource(R.string.credits))
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                    modifier = Modifier.clip(
                        shape = MaterialTheme.shapes.extraLarge,
                    ),
                ) {
                    SoloShelfSegmentedListItem(
                        onClick = { /* TODO: Developer Profile */ },
                        shapes = ListItemDefaults.segmentedShapes(0, 1),
                        leadingContent = { Icon(painterResource(R.drawable.ic_person), contentDescription = null) },
                        headlineText = stringResource(R.string.developer),
                        supportingText = stringResource(R.string.developer_handle),
                    )
                }
            }
        }
    }
}

@DevicePreviews
@Composable
private fun EverythingElseScreenPreview() {
    SoloShelfTheme {
        EverythingElseScreenContent(
            onBack = {},
        )
    }
}
