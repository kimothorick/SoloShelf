package com.kimothorick.soloshelf.ui.screens.reader.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kimothorick.soloshelf.data.models.Bookmark
import com.kimothorick.soloshelf.data.models.Note
import com.kimothorick.soloshelf.data.preferences.ColorTheme
import com.kimothorick.soloshelf.data.preferences.ThemeMode
import com.kimothorick.soloshelf.ui.components.ChapterItem
import com.kimothorick.soloshelf.ui.components.ToggleableButtonGroup
import com.kimothorick.soloshelf.ui.components.ToggleableButtonItem
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.PreviewData
import com.kimothorick.soloshelf.ui.tooling.ThemePreviews
import org.json.JSONObject
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Locator

@Composable
fun TableOfContents(
    toc: List<Link>,
    bookmarks: List<Bookmark> = emptyList(),
    notes: List<Note> = emptyList(),
    readPercentage: Float = 0f,
    onLinkSelected: (Link) -> Unit,
    onBookmarkSelected: (Locator) -> Unit = {},
    onNoteSelected: (Locator) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = "Table of Contents",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp),
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "${readPercentage.toInt()}% completed",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))

        val tabs = listOf(
            ToggleableButtonItem("Chapters (${toc.size})", selectedTab == 0, 0),
            ToggleableButtonItem("Bookmarks", selectedTab == 1, 1),
            ToggleableButtonItem("Notes", selectedTab == 2, 2),
        )

        ToggleableButtonGroup(
            items = tabs,
            onItemSelected = { selectedTab = it.identifier as Int },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        when (selectedTab) {
            0 -> ChaptersTabContent(
                toc = toc,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onLinkSelected = onLinkSelected,
            )

            1 -> BookmarksTabContent(
                bookmarks = bookmarks,
                onBookmarkSelected = onBookmarkSelected,
            )

            2 -> NotesTabContent(
                notes = notes,
                onNoteSelected = onNoteSelected,
            )
        }
    }
}

@Composable
private fun ChaptersTabContent(
    toc: List<Link>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onLinkSelected: (Link) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        TextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Search chapters...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            },
            shape = CircleShape,
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SoloShelfTheme.colorScheme.surfaceContainerHighest,
                unfocusedContainerColor = SoloShelfTheme.colorScheme.surfaceContainerHigh,
                disabledContainerColor = SoloShelfTheme.colorScheme.surfaceContainerHigh,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        val filteredToc = remember(searchQuery, toc) {
            if (searchQuery.isBlank()) {
                toc
            } else {
                toc.filter { it.title?.contains(searchQuery, ignoreCase = true) == true }
            }
        }

        if (filteredToc.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No chapters found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                items(filteredToc) { link ->
                    ChapterItem(
                        link = link,
                        onClick = onLinkSelected,
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

@Composable
private fun BookmarksTabContent(
    bookmarks: List<Bookmark>,
    onBookmarkSelected: (Locator) -> Unit,
) {
    if (bookmarks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No bookmarks yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(bookmarks) { bookmark ->
                ListItem(
                    headlineContent = {
                        Text(
                            text = bookmark.title ?: "Bookmark",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            try {
                                Locator.fromJSON(JSONObject(bookmark.location))?.let { locator ->
                                    onBookmarkSelected(locator)
                                }
                            } catch (_: Exception) {
                                // Ignore
                            }
                        },
                )
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
private fun NotesTabContent(
    notes: List<Note>,
    onNoteSelected: (Locator) -> Unit,
) {
    if (notes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No notes yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(notes) { note ->
                ListItem(
                    headlineContent = {
                        Text(
                            text = note.noteText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            try {
                                Locator.fromJSON(JSONObject(note.location))?.let { locator ->
                                    onNoteSelected(locator)
                                }
                            } catch (_: Exception) {
                                // Ignore
                            }
                        },
                )
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@ThemePreviews
@Composable
fun TableOfContentsPreview() {
    SoloShelfTheme(themeMode = ThemeMode.CUSTOM, colorTheme = ColorTheme.TERRACOTTA) {
        Surface {
            TableOfContents(
                toc = PreviewData.sampleToc,
                readPercentage = 42f,
                onLinkSelected = {},
            )
        }
    }
}
