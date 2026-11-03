package com.kimothorick.soloshelf.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.preferences.SortOrder
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.DevicePreviews
import com.kimothorick.soloshelf.ui.tooling.ThemePreviews

@Composable
fun SortBottomSheetContent(
    currentSortOrder: SortOrder,
    onSortOrderSelected: (SortOrder) -> Unit,
) {
    var selectedBaseSort by remember(currentSortOrder) {
        mutableStateOf(
            when (currentSortOrder) {
                SortOrder.TITLE_ASC, SortOrder.TITLE_DESC -> "TITLE"
                SortOrder.AUTHOR_ASC, SortOrder.AUTHOR_DESC -> "AUTHOR"
                SortOrder.DATE_ADDED_ASC, SortOrder.DATE_ADDED_DESC -> "DATE"
                SortOrder.LAST_READ_ASC, SortOrder.LAST_READ_DESC -> "LAST_READ"
            },
        )
    }

    var isAscending by remember(currentSortOrder) {
        mutableStateOf(
            currentSortOrder == SortOrder.TITLE_ASC || currentSortOrder == SortOrder.AUTHOR_ASC || currentSortOrder == SortOrder.DATE_ADDED_ASC || currentSortOrder == SortOrder.LAST_READ_ASC,
        )
    }

    LaunchedEffect(selectedBaseSort, isAscending) {
        val newSortOrder = when (selectedBaseSort) {
            "TITLE" -> if (isAscending) SortOrder.TITLE_ASC else SortOrder.TITLE_DESC
            "AUTHOR" -> if (isAscending) SortOrder.AUTHOR_ASC else SortOrder.AUTHOR_DESC
            "DATE" -> if (isAscending) SortOrder.DATE_ADDED_ASC else SortOrder.DATE_ADDED_DESC
            "LAST_READ" -> if (isAscending) SortOrder.LAST_READ_ASC else SortOrder.LAST_READ_DESC
            else -> currentSortOrder
        }
        if (newSortOrder != currentSortOrder) {
            onSortOrderSelected(newSortOrder)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = SoloShelfTheme.colorScheme.surfaceContainer,
            ).padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = stringResource(R.string.sort_by),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
        )

        val sortOptions = listOf(
            ToggleableButtonItem(stringResource(R.string.sort_order_title), selectedBaseSort == "TITLE", "TITLE"),
            ToggleableButtonItem(stringResource(R.string.sort_order_date_added), selectedBaseSort == "DATE", "DATE"),
            ToggleableButtonItem(stringResource(R.string.sort_order_author), selectedBaseSort == "AUTHOR", "AUTHOR"),
            ToggleableButtonItem(
                stringResource(R.string.sort_order_last_read),
                selectedBaseSort == "LAST_READ",
                "LAST_READ",
            ),
        )

        ToggleableButtonGroup(
            items = sortOptions,
            onItemSelected = { selectedBaseSort = it.identifier as String },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.order),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        val orderOptions = listOf(
            ToggleableButtonItem(stringResource(R.string.ascending), isAscending, true, Icons.Rounded.ArrowUpward),
            ToggleableButtonItem(stringResource(R.string.descending), !isAscending, false, Icons.Rounded.ArrowDownward),
        )

        ToggleableButtonGroup(
            items = orderOptions,
            onItemSelected = { isAscending = it.identifier as Boolean },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@ThemePreviews
@DevicePreviews
@Composable
private fun SortBottomSheetPreview() {
    SoloShelfTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(
                    Modifier
                        .padding(vertical = 12.dp)
                        .size(width = 32.dp, height = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape,
                        ),
                )
                SortBottomSheetContent(
                    currentSortOrder = SortOrder.LAST_READ_DESC,
                    onSortOrderSelected = {},
                )
            }
        }
    }
}
