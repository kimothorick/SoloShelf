package com.kimothorick.soloshelf.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.Collection
import com.kimothorick.soloshelf.data.models.CollectionWithBooks
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.ThemePreviews

@Composable
fun Bookshelf(
    viewModel: BookshelfViewModel = hiltViewModel(),
) {
    val collections by viewModel.collections.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionTitle(stringResource(R.string.collections))
        }

        items(collections) { collectionWithBooks ->
            CollectionItem(collectionWithBooks = collectionWithBooks)
        }
    }
}

@Composable
fun CollectionItem(
    collectionWithBooks: CollectionWithBooks,
    modifier: Modifier = Modifier,
) {
    val books = collectionWithBooks.books
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Large cover on the left (2/3 width)
                Box(modifier = Modifier.weight(2.1f).fillMaxHeight()) {
                    if (books.isNotEmpty()) {
                        BookCover(book = books[0], modifier = Modifier.fillMaxSize())
                    }
                }

                Spacer(modifier = Modifier.padding(horizontal = 1.dp))

                // Two small covers on the right (1/3 width)
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        if (books.size > 1) {
                            BookCover(book = books[1], modifier = Modifier.fillMaxSize())
                        }
                    }
                    Spacer(modifier = Modifier.padding(vertical = 1.dp))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        if (books.size > 2) {
                            BookCover(book = books[2], modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = collectionWithBooks.collection.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )

        val itemsCount = books.size
        val authorsCount = books.map { it.author }.distinct().size
        Text(
            text = stringResource(R.string.collection_items_authors, itemsCount, authorsCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun BookCover(
    book: Book,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = book.coverPath ?: book.filePath,
        contentDescription = book.title,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.book_icon),
        error = painterResource(R.drawable.book_icon),
    )
}

@ThemePreviews
@Composable
private fun BookshelfPreview() {
    SoloShelfTheme {
        Surface {
            // Mock data for preview
            val mockBooks = listOf(
                Book(title = "The Fellowship of the Ring", author = "J.R.R Tolkien", format = "EPUB", filePath = ""),
                Book(title = "The Two Towers", author = "J.R.R Tolkien", format = "EPUB", filePath = ""),
                Book(title = "The Return of the King", author = "J.R.R Tolkien", format = "EPUB", filePath = ""),
            )
            val mockCollections = listOf(
                CollectionWithBooks(Collection(name = "Fantasy"), mockBooks),
                CollectionWithBooks(Collection(name = "Fantasy"), mockBooks),
                CollectionWithBooks(Collection(name = "Fantasy"), mockBooks),
                CollectionWithBooks(Collection(name = "Fantasy"), mockBooks),
                CollectionWithBooks(Collection(name = "Fantasy"), mockBooks),
                CollectionWithBooks(Collection(name = "Fantasy"), mockBooks),
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                item(span = { GridItemSpan(2) }) {
                    SectionTitle("Collections")
                }
                items(mockCollections) { item ->
                    CollectionItem(collectionWithBooks = item)
                }
            }
        }
    }
}
