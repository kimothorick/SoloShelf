package com.kimothorick.soloshelf.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.ThemePreviews

@Composable
fun CircularIconButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    contentColor: Color? = null,
    onClick: () -> Unit,
) {
    OutlinedIconButton(
        onClick = onClick,
        modifier = modifier.requiredSize(40.dp),
        enabled = enabled,
        shape = CircleShape,
        colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = backgroundColor ?: MaterialTheme.colorScheme.surface,
            contentColor = contentColor ?: MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) {
                borderColor ?: MaterialTheme.colorScheme.outlineVariant
            } else {
                (borderColor ?: MaterialTheme.colorScheme.outlineVariant).copy(alpha = 0.12f)
            },
        ),
    ) {
        Icon(
            modifier = Modifier.requiredSize(20.dp),
            imageVector = icon,
            contentDescription = label,
        )
    }
}

@ThemePreviews
@Composable
private fun CircularIconButtonPreview() {
    SoloShelfTheme {
        Surface {
            Row(modifier = Modifier.padding(16.dp)) {
                CircularIconButton(
                    icon = Icons.Rounded.Settings,
                    label = "Settings",
                    onClick = {},
                )
            }
        }
    }
}
