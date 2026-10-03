package com.kimothorick.soloshelf.ui.components

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.models.Collection
import com.kimothorick.soloshelf.domain.models.LibraryItem
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.util.NumberUtils
import com.kimothorick.soloshelf.util.TimeUtils
import me.saket.cascade.CascadeDropdownMenu

@Composable
fun BookItem(
    libraryItem: LibraryItem,
    showProgress: Boolean? = true,
    onClick: () -> Unit,
    onViewDetails: (() -> Unit)? = null,
    onDelete: () -> Unit,
    collections: List<Collection>,
    onAddToCollection: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    val isAudiobook = libraryItem is LibraryItem.Audiobook

    val aspectRatio = if (isAudiobook) 1f else 0.75f

    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { showMenu = true },
                )
            },
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(aspectRatio).clip(RoundedCornerShape(12.dp)),
            ) {
                AsyncImage(
                    model = remember(libraryItem.coverPath) {
                        libraryItem.coverPath?.let { path ->
                            if (path.startsWith("/") && !path.startsWith("file://")) "file://$path" else path
                        }
                    },
                    contentDescription = libraryItem.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.book_icon),
                    error = painterResource(R.drawable.book_icon),
                    onError = { state ->
                        Log.e(
                            "BookItem",
                            "Error loading cover image for ${libraryItem.title}: ${state.result.throwable.message}. Path: ${libraryItem.coverPath}",
                        )
                    },
                )

                // Audiobook headphone icon
                if (isAudiobook) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(28.dp),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.5f),
                        contentColor = Color.White,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            modifier = Modifier.padding(6.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = libraryItem.title,
                    style = MaterialTheme.typography.bodyMediumEmphasized,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.titleSmall.lineHeight * 0.9f,
                )

                val displayAuthor = libraryItem.author.ifBlank { stringResource(R.string.unknown_author) }
                Text(
                    text = displayAuthor,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                /* // Specialized Metadata
                 when (libraryItem) {
                     is LibraryItem.Ebook -> {
                         Text(
                             text = stringResource(R.string.pages_count, libraryItem.pageCount.toString()),
                             style = MaterialTheme.typography.labelSmall,
                             color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                         )
                     }
                     is LibraryItem.Audiobook -> {
                         val durationText = TimeUtils.formatDuration(libraryItem.totalDurationMs)
                         val narratorText = libraryItem.narrator?.let { " • $it" } ?: ""
                         Text(
                             text = "$durationText$narratorText",
                             style = MaterialTheme.typography.labelSmall,
                             color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                             maxLines = 1,
                             overflow = TextOverflow.Ellipsis,
                         )
                     }
                 }*/

                if (showProgress == true) {
                    Spacer(Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val percentage = libraryItem.progressPercentage
                        CircularProgressIndicator(
                            progress = { if (percentage > 0) percentage / 100f else 0f },
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 1.8.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        )
                        val progressText = when (libraryItem) {
                            is LibraryItem.Ebook -> {
                                val pagesRead =
                                    (libraryItem.pageCount * (libraryItem.progressPercentage / 100f)).toInt()
                                val remainingPages = libraryItem.pageCount - pagesRead
                                stringResource(R.string.pages_left, NumberUtils.formatLocalizedInteger(remainingPages))
                            }

                            is LibraryItem.Audiobook -> {
                                val remainingMs =
                                    (libraryItem.totalDurationMs * (1 - libraryItem.progressPercentage / 100f)).toLong()
                                stringResource(R.string.time_left, TimeUtils.formatHHMMSS(remainingMs / 1000))
                            }
                        }
                        Text(
                            text = progressText,
                            style = MaterialTheme.typography.bodySmallEmphasized,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        BookDropdownMenu(
            modifier = Modifier.align(Alignment.TopEnd),
            expanded = showMenu,
            onBookClick = onClick,
            onViewDetails = onViewDetails,
            onDismiss = { showMenu = false },
            onDelete = onDelete,
            collections = collections,
            onAddToCollection = onAddToCollection,
            isAudiobook = isAudiobook,
        )
    }
}

@Composable
fun BookDropdownMenu(
    expanded: Boolean,
    onBookClick: () -> Unit,
    onViewDetails: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    collections: List<Collection>,
    onAddToCollection: (Long) -> Unit,
    isAudiobook: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        CascadeDropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            shape = MaterialTheme.shapes.medium,
        ) {
            if (isAudiobook && onViewDetails != null) {
                DropdownMenuItem(
                    text = { Text("Book Details") },
                    onClick = {
                        onViewDetails()
                        onDismiss()
                    },
                    leadingIcon = {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                        )
                    },
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.favorite)) },
                onClick = {
                    /*
                     * TODO add favorites to database and then implement this.
                     */
                },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Rounded.FavoriteBorder,
                        contentDescription = null,
                    )
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.edit)) },
                onClick = {
                    /*
                     * TODO Add edit functionality. Sync with open lib as well
                     */
                },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                    )
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.add_to_collection)) },
                children = {
                    if (collections.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.no_collections_found),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            },
                            onClick = onDismiss,
                            enabled = false,
                        )
                    } else {
                        collections.forEach { collection ->
                            DropdownMenuItem(
                                text = { Text(collection.name) },
                                onClick = {
                                    onAddToCollection(collection.collectionId)
                                    onDismiss()
                                },
                                leadingIcon = {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                    )
                                },
                            )
                        }
                    }
                },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Outlined.Collections,
                        contentDescription = null,
                    )
                },
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(R.string.delete),
                    )
                },
                onClick = {
                    onDelete()
                    onDismiss()
                },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EbookItemPreview() {
    SoloShelfTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Ebook Preview
            BookItem(
                libraryItem = LibraryItem.Ebook(
                    id = 1L,
                    title = "I'm Glad My Mom Died",
                    author = "Jennette McCurdy",
                    filePath = "",
                    coverPath = null,
                    mediaType = null,
                    lastReadLocatorJson = null,
                    lastAccessed = System.currentTimeMillis(),
                    progressPercentage = 10f,
                    pageCount = 304,
                ),
                onClick = {},
                onDelete = {},
                collections = emptyList(),
                onAddToCollection = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AudioBookItemPreview() {
    SoloShelfTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Audiobook Preview
            BookItem(
                libraryItem = LibraryItem.Audiobook(
                    id = 2L,
                    title = "Harry Potter and the Sorcerer's Stone",
                    author = "J. K. Rowling",
                    filePath = "",
                    coverPath = null,
                    mediaType = null,
                    lastReadLocatorJson = null,
                    lastAccessed = System.currentTimeMillis(),
                    progressPercentage = 0f,
                    totalDurationMs = 8 * 3600 * 1000L + 12 * 60 * 1000L + 57 * 1000L,
                    narrator = "Stephen Fry",
                ),
                onClick = {},
                onDelete = {},
                collections = emptyList(),
                onAddToCollection = {},
            )
        }
    }
}
