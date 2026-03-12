package com.kimothorick.soloshelf.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.preferences.SortOrder
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.DevicePreviews
import com.kimothorick.soloshelf.ui.tooling.ThemePreviews

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SortBottomSheetContent(
    currentSortOrder: SortOrder,
    onSortOrderSelected: (SortOrder) -> Unit,
    onDone: () -> Unit,
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
            currentSortOrder == SortOrder.TITLE_ASC ||
                currentSortOrder == SortOrder.AUTHOR_ASC ||
                currentSortOrder == SortOrder.DATE_ADDED_ASC ||
                currentSortOrder == SortOrder.LAST_READ_ASC,
        )
    }

    // Update sort order in real-time
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
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    ) {
        Text(
            text = stringResource(R.string.sort_by),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp),
        )

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )

        val options = listOf(
            stringResource(R.string.sort_order_title) to "TITLE",
            stringResource(R.string.sort_order_date_added) to "DATE",
            stringResource(R.string.sort_order_author) to "AUTHOR",
            stringResource(R.string.sort_order_last_read) to "LAST_READ",
        )

        options.forEach { (label, key) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedBaseSort = key }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (selectedBaseSort == key) {
                        Icons.Rounded.RadioButtonChecked
                    } else {
                        Icons.Rounded.RadioButtonUnchecked
                    },
                    contentDescription = null,
                    tint = if (selectedBaseSort == key) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.order),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        Row(
            Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.medium,
                ).height(56.dp)
                .padding(all = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ToggleButton(
                checked = isAscending,
                onCheckedChange = { isAscending = true },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .semantics { role = Role.RadioButton },
                shapes = ToggleButtonDefaults.shapes(MaterialTheme.shapes.medium),
                border = null,
                colors = ToggleButtonDefaults.toggleButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    checkedContainerColor = MaterialTheme.colorScheme.onSurface,
                    checkedContentColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowUpward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.ascending))
                }
            }

            ToggleButton(
                checked = !isAscending,
                onCheckedChange = { isAscending = false },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .semantics { role = Role.RadioButton },
                shapes = ToggleButtonDefaults.shapes(MaterialTheme.shapes.medium),
                border = null,
                colors = ToggleButtonDefaults.toggleButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    checkedContainerColor = MaterialTheme.colorScheme.onSurface,
                    checkedContentColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.descending))
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1C1B1F),
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = stringResource(R.string.done_action),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
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
                // Drag handle simulation
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
                    onDone = {},
                )
            }
        }
    }
}
