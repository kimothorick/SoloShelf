package com.kimothorick.soloshelf.ui.components

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import coil3.compose.AsyncImage
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.models.Shelf
import com.kimothorick.soloshelf.data.models.ShelfWithBooks
import com.kimothorick.soloshelf.domain.models.LibraryItem
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.ComponentPreviews
import com.kimothorick.soloshelf.util.NumberUtils

@Composable
fun BookItem(
    libraryItem: LibraryItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    shelves: List<ShelfWithBooks>,
    onAddToShelf: (Long, Boolean) -> Unit,
    onCreateShelf: (String) -> Unit,
    onFavoriteClick: () -> Unit = {},
    onNotStartedClick: () -> Unit = {},
    onMarkFinishedClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    showProgress: Boolean? = true,
) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
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
                    .border(
                        width = if (showMenu) 2.dp else 0.dp,
                        color = if (showMenu) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                    ).clip(RoundedCornerShape(12.dp)),
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

                if (libraryItem.lastAccessed != null) {
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

            if (libraryItem.author?.isBlank() == true) {
                stringResource(R.string.unknown_author)
            } else {
                libraryItem.author
            }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (showProgress == true && libraryItem.lastAccessed != null) {
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

        BookBottomSheet(
            modifier = Modifier.align(Alignment.BottomEnd),
            expanded = showMenu,
            bookId = libraryItem.id,
            title = libraryItem.title,
            author = libraryItem.author,
            coverPath = libraryItem.coverPath,
            progressPercentage = libraryItem.progressPercentage,
            onDismiss = { showMenu = false },
            onDelete = onDelete,
            onViewDetails = {
                onClick()
            },
            shelves = shelves,
            onToggleShelf = { shelfId, shouldAdd ->
                onAddToShelf(shelfId, shouldAdd)
            },
            onCreateShelf = onCreateShelf,
            onFavoriteClick = onFavoriteClick,
            onNotStartedClick = onNotStartedClick,
            onMarkFinishedClick = onMarkFinishedClick,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookBottomSheet(
    expanded: Boolean,
    bookId: Long,
    title: String,
    author: String? = null,
    coverPath: String? = null,
    progressPercentage: Float = 0f,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onViewDetails: (() -> Unit)? = null,
    shelves: List<ShelfWithBooks>,
    onToggleShelf: (Long, Boolean) -> Unit,
    onCreateShelf: (String) -> Unit,
    onFavoriteClick: () -> Unit = {},
    onNotStartedClick: () -> Unit = {},
    onMarkFinishedClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(
            SheetValue.Hidden,
            SheetValue.Expanded,
        ),
        confirmValueChange = { it != SheetValue.PartiallyExpanded },
    )
    if (expanded) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss,
            containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
        ) {
            BookBottomSheetContent(
                bookId = bookId,
                title = title,
                author = author,
                coverPath = coverPath,
                progressPercentage = progressPercentage,
                onDismiss = onDismiss,
                onDelete = onDelete,
                onViewDetails = onViewDetails,
                shelves = shelves,
                onToggleShelf = onToggleShelf,
                onCreateShelf = onCreateShelf,
                onFavoriteClick = onFavoriteClick,
                onNotStartedClick = onNotStartedClick,
                onMarkFinishedClick = onMarkFinishedClick,
            )
        }
    }
}

@Composable
fun BookBottomSheetContent(
    bookId: Long,
    title: String,
    author: String? = null,
    coverPath: String? = null,
    progressPercentage: Float = 0f,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onViewDetails: (() -> Unit)? = null,
    shelves: List<ShelfWithBooks>,
    onToggleShelf: (Long, Boolean) -> Unit,
    onCreateShelf: (String) -> Unit,
    onFavoriteClick: () -> Unit = {},
    onNotStartedClick: () -> Unit = {},
    onMarkFinishedClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    initialShowShelvesSubMenu: Boolean = false,
) {
    var showShelvesSubMenu by rememberSaveable { mutableStateOf(initialShowShelvesSubMenu) }
    var addedShelfIds by remember(shelves, bookId) {
        mutableStateOf(
            shelves.filter { s -> s.books.any { it.bookId == bookId } }.map { it.shelf.shelfId }.toSet(),
        )
    }

    val isFavorite = remember(shelves, bookId) {
        shelves
            .find { it.shelf.name.equals("Favorites", ignoreCase = true) }
            ?.books
            ?.any { it.bookId == bookId } == true
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SoloShelfTheme.colorScheme.surfaceContainer)
            .verticalScroll(scrollState)
            .animateContentSize()
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (!showShelvesSubMenu) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = remember(coverPath) {
                        coverPath?.let { path ->
                            if (path.startsWith("/") && !path.startsWith("file://")) "file://$path" else path
                        }
                    },
                    contentDescription = title,
                    modifier = Modifier
                        .height(96.dp)
                        .aspectRatio(0.75f)
                        .clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.book_icon),
                    error = painterResource(R.drawable.book_icon),
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (title.isNotBlank()) {
                            Text(
                                text = title,
                                style = SoloShelfTheme.typography.titleLargeEmphasized,
                                fontWeight = FontWeight.Bold,
                                color = SoloShelfTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (!author.isNullOrBlank()) {
                            Text(
                                text = author,
                                style = SoloShelfTheme.typography.bodyMedium,
                                color = SoloShelfTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        LinearProgressIndicator(
                            progress = { if (progressPercentage > 0) progressPercentage / 100f else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = SoloShelfTheme.colorScheme.onSurface,
                            trackColor = SoloShelfTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        )
                        Text(
                            text = "${progressPercentage.toInt()}% complete",
                            style = SoloShelfTheme.typography.bodySmall,
                            color = SoloShelfTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FilledTonalButton(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 88.dp),
                    onClick = {
                        onFavoriteClick()
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isFavorite) Color(0xFFE16969) else SoloShelfTheme.colorScheme.surfaceContainerHighest,
                        contentColor = if (isFavorite) Color.White else SoloShelfTheme.colorScheme.onSurface,
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )

                        Text(
                            text = stringResource(R.string.favorite),
                            style = SoloShelfTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                FilledTonalButton(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 88.dp),
                    onClick = {
                        onNotStartedClick()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SoloShelfTheme.colorScheme.surfaceContainerHighest,
                        contentColor = SoloShelfTheme.colorScheme.onSurface,
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.StarRate,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )

                        Text(
                            text = "Not started",
                            style = SoloShelfTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                FilledTonalButton(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 88.dp),
                    onClick = {
                        onMarkFinishedClick()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SoloShelfTheme.colorScheme.surfaceContainerHighest,
                        contentColor = SoloShelfTheme.colorScheme.onSurface,
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "Mark finished",
                            style = SoloShelfTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (onViewDetails != null) {
                    ActionCardItem(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        icon = Icons.Outlined.Info,
                        title = stringResource(R.string.book_details),
                        subtitle = "View book info and chapters",
                        onClick = {
                            onDismiss()
                            onViewDetails()
                        },
                    )
                }
                ActionCardItem(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    icon = Icons.Rounded.BarChart,
                    title = stringResource(R.string.add_to_shelf),
                    subtitle = "Organize into custom shelves",
                    trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    onClick = { showShelvesSubMenu = true },
                )
            }

            ActionCardItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                icon = Icons.Default.Delete,
                title = "Delete Book",
                tint = SoloShelfTheme.colorScheme.error,
                onClick = {
                    onDelete()
                    onDismiss()
                },
            )
        } else {
            var showCreateShelfDialog by remember { mutableStateOf(false) }

            if (showCreateShelfDialog) {
                CreateShelfDialog(
                    onDismissRequest = { showCreateShelfDialog = false },
                    onConfirm = { name ->
                        onCreateShelf(name)
                        showCreateShelfDialog = false
                    },
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CircularIconButton(
                    icon = R.drawable.ic_arrow_back,
                    label = "Back",
                    onClick = { showShelvesSubMenu = false },
                )
                if (title.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.add_to_shelf),
                        style = SoloShelfTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoloShelfTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = SoloShelfTheme.colorScheme.outlineVariant,
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (shelves.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SoloShelfTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.no_shelves_found),
                            style = SoloShelfTheme.typography.bodyMedium,
                            color = SoloShelfTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                } else {
                    shelves.forEach { shelfWithBooks ->
                        val shelf = shelfWithBooks.shelf
                        val isAdded = addedShelfIds.contains(shelf.shelfId)
                        val bookCount = shelfWithBooks.books.size
                        ActionCardItem(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            icon = if (isAdded) Icons.Rounded.CheckCircle else Icons.Default.Add,
                            title = shelf.name,
                            subtitle = "$bookCount ${if (bookCount == 1) "book" else "books"}",
                            tint = SoloShelfTheme.colorScheme.onSurface,
                            onClick = {
                                val newIsAdded = !isAdded
                                onToggleShelf(shelf.shelfId, newIsAdded)
                                addedShelfIds = if (newIsAdded) {
                                    addedShelfIds + shelf.shelfId
                                } else {
                                    addedShelfIds - shelf.shelfId
                                }
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FilledTonalButton(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 66.dp),
                    onClick = { showCreateShelfDialog = true },
                    shape = CircleShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SoloShelfTheme.colorScheme.secondaryContainer,
                        contentColor = SoloShelfTheme.colorScheme.onSecondaryContainer,
                    ),
                ) {
                    Text(
                        text = "New shelf",
                        textAlign = TextAlign.Center,
                        style = SoloShelfTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }

                FilledTonalButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 66.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoloShelfTheme.colorScheme.onSurface,
                        contentColor = SoloShelfTheme.colorScheme.surfaceContainerHigh,
                    ),
                ) {
                    Text(
                        text = "Done",
                        style = SoloShelfTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
fun ActionCardItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    tint: Color = SoloShelfTheme.colorScheme.onSurface,
    trailingIcon: ImageVector? = null,
) {
    Surface(
        color = SoloShelfTheme.colorScheme.surfaceContainerHighest,
        onClick = onClick,
        shape = MaterialTheme.shapes.largeIncreased,
        modifier = modifier.fillMaxWidth(),
    ) {
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(24.dp),
                )
            },
            trailingContent = trailingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = SoloShelfTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
            supportingContent = subtitle?.let {
                {
                    Text(
                        text = it,
                        style = SoloShelfTheme.typography.bodyMedium,
                        color = SoloShelfTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
            content = {
                Text(
                    text = title,
                    style = SoloShelfTheme.typography.titleMedium,
                    color = tint,
                    maxLines = 1,
                )
            },
        )
    }
}

@ComponentPreviews
@Composable
private fun BookBottomSheetPreview() {
    SoloShelfTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SoloShelfTheme.colorScheme.surface,
        ) {
            BookBottomSheetContent(
                bookId = 1L,
                title = "I'm Glad My Mom Died",
                // title = "I'm Glad My Mom DiedI'm Glad My Mom DiedI'm Glad My Mom DiedI'm Glad My Mom DiedI'm Glad My Mom DiedI'm Glad My Mom DiedI'm Glad My Mom DiedI'm Glad My Mom DiedI'm Glad My Mom DiedI'm Glad My Mom Died",
                author = "Jennette McCurdy",
                progressPercentage = 50f,
                onDismiss = {},
                onDelete = {},
                onViewDetails = {},
                shelves = listOf(
                    ShelfWithBooks(
                        shelf = Shelf(shelfId = 1, name = "Favorites"),
                        books = emptyList(),
                    ),
                    ShelfWithBooks(
                        shelf = Shelf(shelfId = 2, name = "To Read"),
                        books = emptyList(),
                    ),
                ),
                onToggleShelf = { _, _ -> },
                onCreateShelf = {},
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun BookBottomSheetShelvesPreview() {
    SoloShelfTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SoloShelfTheme.colorScheme.surface,
        ) {
            BookBottomSheetContent(
                bookId = 1L,
                title = "I'm Glad My Mom Died",
                author = "Jennette McCurdy",
                progressPercentage = 50f,
                onDismiss = {},
                onDelete = {},
                onViewDetails = {},
                shelves = listOf(
                    ShelfWithBooks(
                        shelf = Shelf(shelfId = 1, name = "Favorites"),
                        books = emptyList(),
                    ),
                    ShelfWithBooks(
                        shelf = Shelf(shelfId = 2, name = "To Read"),
                        books = emptyList(),
                    ),
                ),
                onToggleShelf = { _, _ -> },
                onCreateShelf = {},
                initialShowShelvesSubMenu = true,
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
                shelves = emptyList(),
                onAddToShelf = { _, _ -> },
                onCreateShelf = {},
            )
        }
    }
}
