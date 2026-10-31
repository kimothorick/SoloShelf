package com.kimothorick.soloshelf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.ui.components.ShelfItem
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.PreviewData
import com.kimothorick.soloshelf.ui.tooling.ThemePreviews

@Composable
fun Bookshelf(
    modifier: Modifier = Modifier,
    viewModel: BookshelfViewModel = hiltViewModel(),
) {
    val shelves by viewModel.shelves.collectAsState()
    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier
                .background(color = SoloShelfTheme.colorScheme.surfaceContainer)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionTitle(stringResource(R.string.shelves))
            }

            items(shelves) { shelfWithBooks ->
                ShelfItem(shelfWithBooks = shelfWithBooks)
            }
        }
    }
}

@ThemePreviews
@Composable
private fun BookshelfPreview() {
    SoloShelfTheme {
        Surface {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = SoloShelfTheme.colorScheme.surfaceContainerHighest),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                item(span = { GridItemSpan(2) }) {
                    SectionTitle("Shelves")
                }
                items(PreviewData.sampleShelves) { item ->
                    ShelfItem(shelfWithBooks = item)
                }
            }
        }
    }
}
