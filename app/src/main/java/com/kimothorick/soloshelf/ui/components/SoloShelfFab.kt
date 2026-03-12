package com.kimothorick.soloshelf.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.kimothorick.soloshelf.navigation.Route

data class FabMenuItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
    val contentDescription: String? = null,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SoloShelfFab(
    currentRoute: Route,
    onAddEbookFileClick: () -> Unit,
    onAddEbookFolderClick: () -> Unit,
    onAddAudiobookFileClick: () -> Unit,
    onAddAudiobookFolderClick: () -> Unit,
    onCreateCollectionClick: () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    // Automatically close the FAB menu when switching screens
    LaunchedEffect(currentRoute) {
        expanded = false
    }

    val menuItems = when (currentRoute) {
        Route.Bookshelf -> listOf(
            FabMenuItem(
                icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                label = "Create Collection",
                onClick = onCreateCollectionClick,
            ),
        )

        Route.Audiobooks -> listOf(
            FabMenuItem(
                icon = Icons.Filled.Headphones,
                label = "Add Audiobook",
                onClick = onAddAudiobookFileClick,
            ),
            FabMenuItem(
                icon = Icons.Filled.Folder,
                label = "Add Folder",
                onClick = onAddAudiobookFolderClick,
            ),
        )

        else -> listOf(
            FabMenuItem(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                label = "Add Book",
                onClick = onAddEbookFileClick,
            ),
            FabMenuItem(
                icon = Icons.Filled.Folder,
                label = "Add Folder",
                onClick = onAddEbookFolderClick,
            ),
        )
    }

    // Use FloatingActionButtonMenu for all cases to ensure consistent positioning and sizing
    // as it is part of the Material3 Expressive API and may have different default alignments
    // than a standard FloatingActionButton.
    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = { 
                    if (menuItems.size == 1) {
                        menuItems[0].onClick()
                    } else {
                        expanded = it 
                    }
                },
                containerCornerRadius = { 100.dp },
            ) {
                Icon(
                    painter = rememberVectorPainter(
                        if (expanded) Icons.Rounded.Close else Icons.Rounded.Add
                    ),
                    contentDescription = null,
                )
            }
        },
        content = {
            // Only show menu items if there's more than one
            if (menuItems.size > 1) {
                menuItems.forEach { item ->
                    FloatingActionButtonMenuItem(
                        onClick = {
                            item.onClick()
                            expanded = false
                        },
                        icon = { },
                        text = { Text(text = item.label) },
                    )
                }
            }
        },
    )
}
