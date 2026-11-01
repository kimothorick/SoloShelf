package com.kimothorick.soloshelf.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.models.ShelfWithBooks
import com.kimothorick.soloshelf.domain.models.LibraryItem
import com.kimothorick.soloshelf.ui.components.BookItem
import com.kimothorick.soloshelf.ui.components.DeleteBookDialog
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.PreviewData
import com.kimothorick.soloshelf.util.Constants
import com.kimothorick.soloshelf.util.TimeUtils
import com.kimothorick.soloshelf.util.isAtLeastMedium
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf

@Composable
fun Library(
    onNavigateToBookDetails: (Long) -> Unit,
    onNavigateToReader: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val ebooks = viewModel.ebooks.collectAsLazyPagingItems()
    val recentEbooks by viewModel.recentEbooks.collectAsState()
    val shelves by viewModel.shelves.collectAsState()

    LibraryContent(
        ebooks = ebooks,
        recentEbooks = recentEbooks,
        shelves = shelves.toImmutableList(),
        onNavigateToBookDetails = onNavigateToBookDetails,
        onDeleteBook = viewModel::deleteBook,
        onAddBookToShelf = viewModel::addBookToShelf,
        onRemoveBookFromShelf = viewModel::removeBookFromShelf,
        onCreateShelf = viewModel::createShelf,
        onNavigateToReader = onNavigateToReader,
        onFavoriteClick = viewModel::toggleFavorite,
        onNotStartedClick = viewModel::resetProgress,
        onMarkFinishedClick = viewModel::markFinished,
    )
}

@Composable
fun LibraryContent(
    ebooks: LazyPagingItems<LibraryItem.Ebook>,
    recentEbooks: ImmutableList<LibraryItem.Ebook>,
    shelves: ImmutableList<ShelfWithBooks>,
    onNavigateToBookDetails: (Long) -> Unit,
    onNavigateToReader: (Long) -> Unit,
    onDeleteBook: (Long) -> Unit,
    onAddBookToShelf: (Long, Long) -> Unit,
    onRemoveBookFromShelf: (Long, Long) -> Unit,
    onCreateShelf: (String) -> Unit,
    onFavoriteClick: (Long) -> Unit = {},
    onNotStartedClick: (Long) -> Unit = {},
    onMarkFinishedClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var bookIdToDelete by remember { mutableStateOf<Long?>(null) }
    var bookTitleToDelete by remember { mutableStateOf("") }
    var bookCoverToDelete by remember { mutableStateOf<String?>(null) }

    if (bookIdToDelete != null) {
        DeleteBookDialog(
            bookTitle = bookTitleToDelete,
            coverPath = bookCoverToDelete,
            onConfirm = {
                onDeleteBook(bookIdToDelete!!)
                bookIdToDelete = null
                bookCoverToDelete = null
            },
            onDismiss = {
                bookIdToDelete = null
                bookCoverToDelete = null
            },
        )
    }

    val refreshState = ebooks.loadState.refresh
    val isLoading = refreshState is androidx.paging.LoadState.Loading

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading && ebooks.itemCount == 0 && recentEbooks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize())
        } else if (ebooks.itemCount == 0 && recentEbooks.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.book_icon),
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.no_books_added),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LibraryGrid(
                recentBooks = recentEbooks,
                allBooks = ebooks,
                shelves = shelves,
                onItemClick = onNavigateToBookDetails,
                onDeleteBook = { id, title, coverPath ->
                    bookIdToDelete = id
                    bookTitleToDelete = title
                    bookCoverToDelete = coverPath
                },
                onAddBookToShelf = onAddBookToShelf,
                onRemoveBookFromShelf = onRemoveBookFromShelf,
                onCreateShelf = onCreateShelf,
                onNavigateToReader = onNavigateToReader,
                onFavoriteClick = onFavoriteClick,
                onNotStartedClick = onNotStartedClick,
                onMarkFinishedClick = onMarkFinishedClick,
            )
        }
    }
}

@Composable
fun LibraryGrid(
    recentBooks: ImmutableList<LibraryItem>,
    allBooks: LazyPagingItems<out LibraryItem>,
    shelves: ImmutableList<ShelfWithBooks>,
    onItemClick: (Long) -> Unit,
    onDeleteBook: (Long, String, String?) -> Unit,
    onAddBookToShelf: (Long, Long) -> Unit,
    onRemoveBookFromShelf: (Long, Long) -> Unit,
    onCreateShelf: (String) -> Unit,
    onNavigateToReader: (Long) -> Unit,
    onFavoriteClick: (Long) -> Unit = {},
    onNotStartedClick: (Long) -> Unit = {},
    onMarkFinishedClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isAtLeastMedium = isAtLeastMedium()
    val minItemWidth = if (!isAtLeastMedium) Constants.EBOOK_ITEM_WIDTH_COMPACT else Constants.EBOOK_ITEM_WIDTH_MEDIUM
    val context = LocalContext.current
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = minItemWidth),
        modifier = modifier.fillMaxSize().background(color = SoloShelfTheme.colorScheme.surfaceContainer),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 100.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item(
            key = "recent_books_section",
            span = { GridItemSpan(maxLineSpan) },
        ) {
            Column(
                modifier = Modifier.ignoreGridPadding(16.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = TimeUtils.getGreeting(context),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = stringResource(R.string.continue_where_you_left_off),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                RecentBooksRow(
                    books = recentBooks,
                    onBookClick = onNavigateToReader,
                    onDeleteBook = onDeleteBook,
                    shelves = shelves,
                    onAddBookToShelf = onAddBookToShelf,
                )
                SectionTitle(
                    stringResource(R.string.all_books),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        items(
            count = allBooks.itemCount,
            key = allBooks.itemKey { it.id },
        ) { index ->
            allBooks[index]?.let { libraryItem ->
                BookItem(
                    modifier = Modifier.fillMaxWidth(),
                    libraryItem = libraryItem,
                    showProgress = false,
                    onClick = { onItemClick(libraryItem.id) },
                    onDelete = { onDeleteBook(libraryItem.id, libraryItem.title, libraryItem.coverPath) },
                    shelves = shelves,
                    onAddToShelf = { shelfId, shouldAdd ->
                        if (shouldAdd) {
                            onAddBookToShelf(libraryItem.id, shelfId)
                        } else {
                            onRemoveBookFromShelf(libraryItem.id, shelfId)
                        }
                    },
                    onCreateShelf = onCreateShelf,
                    onFavoriteClick = { onFavoriteClick(libraryItem.id) },
                    onNotStartedClick = { onNotStartedClick(libraryItem.id) },
                    onMarkFinishedClick = { onMarkFinishedClick(libraryItem.id) },
                )
            }
        }
    }
}

private fun Modifier.ignoreGridPadding(
    padding: Dp,
): Modifier =
    layout { measurable, constraints ->
        val paddingPx = padding.roundToPx()
        val expandedWidth = constraints.maxWidth + paddingPx * 2
        val placeable = measurable.measure(
            constraints.copy(
                minWidth = expandedWidth,
                maxWidth = expandedWidth,
            ),
        )
        layout(constraints.maxWidth, placeable.height) {
            placeable.place(-paddingPx, 0)
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
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun RecentBooksRow(
    books: List<LibraryItem>,
    onBookClick: (Long) -> Unit,
    onDeleteBook: (Long, String, String?) -> Unit,
    shelves: List<ShelfWithBooks>,
    onAddBookToShelf: (Long, Long) -> Unit,
) {
    val isAtLeastMedium = isAtLeastMedium()
    val cardWidth = if (isAtLeastMedium) 320.dp else 360.dp

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(
            items = books,
            key = { it.id },
        ) { libraryItem ->

            Row(
                modifier = Modifier
                    .size(cardWidth, 120.dp)
                    .clip(shape = MaterialTheme.shapes.extraLarge)
                    .background(SoloShelfTheme.colorScheme.surfaceContainerHighest)
                    .clickable { onBookClick(libraryItem.id) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = remember(libraryItem.coverPath) {
                        libraryItem.coverPath?.let { path ->
                            if (path.startsWith("/") && !path.startsWith("file://")) "file://$path" else path
                        }
                    },
                    contentDescription = libraryItem.title,
                    modifier = Modifier.fillMaxHeight().aspectRatio(0.75f).clip(MaterialTheme.shapes.medium),
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
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
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
                                fontStyle = FontStyle.Italic,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        LinearProgressIndicator(
                            progress = { libraryItem.progressPercentage / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            trackColor = ProgressIndicatorDefaults.linearColor.copy(0.08f),
                        )

                        Text(
                            text = "${libraryItem.progressPercentage.toInt()}% complete",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun LibraryPreview() {
    val ebooks = flowOf(PagingData.from(PreviewData.sampleLibraryEbooks)).collectAsLazyPagingItems()

    SoloShelfTheme {
        Surface(color = SoloShelfTheme.colorScheme.surfaceContainer) {
            LibraryContent(
                ebooks = ebooks,
                recentEbooks = PreviewData.sampleLibraryEbooks.take(2).toImmutableList(),
                shelves = PreviewData.sampleShelves.toImmutableList(),
                onNavigateToBookDetails = {},
                onNavigateToReader = {},
                onDeleteBook = { _ -> },
                onAddBookToShelf = { _, _ -> },
                onRemoveBookFromShelf = { _, _ -> },
                onCreateShelf = {},
            )
        }
    }
}
