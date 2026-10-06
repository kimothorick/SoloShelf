package com.kimothorick.soloshelf.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.models.CollectionWithBooks
import com.kimothorick.soloshelf.domain.models.LibraryItem
import com.kimothorick.soloshelf.ui.components.BookItem
import com.kimothorick.soloshelf.ui.components.DeleteBookDialog
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.PreviewData
import com.kimothorick.soloshelf.util.Constants
import com.kimothorick.soloshelf.util.isAtLeastMedium
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf

@Composable
fun Library(
    onNavigateToBookDetails: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val ebooks = viewModel.ebooks.collectAsLazyPagingItems()
    val recentEbooks = viewModel.recentEbooks.collectAsLazyPagingItems()
    val collections by viewModel.collections.collectAsState()

    LibraryContent(
        ebooks = ebooks,
        recentEbooks = recentEbooks.itemSnapshotList.items.toImmutableList(),
        collections = collections.toImmutableList(),
        onNavigateToBookDetails = onNavigateToBookDetails,
        onDeleteBook = viewModel::deleteBook,
        onAddBookToCollection = viewModel::addBookToCollection,
        modifier = modifier,
    )
}

@Composable
fun LibraryContent(
    ebooks: LazyPagingItems<LibraryItem.Ebook>,
    recentEbooks: ImmutableList<LibraryItem.Ebook>,
    collections: ImmutableList<CollectionWithBooks>,
    onNavigateToBookDetails: (Long) -> Unit,
    onDeleteBook: (Long) -> Unit,
    onAddBookToCollection: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var bookIdToDelete by remember { mutableStateOf<Long?>(null) }
    var bookTitleToDelete by remember { mutableStateOf("") }

    if (bookIdToDelete != null) {
        DeleteBookDialog(
            bookTitle = bookTitleToDelete,
            onConfirm = {
                onDeleteBook(bookIdToDelete!!)
                bookIdToDelete = null
            },
            onDismiss = { bookIdToDelete = null },
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        LibraryGrid(
            recentBooks = recentEbooks,
            allBooks = ebooks,
            collections = collections,
            onItemClick = onNavigateToBookDetails,
            onDeleteBook = { id, title -> 
                bookIdToDelete = id
                bookTitleToDelete = title
            },
            onAddBookToCollection = onAddBookToCollection,
        )
    }
}

@Composable
fun LibraryGrid(
    recentBooks: ImmutableList<LibraryItem>,
    allBooks: LazyPagingItems<out LibraryItem>,
    collections: ImmutableList<CollectionWithBooks>,
    onItemClick: (Long) -> Unit,
    onDeleteBook: (Long, String) -> Unit,
    onAddBookToCollection: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAtLeastMedium = isAtLeastMedium()
    val minItemWidth = if (!isAtLeastMedium) Constants.EBOOK_ITEM_WIDTH_COMPACT else Constants.EBOOK_ITEM_WIDTH_MEDIUM

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = minItemWidth),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item(
            key = "recent_books_section",
            span = { GridItemSpan(maxLineSpan) },
        ) {
            Column(
                modifier = Modifier
                    .ignoreGridPadding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SectionTitle(
                    stringResource(R.string.currently_reading),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                RecentBooksRow(
                    books = recentBooks,
                    onBookClick = onItemClick,
                    onDeleteBook = onDeleteBook,
                    collections = collections,
                    onAddBookToCollection = onAddBookToCollection,
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
                    onDelete = { onDeleteBook(libraryItem.id, libraryItem.title) },
                    collections = collections.map { it.collection },
                    onAddToCollection = { collectionId ->
                        onAddBookToCollection(libraryItem.id, collectionId)
                    },
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
    )
}

@Composable
private fun RecentBooksRow(
    books: List<LibraryItem>,
    onBookClick: (Long) -> Unit,
    onDeleteBook: (Long, String) -> Unit,
    collections: List<CollectionWithBooks>,
    onAddBookToCollection: (Long, Long) -> Unit,
) {
    val isAtLeastMedium = isAtLeastMedium()

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(
            items = books,
            key = { it.id },
        ) { libraryItem ->
            val width = if (isAtLeastMedium) Constants.EBOOK_ITEM_WIDTH_MEDIUM else Constants.EBOOK_ITEM_WIDTH_COMPACT
            BookItem(
                modifier = Modifier.width(width),
                libraryItem = libraryItem,
                onClick = { onBookClick(libraryItem.id) },
                onDelete = { onDeleteBook(libraryItem.id, libraryItem.title) },
                collections = collections.map { it.collection },
                onAddToCollection = { collectionId ->
                    onAddBookToCollection(libraryItem.id, collectionId)
                },
            )
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
                collections = PreviewData.sampleCollections.toImmutableList(),
                onNavigateToBookDetails = {},
                onDeleteBook = { _ -> },
                onAddBookToCollection = { _, _ -> },
            )
        }
    }
}
