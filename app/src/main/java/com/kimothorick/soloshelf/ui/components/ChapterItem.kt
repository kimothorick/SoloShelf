package com.kimothorick.soloshelf.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.ComponentPreviews
import com.kimothorick.soloshelf.ui.tooling.PreviewData
import org.readium.r2.shared.publication.Link

@Composable
fun ChapterItem(
    modifier: Modifier = Modifier,
    link: Link,
    depth: Int = 0,
    onClick: (Link) -> Unit = {},
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val hasChildren = link.children.isNotEmpty()

    Column(modifier = modifier) {
        ListItem(
            headlineContent = {
                Text(
                    text = link.title ?: "Untitled Chapter",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = (depth * 16).dp),
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.clickable {
                onClick(link)
            },
            trailingContent = if (hasChildren) {
                {
                    IconButton(
                        onClick = { expanded = !expanded },
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                        )
                    }
                }
            } else {
                null
            },
        )
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column {
                link.children.forEachIndexed { index, child ->
                    ChapterItem(
                        link = child,
                        depth = depth + 1,
                        onClick = onClick,
                    )
                    if (index != link.children.lastIndex) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = ((depth + 1) * 16 + 16).dp, end = 24.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun ChapterItemPreview() {
    SoloShelfTheme {
        Surface {
            Column {
                ChapterItem(link = PreviewData.sampleLink)
                ChapterItem(link = PreviewData.sampleLinkWithChildren)
            }
        }
    }
}
