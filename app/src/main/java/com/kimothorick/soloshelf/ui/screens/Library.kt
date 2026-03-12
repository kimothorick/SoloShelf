package com.kimothorick.soloshelf.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.BookAndProgress
import com.kimothorick.soloshelf.data.models.ReadingProgress
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.ThemePreviews

@Composable
fun Library(
    onNavigateToBookDetails: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val recentBooks = viewModel.recentBooks.collectAsLazyPagingItems()
    val allBooks = viewModel.allBooks.collectAsLazyPagingItems()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(
            key = "recent_books_section",
            span = { GridItemSpan(maxLineSpan) },
        ) {
            AnimatedVisibility(
                visible = recentBooks.itemCount > 0,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(modifier = Modifier.animateContentSize(animationSpec = tween(durationMillis = 300))) {
                    SectionTitle("Currently Reading", modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(16.dp))
                    RecentBooksRow(recentBooks, onNavigateToBookDetails)
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        item(
            key = "all_books_title",
            span = { GridItemSpan(maxLineSpan) },
        ) {
            SectionTitle("All Books", modifier = Modifier.padding(horizontal = 16.dp))
        }

        items(
            count = allBooks.itemCount,
            key = allBooks.itemKey { it.book.bookId },
        ) { index ->
            allBooks[index]?.let { bookAndProgress ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    BookListItem(bookAndProgress, onClick = { onNavigateToBookDetails(bookAndProgress.book.bookId) })
                }
            }
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
    )
}

@Composable
private fun RecentBooksRow(
    books: LazyPagingItems<BookAndProgress>,
    onBookClick: (Long) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.animateContentSize(),
    ) {
        items(
            count = books.itemCount,
            key = books.itemKey { it.book.bookId },
        ) { index ->
            books[index]?.let { bookAndProgress ->
                RecentBookItem(bookAndProgress, onClick = { onBookClick(bookAndProgress.book.bookId) })
            }
        }
    }
}

@Composable
private fun RecentBookItem(
    bookAndProgress: BookAndProgress,
    onClick: () -> Unit,
) {
    val book = bookAndProgress.book
    val progress = bookAndProgress.progress
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(140.dp, 200.dp)
                .clip(RoundedCornerShape(12.dp)),
        ) {
            AsyncImage(
                model = book.coverPath ?: book.filePath,
                contentDescription = book.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.book_icon),
                error = painterResource(R.drawable.book_icon),
            )

            // Format Icon Overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(36.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                tonalElevation = 4.dp,
                shadowElevation = 4.dp,
            ) {
                val icon = if (book.format == "AUDIOBOOK") {
                    Icons.Default.Headphones
                } else {
                    Icons.AutoMirrored.Filled.MenuBook
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = book.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = book.author,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val percentage = progress?.readPercentage ?: 0f
            CircularProgressIndicator(
                progress = { if (percentage > 0) percentage / 100f else 0f },
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
            )
            Spacer(Modifier.width(8.dp))
            val progressText = if (book.format == "AUDIOBOOK") {
                if (progress != null) {
                    val remainingSeconds = (book.totalPages * (1 - progress.readPercentage / 100f)).toLong()
                    val hours = remainingSeconds / 3600
                    val minutes = (remainingSeconds % 3600) / 60
                    if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                } else {
                    val hours = book.totalPages / 3600
                    val minutes = (book.totalPages % 3600) / 60
                    if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                }
            } else {
                if (progress != null) {
                    val pagesRead = (book.totalPages * (progress.readPercentage / 100f)).toInt()
                    "${book.totalPages - pagesRead} pages left"
                } else {
                    "${book.totalPages} pages left"
                }
            }
            Text(
                text = progressText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BookListItem(
    bookAndProgress: BookAndProgress,
    onClick: () -> Unit,
) {
    val book = bookAndProgress.book
    val progress = bookAndProgress.progress
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            AsyncImage(
                model = book.coverPath ?: book.filePath,
                contentDescription = book.title,
                modifier = Modifier
                    .size(80.dp, 120.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.book_icon),
                error = painterResource(R.drawable.book_icon),
            )
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.height(120.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "by ${book.author}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    if (book.totalPages > 0 || book.format == "AUDIOBOOK") {
                        val pagesText = if (book.format == "AUDIOBOOK") {
                            if (progress != null) {
                                // Assuming totalPages stores duration in seconds for audiobooks
                                val remainingSeconds = (book.totalPages * (1 - progress.readPercentage / 100f)).toLong()
                                val hours = remainingSeconds / 3600
                                val minutes = (remainingSeconds % 3600) / 60
                                if (hours > 0) "${hours}h ${minutes}m left" else "${minutes}m left"
                            } else {
                                val hours = book.totalPages / 3600
                                val minutes = (book.totalPages % 3600) / 60
                                if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                            }
                        } else {
                            if (progress != null) {
                                val pagesRead = (book.totalPages * (progress.readPercentage / 100f)).toInt()
                                "${book.totalPages - pagesRead} pages left"
                            } else {
                                "${book.totalPages} pages"
                            }
                        }
                        Text(
                            text = pagesText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        )
                    }
                }

                if (progress != null && progress.readPercentage > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        LinearProgressIndicator(
                            progress = { progress.readPercentage / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${progress.readPercentage.toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = if (book.format == "AUDIOBOOK") {
                        Icons.Default.Headphones
                    } else {
                        Icons.AutoMirrored.Filled.MenuBook
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = book.format.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun BookListItemPreview() {
    SoloShelfTheme {
        Surface {
            BookListItem(
                bookAndProgress = BookAndProgress(
                    book = Book(
                        title = "The Fellowship of the Ring",
                        author = "J.R.R Tolkien",
                        format = "EPUB",
                        totalPages = 234,
                        filePath = "",
                    ),
                    progress = null,
                ),
                onClick = {},
            )
        }
    }
}

@ThemePreviews
@Composable
private fun BookListItemWithProgressPreview() {
    SoloShelfTheme {
        Surface {
            BookListItem(
                bookAndProgress = BookAndProgress(
                    book = Book(
                        title = "The Fellowship of the Ring",
                        author = "J.R.R Tolkien",
                        format = "EPUB",
                        totalPages = 468,
                        filePath = "",
                    ),
                    progress = ReadingProgress(
                        bookId = 1L,
                        lastPosition = "",
                        readPercentage = 50f,
                        lastAccessed = 0L,
                    ),
                ),
                onClick = {},
            )
        }
    }
}

@ThemePreviews
@Composable
private fun AudiobookListItemPreview() {
    SoloShelfTheme {
        Surface {
            BookListItem(
                bookAndProgress = BookAndProgress(
                    book = Book(
                        title = "The Fellowship of the Ring",
                        author = "J.R.R Tolkien",
                        format = "AUDIOBOOK",
                        totalPages = 0,
                        filePath = "",
                    ),
                    progress = null,
                ),
                onClick = {},
            )
        }
    }
}

@ThemePreviews
@Composable
private fun AudiobookListItemWithProgressPreview() {
    SoloShelfTheme {
        Surface {
            BookListItem(
                bookAndProgress = BookAndProgress(
                    book = Book(
                        title = "The Fellowship of the Ring",
                        author = "J.R.R Tolkien",
                        format = "AUDIOBOOK",
                        totalPages = 81800, // Roughly 22h 43m
                        filePath = "",
                    ),
                    progress = ReadingProgress(
                        bookId = 1L,
                        lastPosition = "",
                        readPercentage = 0f,
                        lastAccessed = 0L,
                    ),
                ),
                onClick = {},
            )
        }
    }
}

@ThemePreviews
@Composable
private fun RecentBookItemPreview() {
    SoloShelfTheme {
        Surface {
            RecentBookItem(
                bookAndProgress = BookAndProgress(
                    book = Book(
                        title = "The Fellowship of the Ring",
                        author = "J.R.R Tolkien",
                        format = "EPUB",
                        totalPages = 468,
                        filePath = "",
                    ),
                    progress = ReadingProgress(
                        bookId = 1L,
                        lastPosition = "",
                        readPercentage = 48.5f,
                        lastAccessed = 0L,
                    ),
                ),
                onClick = {},
            )
        }
    }
}

@ThemePreviews
@Composable
private fun RecentAudiobookItemPreview() {
    SoloShelfTheme {
        Surface {
            RecentBookItem(
                bookAndProgress = BookAndProgress(
                    book = Book(
                        title = "Harry Potter and the Sorcerer's Stone",
                        author = "J. K. Rowling",
                        format = "AUDIOBOOK",
                        totalPages = 6060, // Total duration
                        filePath = "",
                    ),
                    progress = ReadingProgress(
                        bookId = 2L,
                        lastPosition = "",
                        readPercentage = 0f, // Simplified for preview
                        lastAccessed = 0L,
                    ),
                ),
                onClick = {},
            )
        }
    }
}
