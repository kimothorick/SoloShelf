package com.kimothorick.soloshelf.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.DevicePreviews
import com.kimothorick.soloshelf.ui.tooling.ThemePreviews

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainAppBar(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit,
    onShowSortSheet: () -> Unit,
) {
    val filterLabel = stringResource(R.string.filter)
    val settingsLabel = stringResource(R.string.settings)

    TopAppBar(
        modifier = modifier.fillMaxWidth(),
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
        ),
        actions = {
            AppBarRow(modifier = Modifier.padding(end = 16.dp)) {
                clickableItem(
                    onClick = onShowSortSheet,
                    icon = {
                        CircularIconButton(
                            icon = Icons.Rounded.FilterList,
                            label = filterLabel,
                            onClick = onShowSortSheet,
                        )
                    },
                    label = filterLabel,
                )

                clickableItem(
                    onClick = onNavigateToSettings,
                    icon = {
                        CircularIconButton(
                            icon = Icons.Rounded.Settings,
                            label = settingsLabel,
                            onClick = onNavigateToSettings,
                        )
                    },
                    label = settingsLabel,
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@ThemePreviews
@DevicePreviews
@Composable
private fun MainAppBarPreview() {
    SoloShelfTheme {
        MainAppBar(
            onNavigateToSettings = {},
            onShowSortSheet = {},
        )
    }
}
