package com.kimothorick.soloshelf.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.DevicePreviews
import com.kimothorick.soloshelf.ui.tooling.ThemePreviews
import me.saket.cascade.CascadeDropdownMenu

@Composable
fun MainAppBar(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit,
    onShowSortSheet: () -> Unit,
    onAddBookClick: () -> Unit,
    onAddFolderClick: () -> Unit,
    onCreateShelfClick: () -> Unit,
) {
    val filterLabel = stringResource(R.string.filter)
    val settingsLabel = stringResource(R.string.settings)
    val addLabel = "Add"

    var showAddMenu by remember { mutableStateOf(false) }

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
            containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
            scrolledContainerColor = SoloShelfTheme.colorScheme.surfaceContainer,
        ),
        actions = {
            AppBarRow(modifier = Modifier.padding(end = 16.dp)) {
                clickableItem(
                    onClick = { showAddMenu = true },
                    icon = {
                        Column {
                            CircularIconButton(
                                icon = R.drawable.ic_add,
                                label = addLabel,
                                onClick = { showAddMenu = true },
                            )

                            Spacer(Modifier.height(8.dp))
                            CascadeDropdownMenu(
                                shape = MaterialTheme.shapes.large,
                                modifier = Modifier
                                    .sizeIn(minWidth = 216.dp)
                                    .background(color = SoloShelfTheme.colorScheme.surface),
                                expanded = showAddMenu,
                                onDismissRequest = { showAddMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Add Book", style = MaterialTheme.typography.bodyMedium) },
                                    onClick = {
                                        showAddMenu = false
                                        onAddBookClick()
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Add Folder", style = MaterialTheme.typography.bodyMedium) },
                                    onClick = {
                                        showAddMenu = false
                                        onAddFolderClick()
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Create Shelf", style = MaterialTheme.typography.bodyMedium) },
                                    onClick = {
                                        showAddMenu = false
                                        onCreateShelfClick()
                                    },
                                )
                            }
                        }
                    },
                    label = addLabel,
                )

                clickableItem(
                    onClick = onShowSortSheet,
                    icon = {
                        CircularIconButton(
                            icon = R.drawable.ic_filter,
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
                            icon = R.drawable.ic_settings,
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
            onAddBookClick = {},
            onAddFolderClick = {},
            onCreateShelfClick = {},
        )
    }
}
