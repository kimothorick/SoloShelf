package com.kimothorick.soloshelf.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.ComponentPreviews
import com.kimothorick.soloshelf.ui.tooling.ThemePreviews
import com.skydoves.compose.stability.runtime.TraceRecomposition

@Composable
fun CircularIconButton(
    icon: Int,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color? = null,
    borderColor: Boolean = false,
    contentColor: Color? = null,
    onClick: () -> Unit,
) {
    OutlinedIconButton(
        onClick = onClick,
        modifier = modifier.requiredSize(40.dp),
        enabled = enabled,
        colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = backgroundColor ?: SoloShelfTheme.colorScheme.surfaceContainerHighest,
            contentColor = contentColor ?: SoloShelfTheme.colorScheme.onSurface,
        ),
        border = if (borderColor) {
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        } else {
            null
        },
    ) {
        Icon(
            modifier = Modifier.requiredSize(20.dp),
            painter = painterResource(icon),
            contentDescription = label,
        )
    }
}

@ComponentPreviews
@Composable
private fun CircularIconButtonPreview(
    borderColor: Boolean = false,
) {
    SoloShelfTheme {
        Surface {
            Row(modifier = Modifier.padding(16.dp)) {
                CircularIconButton(
                    icon = R.drawable.ic_palette,
                    label = "Settings",
                    onClick = {},
                    borderColor = borderColor,
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun CircularIconButtonWithBorderPreview() {
    CircularIconButtonPreview(borderColor = true)
}

@TraceRecomposition
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToggleableButtonGroup(
    items: List<ToggleableButtonItem>,
    onItemSelected: (ToggleableButtonItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = items.indexOfFirst { it.isSelected }.coerceAtLeast(0)
    val selectedItemIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        label = "selected_index_animation",
    )
    val backgroundColor = SoloShelfTheme.colorScheme.inverseSurface
    val outlineColor = SoloShelfTheme.colorScheme.outline
    val containerShape = MaterialTheme.shapes.extraLarge

    Row(
        modifier
            .border(
                width = 1.dp,
                color = outlineColor,
                shape = containerShape,
            ).height(52.dp)
            .padding(all = 4.dp)
            .drawBehind {
                val itemCount = items.size
                val itemSpacingPx = 4.dp.toPx()
                val totalSpacing = itemSpacingPx * (itemCount - 1)
                val itemWidth = (size.width - totalSpacing) / itemCount

                drawRoundRect(
                    color = backgroundColor,
                    topLeft = Offset(
                        x = (itemWidth + itemSpacingPx) * selectedItemIndex,
                        y = 0f,
                    ),
                    size = Size(itemWidth, size.height),
                    cornerRadius = CornerRadius(28.dp.toPx()),
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = item.isSelected
            val isPreviousSelected = if (index > 0) items[index - 1].isSelected else false

            if (index > 0 && !isSelected && !isPreviousSelected) {
                VerticalDivider(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .height(24.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }

            ToggleButton(
                checked = isSelected,
                onCheckedChange = { onItemSelected(item) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .semantics { role = Role.RadioButton },
                shapes = ToggleButtonShapes(
                    shape = containerShape,
                    pressedShape = containerShape,
                    checkedShape = containerShape,
                ),
                border = null,
                colors = ToggleButtonDefaults.colors(
                    containerColor = Color.Transparent,
                    checkedContainerColor = Color.Transparent,
                    checkedContentColor = SoloShelfTheme.colorScheme.surfaceContainerHigh,
                    contentColor = SoloShelfTheme.colorScheme.onSurface,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (item.icon != null) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

data class ToggleableButtonItem(
    val label: String,
    val isSelected: Boolean,
    val identifier: Any?,
    val icon: ImageVector? = null,
)

@ThemePreviews
@Composable
private fun ToggleableButtonGroupPreview() {
    SoloShelfTheme {
        Surface {
            var selectedOption by remember { mutableStateOf("Light") }
            val items = listOf("Light", "Dark", "System").map {
                ToggleableButtonItem(
                    label = it,
                    isSelected = it == selectedOption,
                    identifier = it,
                )
            }

            Box(modifier = Modifier.padding(16.dp)) {
                ToggleableButtonGroup(
                    items = items,
                    onItemSelected = { selectedOption = it.identifier as String },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
