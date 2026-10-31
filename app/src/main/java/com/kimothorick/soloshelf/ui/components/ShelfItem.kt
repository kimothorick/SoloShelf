package com.kimothorick.soloshelf.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.ShelfWithBooks

@Composable
fun ShelfItem(
    shelfWithBooks: ShelfWithBooks,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val books = shelfWithBooks.books
    val itemsCount = books.size

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (books.isNotEmpty()) {
                    if (books.size > 1) {
                        BookCoverCard(
                            book = books[1],
                            modifier = Modifier
                                .width(70.dp)
                                .height(105.dp)
                                .rotate(-12f)
                                .align(Alignment.Center)
                                .offset(x = (-32).dp, y = 4.dp),
                        )
                    }

                    if (books.size > 2) {
                        BookCoverCard(
                            book = books[2],
                            modifier = Modifier
                                .width(70.dp)
                                .height(105.dp)
                                .rotate(12f)
                                .align(Alignment.Center)
                                .offset(x = 32.dp, y = 4.dp),
                        )
                    }

                    BookCoverCard(
                        book = books[0],
                        modifier = Modifier
                            .width(80.dp)
                            .height(120.dp)
                            .align(Alignment.Center),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        AsyncImage(
                            model = null,
                            contentDescription = null,
                            placeholder = painterResource(R.drawable.book_icon),
                            error = painterResource(R.drawable.book_icon),
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = shelfWithBooks.shelf.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = pluralStringResource(R.plurals.shelf_books_count, itemsCount, itemsCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun BookCoverCard(
    book: Book,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 4.dp,
        modifier = modifier,
    ) {
        AsyncImage(
            model = book.coverPath,
            contentDescription = book.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.book_icon),
            error = painterResource(R.drawable.book_icon),
        )
    }
}
