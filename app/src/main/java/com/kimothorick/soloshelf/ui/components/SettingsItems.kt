package com.kimothorick.soloshelf.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.style.styleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kimothorick.soloshelf.ui.theme.SoloShelfStyles
import com.kimothorick.soloshelf.ui.theme.SoloShelfTextStyles
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.ComponentPreviews

@Composable
fun SoloShelfSettingsItem(
    headline: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    containerColor: Color = Color.Transparent,
    contentPadding: PaddingValues = PaddingValues(horizontal = 0.dp, vertical = 12.dp),
    shape: Shape = MaterialTheme.shapes.extraLarge,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        color = containerColor,
        shape = shape,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingContent != null) {
                leadingContent()
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = headline,
                    style = SoloShelfTheme.typography.titleMedium,
                    color = SoloShelfTheme.colorScheme.onSurface,
                )
                if (supportingText != null) {
                    Text(
                        text = supportingText,
                        style = SoloShelfTheme.typography.bodyMedium,
                        color = SoloShelfTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(16.dp))
                trailingContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SoloShelfSegmentedListItem(
    onClick: () -> Unit,
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
    leadingContent: @Composable (() -> Unit)? = null,
    headlineText: String,
    supportingText: String,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    val supportingContent: @Composable (() -> Unit)? = if (supportingText.isNotBlank()) {
        {
            Text(
                text = supportingText,
                style = SoloShelfTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = SoloShelfTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            )
        }
    } else {
        null
    }

    SegmentedListItem(
        onClick = onClick,
        shapes = shapes,
        colors = ListItemDefaults.segmentedColors(
            containerColor = SoloShelfTheme.colorScheme.surfaceContainerHighest,
        ),
        leadingContent = leadingContent,
        supportingContent = supportingContent,
        trailingContent = trailingContent,
        modifier = modifier.styleable(style = SoloShelfStyles.segmentedListItem),
    ) {
        Text(
            text = headlineText,
            style = SoloShelfTextStyles.segmentedListItemHeaderText,
            color = SoloShelfTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SoloShelfSettingsItemPreview() {
    SoloShelfTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SoloShelfSettingsItem(
                headline = "Simple Item",
                supportingText = "Just text, no icons or controls",
            )

            SoloShelfSettingsItem(
                headline = "Item with Icon",
                supportingText = "Includes a leading icon",
                leadingContent = {
                    Icon(imageVector = Icons.Rounded.Info, contentDescription = null)
                },
            )

            SoloShelfSettingsItem(
                headline = "Item with Switch",
                supportingText = "Includes a trailing toggle",
                trailingContent = {
                    Switch(
                        true,
                    ) { }
                },
            )

            SoloShelfSettingsItem(
                headline = "Full Item",
                supportingText = "Leading icon, headline, supporting text, and trailing control",
                leadingContent = {
                    Icon(imageVector = Icons.Rounded.Info, contentDescription = null)
                },
                trailingContent = {
                    Switch(
                        false,
                    ) { }
                },
            )

            SoloShelfSettingsItem(
                headline = "Container Color",
                supportingText = "Custom background color applied",
                containerColor = SoloShelfTheme.colorScheme.surfaceContainerLow,
                contentPadding = PaddingValues(16.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@ComponentPreviews
@Composable
private fun SoloShelfSegmentedListItemPreview() {
    SoloShelfTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .clip(
                    MaterialTheme.shapes.extraLarge,
                ),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        ) {
            SoloShelfSegmentedListItem(
                headlineText = "Top Item",
                supportingText = "First item in a group",
                shapes = ListItemDefaults.segmentedShapes(0, 3),
                onClick = {},
            )

            SoloShelfSegmentedListItem(
                headlineText = "Middle Item",
                supportingText = "Item with an icon",
                leadingContent = {
                    Icon(
                        painterResource(com.kimothorick.soloshelf.R.drawable.ic_info),
                        contentDescription = null,
                    )
                },
                shapes = ListItemDefaults.segmentedShapes(1, 3),
                onClick = {},
            )

            SoloShelfSegmentedListItem(
                headlineText = "Bottom Item",
                supportingText = "Includes a trailing control",
                shapes = ListItemDefaults.segmentedShapes(2, 3),
                trailingContent = {
                    Switch(
                        checked = true,
                        onCheckedChange = {},
                    )
                },
                onClick = {},
            )

            SoloShelfSegmentedListItem(
                headlineText = "Single Item",
                supportingText = "A standalone segmented item",
                shapes = ListItemDefaults.segmentedShapes(0, 1),
                onClick = {},
            )

            SoloShelfSegmentedListItem(
                headlineText = "Headline Only",
                supportingText = "",
                shapes = ListItemDefaults.segmentedShapes(0, 1),
                onClick = {},
            )
        }
    }
}
