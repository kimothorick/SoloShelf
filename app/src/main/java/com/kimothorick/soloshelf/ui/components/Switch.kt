package com.kimothorick.soloshelf.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.ComponentPreviews
import androidx.compose.material3.Switch as M3Switch

@Composable
fun Switch(
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
) {
    M3Switch(
        modifier = modifier,
        checked = checked,
        onCheckedChange = onCheckedChange,
        thumbContent = {
            val icon = if (checked) Icons.Rounded.Check else Icons.Rounded.Close
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            )
        },
    )
}

@ComponentPreviews
@Composable
private fun SwitchPreview() {
    SoloShelfTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                Switch(
                    checked = true,
                    onCheckedChange = {},
                )
                Spacer(modifier = Modifier.height(8.dp))
                Switch(
                    checked = false,
                    onCheckedChange = {},
                )
            }
        }
    }
}
