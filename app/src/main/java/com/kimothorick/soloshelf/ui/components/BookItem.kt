package com.kimothorick.soloshelf.ui.components

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.models.Collection
import com.kimothorick.soloshelf.domain.models.LibraryItem
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.util.NumberUtils
import me.saket.cascade.CascadeDropdownMenu

@Composable
fun BookItem(
    libraryItem: LibraryItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    collections: List<Collection>,
    onAddToCollection: (Long) -> Unit,
    modifier: Modifier = Modifier,
    showProgress: Boolean? = true,
) {
    var showMenu by remember { mutableStateOf(false) }
    val aspectRatio = 0.75f
    val hapticFeedback = LocalHapticFeedback.current

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = {
                            hapticFeedback.performHapticFeedback(
                                HapticFeedbackType.LongPress,
                            )
                            showMenu = true
                        },
                    )
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .clip(RoundedCornerShape(12.dp)),
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

                if (libraryItem.progressPercentage > 0f) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .clip(shape = CircleShape)
                            .background(Color.Black.copy(alpha = 0.8f))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "${libraryItem.progressPercentage.toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = libraryItem.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = libraryItem.author,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (showProgress == true && libraryItem.progressPercentage > 0f) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val percentage = libraryItem.progressPercentage
                    CircularProgressIndicator(
                        progress = { if (percentage > 0) percentage / 100f else 0f },
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 1.8.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    )
                    val ebookItem = libraryItem as? LibraryItem.Ebook
                    if (ebookItem != null) {
                        val progressText = when {
                            ebookItem.progressPercentage >= 100f -> {
                                stringResource(R.string.finished)
                            }

                            ebookItem.pageCount > 0 -> {
                                val pagesRead = (ebookItem.pageCount * (ebookItem.progressPercentage / 100f)).toInt()
                                val remainingPages = ebookItem.pageCount - pagesRead
                                if (remainingPages <= 0) {
                                    stringResource(R.string.finished)
                                } else {
                                    stringResource(
                                        R.string.pages_left,
                                        NumberUtils.formatLocalizedInteger(remainingPages),
                                    )
                                }
                            }

                            else -> {
                                null
                            }
                        }
                        if (progressText != null) {
                            Text(
                                text = progressText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }

        BookDropdownMenu(
            modifier = Modifier.align(Alignment.TopEnd),
            expanded = showMenu,
            onDismiss = { showMenu = false },
            onDelete = onDelete,
            collections = collections,
            onAddToCollection = onAddToCollection,
        )
    }
}

@Composable
private fun BookDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    collections: List<Collection>,
    onAddToCollection: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        CascadeDropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            shape = MaterialTheme.shapes.medium,
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.favorite)) },
                onClick = {},
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
                onClick = {},
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
