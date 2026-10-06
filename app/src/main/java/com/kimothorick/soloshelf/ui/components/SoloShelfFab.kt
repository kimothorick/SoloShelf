package com.kimothorick.soloshelf.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
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
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAddEbookFileClick: () -> Unit,
    onAddFolderClick: () -> Unit,
    onCreateCollectionClick: () -> Unit,
) {
    LaunchedEffect(currentRoute) {
        onExpandedChange(false)
    }

    val menuItems = when (currentRoute) {
        Route.Bookshelf -> listOf(
            FabMenuItem(
                icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                label = "Create Collection",
                onClick = onCreateCollectionClick,
            ),
        )

        else -> listOf(
            FabMenuItem(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                label = "Add Book",
                onClick = onAddEbookFileClick,
            ),
            FabMenuItem(
                icon = Icons.Filled.Headphones,
                label = "Add Folder",
                onClick = onAddFolderClick,
            ),
        )
    }

    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = {
                    if (menuItems.size == 1) {
                        menuItems[0].onClick()
                    } else {
                        onExpandedChange(it)
                    }
                },
                containerCornerRadius = { 100.dp },
            ) {
                Icon(
                    painter = rememberVectorPainter(
                        if (expanded) Icons.Rounded.Close else Icons.Rounded.Add,
                    ),
                    contentDescription = null,
                )
            }
        },
        content = {
            if (menuItems.size > 1) {
                menuItems.forEach { item ->
                    FloatingActionButtonMenuItem(
                        onClick = {
                            item.onClick()
                            onExpandedChange(false)
                        },
                        icon = { },
                        text = { Text(text = item.label) },
                    )
                }
            }
        },
    )
}
