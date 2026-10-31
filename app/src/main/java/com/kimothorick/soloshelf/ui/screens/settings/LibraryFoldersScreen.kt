package com.kimothorick.soloshelf.ui.screens.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.models.LibraryFolder
import com.kimothorick.soloshelf.ui.components.SoloShelfTopAppBar
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.DevicePreviews
import me.saket.cascade.CascadeDropdownMenu
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LibraryFoldersScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val libraryFolders by viewModel.libraryFolders.collectAsStateWithLifecycle()

    LibraryFoldersScreenContent(
        libraryFolders = libraryFolders,
        onAddFolder = { uriString, name, date -> viewModel.addLibraryFolder(uriString, name, date) },
        onRemoveFolder = { uriString -> viewModel.removeLibraryFolder(uriString) },
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibraryFoldersScreenContent(
    libraryFolders: List<LibraryFolder>,
    onAddFolder: (String, String, String) -> Unit,
    onRemoveFolder: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val context = LocalContext.current

    val treePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            } catch (_: Exception) {
                // Ignore if persistable permission fails or isn't supported
            }

            val docFile = DocumentFile.fromTreeUri(context, it)
            val name = docFile?.name ?: "Library Folder"
            val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
            val dateStr = dateFormat.format(Date())

            onAddFolder(it.toString(), name, dateStr)
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SoloShelfTopAppBar(
                title = stringResource(R.string.manage_library_folders),
                onBackClick = onBack,
                showNavigationIcon = true,
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { treePickerLauncher.launch(null) },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text(text = "Add folder") },
                modifier = Modifier.padding(bottom = 12.dp),
            )
        },
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 80.dp,
            ),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_info),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.manage_library_folders_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (libraryFolders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No library folders added yet.\nTap 'Add folder' to start indexing.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            } else {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                        modifier = Modifier.clip(
                            shape = MaterialTheme.shapes.extraLarge,
                        ),
                    ) {
                        libraryFolders.forEachIndexed { index, folder ->
                            var menuExpanded by remember { mutableStateOf(false) }

                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = folder.name,
                                        style = SoloShelfTheme.typography.titleMedium,
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = folder.dateAdded,
                                        style = SoloShelfTheme.typography.bodyMedium,
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Outlined.Folder,
                                        contentDescription = null,
                                    )
                                },
                                trailingContent = {
                                    Box {
                                        IconButton(onClick = { menuExpanded = true }) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Folder options",
                                            )
                                        }
                                        CascadeDropdownMenu(
                                            shape = MaterialTheme.shapes.large,
                                            modifier = Modifier
                                                .sizeIn(minWidth = 216.dp)
                                                .background(color = SoloShelfTheme.colorScheme.surface),
                                            expanded = menuExpanded,
                                            onDismissRequest = { menuExpanded = false },
                                        ) {
                                            DropdownMenuItem(
                                                leadingIcon = {
                                                    Icon(
                                                        modifier = Modifier.size(20.dp),
                                                        painter = painterResource(R.drawable.ic_sync),
                                                        contentDescription = null,
                                                    )
                                                },
                                                text = {
                                                    Text(
                                                        "Resync books",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                    )
                                                },
                                                onClick = {
                                                    menuExpanded = false
                                                },
                                            )
                                            DropdownMenuItem(
                                                leadingIcon = {
                                                    Icon(
                                                        modifier = Modifier.size(20.dp),
                                                        painter = painterResource(R.drawable.ic_delete),
                                                        contentDescription = null,
                                                    )
                                                },
                                                text = {
                                                    Text(
                                                        "Remove folder",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                    )
                                                },
                                                onClick = {
                                                    menuExpanded = false
                                                    onRemoveFolder(folder.uri)
                                                },
                                            )
                                        }
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@DevicePreviews
@Composable
private fun LibraryFoldersScreenPreview() {
    SoloShelfTheme {
        LibraryFoldersScreenContent(
            libraryFolders = listOf(
                LibraryFolder("content://media/downloads", "Downloads", "25 June 2026"),
                LibraryFolder("content://media/ebooks", "Ebooks", "25 June 2026"),
            ),
            onAddFolder = { _, _, _ -> },
            onRemoveFolder = {},
            onBack = {},
        )
    }
}
