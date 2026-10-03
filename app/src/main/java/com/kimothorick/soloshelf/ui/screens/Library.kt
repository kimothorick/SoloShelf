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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    onNavigateToPlayer: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val ebooks = viewModel.ebooks.collectAsLazyPagingItems()
    val audiobooks = viewModel.audiobooks.collectAsLazyPagingItems()
    val recentEbooks = viewModel.recentEbooks.collectAsLazyPagingItems()
    val recentAudiobooks = viewModel.recentAudiobooks.collectAsLazyPagingItems()
    val collections by viewModel.collections.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    LibraryContent(
        ebooks = ebooks,
        audiobooks = audiobooks,
        recentEbooks = recentEbooks.itemSnapshotList.items.toImmutableList(),
        recentAudiobooks = recentAudiobooks.itemSnapshotList.items.toImmutableList(),
        collections = collections.toImmutableList(),
        selectedTab = selectedTab,
        onNavigateToBookDetails = onNavigateToBookDetails,
        onNavigateToPlayer = onNavigateToPlayer,
        onSelectTab = viewModel::selectTab,
        onDeleteBook = viewModel::deleteBook,
        onAddBookToCollection = viewModel::addBookToCollection,
        modifier = modifier,
    )
}

@Composable
fun LibraryContent(
    ebooks: LazyPagingItems<LibraryItem.Ebook>,
    audiobooks: LazyPagingItems<LibraryItem.Audiobook>,
    recentEbooks: ImmutableList<LibraryItem.Ebook>,
    recentAudiobooks: ImmutableList<LibraryItem.Audiobook>,
    collections: ImmutableList<CollectionWithBooks>,
    selectedTab: LibraryTab,
    onNavigateToBookDetails: (Long) -> Unit,
    onNavigateToPlayer: (Long) -> Unit,
    onSelectTab: (LibraryTab) -> Unit,
    onDeleteBook: (Long) -> Unit,
    onAddBookToCollection: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(
        initialPage = selectedTab.ordinal,
        pageCount = { LibraryTab.entries.size },
    )

    // Sync selectedTab from ViewModel to pagerState
    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab.ordinal) {
            pagerState.animateScrollToPage(selectedTab.ordinal)
        }
    }

    // Sync pagerState to ViewModel
    LaunchedEffect(pagerState.currentPage) {
        onSelectTab(LibraryTab.entries[pagerState.currentPage])
    }

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
        PrimaryScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                    width = 40.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp),
                )
            },
            divider = {
                HorizontalDivider(
                    color = SoloShelfTheme.colorScheme.outlineVariant,
                )
            },
            edgePadding = 0.dp,
        ) {
            LibraryTab.entries.forEach { tab ->
                Tab(
                    selected = pagerState.currentPage == tab.ordinal,
                    onClick = { onSelectTab(tab) },
                    text = {
                        Text(
                            text = when (tab) {
                                LibraryTab.Ebooks -> stringResource(R.string.ebooks)
                                LibraryTab.Audiobooks -> stringResource(R.string.audiobooks)
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (pagerState.currentPage == tab.ordinal) FontWeight.Bold else FontWeight.Medium,
                            color = if (pagerState.currentPage == tab.ordinal) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
        ) { page ->
            val tab = LibraryTab.entries[page]
            LibraryGrid(
                tab = tab,
                recentBooks = if (tab == LibraryTab.Ebooks) recentEbooks else recentAudiobooks,
                allBooks = if (tab == LibraryTab.Ebooks) ebooks else audiobooks,
                collections = collections,
                onItemClick = if (tab == LibraryTab.Ebooks) onNavigateToBookDetails else onNavigateToPlayer,
                onNavigateToBookDetails = onNavigateToBookDetails,
                onDeleteBook = { id, title -> 
                    bookIdToDelete = id
                    bookTitleToDelete = title
                },
                onAddBookToCollection = onAddBookToCollection,
            )
        }
    }
}

@Composable
fun LibraryGrid(
    tab: LibraryTab,
    recentBooks: ImmutableList<LibraryItem>,
    allBooks: LazyPagingItems<out LibraryItem>,
    collections: ImmutableList<CollectionWithBooks>,
    onItemClick: (Long) -> Unit,
    onNavigateToBookDetails: (Long) -> Unit,
    onDeleteBook: (Long, String) -> Unit,
    onAddBookToCollection: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAtLeastMedium = isAtLeastMedium()
    val minItemWidth = if (tab == LibraryTab.Audiobooks) {
        if (!isAtLeastMedium) Constants.AUDIOBOOK_ITEM_WIDTH_COMPACT else Constants.AUDIOBOOK_ITEM_WIDTH_MEDIUM
    } else {
        if (!isAtLeastMedium) Constants.EBOOK_ITEM_WIDTH_COMPACT else Constants.EBOOK_ITEM_WIDTH_MEDIUM
    }

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
                    stringResource(
                        when (tab) {
                            LibraryTab.Ebooks -> R.string.currently_reading
                            LibraryTab.Audiobooks -> R.string.continue_listening
                        },
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                RecentBooksRow(
                    books = recentBooks,
                    onBookClick = onItemClick,
                    onViewDetails = onNavigateToBookDetails,
                    onDeleteBook = onDeleteBook,
                    collections = collections,
                    onAddBookToCollection = onAddBookToCollection,
                )
                SectionTitle(
                    stringResource(
                        when (tab) {
                            LibraryTab.Ebooks -> R.string.all_books
                            LibraryTab.Audiobooks -> R.string.all_audiobooks
                        },
                    ),
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
                    showProgress = true,
                    onClick = { onItemClick(libraryItem.id) },
                    onViewDetails = { onNavigateToBookDetails(libraryItem.id) },
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
    onViewDetails: (Long) -> Unit,
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
            val isAudiobooks = libraryItem is LibraryItem.Audiobook
            val width = if (isAudiobooks) {
                if (isAtLeastMedium) Constants.AUDIOBOOK_ITEM_WIDTH_MEDIUM else Constants.AUDIOBOOK_ITEM_WIDTH_COMPACT
            } else {
                if (isAtLeastMedium) Constants.EBOOK_ITEM_WIDTH_MEDIUM else Constants.EBOOK_ITEM_WIDTH_COMPACT
            }
            BookItem(
                modifier = Modifier.width(width),
                libraryItem = libraryItem,
                onClick = { onBookClick(libraryItem.id) },
                onViewDetails = { onViewDetails(libraryItem.id) },
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
    val audiobooks = flowOf(PagingData.empty<LibraryItem.Audiobook>()).collectAsLazyPagingItems()

    SoloShelfTheme {
        Surface(color = SoloShelfTheme.colorScheme.surfaceContainer) {
            LibraryContent(
                ebooks = ebooks,
                audiobooks = audiobooks,
                recentEbooks = PreviewData.sampleLibraryEbooks.take(2).toImmutableList(),
                recentAudiobooks = emptyList<LibraryItem.Audiobook>().toImmutableList(),
                collections = PreviewData.sampleCollections.toImmutableList(),
                selectedTab = LibraryTab.Ebooks,
                onNavigateToBookDetails = {},
                onNavigateToPlayer = {},
                onSelectTab = {},
                onDeleteBook = { _ -> },
                onAddBookToCollection = { _, _ -> },
            )
        }
    }
}
