package com.kimothorick.soloshelf.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.ThemePreviews
import com.kimothorick.soloshelf.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperCreditsBottomSheet(
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(
            SheetValue.Hidden,
            SheetValue.Expanded,
        ),
        confirmValueChange = { it != SheetValue.PartiallyExpanded },
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
        sheetState = sheetState,
        dragHandle = {
            Spacer(
                Modifier.padding(vertical = 12.dp).size(width = 32.dp, height = 4.dp).background(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape,
                ),
            )
        },
    ) {
        DeveloperCreditsBottomSheetContent(
            onGitHubClick = {
                val intent = Intent(Intent.ACTION_VIEW, Constants.RICK_GITHUB_URL.toUri())
                context.startActivity(intent)
            },
            onInstagramClick = {
                val intent = Intent(Intent.ACTION_VIEW, Constants.RICK_INSTAGRAM_URL.toUri())
                context.startActivity(intent)
            },
            onThreadsClick = {
                val intent = Intent(Intent.ACTION_VIEW, Constants.RICK_THREADS_URL.toUri())
                context.startActivity(intent)
            },
            onTwitterClick = {
                val intent = Intent(Intent.ACTION_VIEW, Constants.RICK_TWITTER_URL.toUri())
                context.startActivity(intent)
            },
            onBuyCoffeeClick = {
                val intent = Intent(Intent.ACTION_VIEW, Constants.DONATION_URL.toUri())
                context.startActivity(intent)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DeveloperCreditsBottomSheetContent(
    onGitHubClick: () -> Unit,
    onInstagramClick: () -> Unit,
    onThreadsClick: () -> Unit,
    onTwitterClick: () -> Unit,
    onBuyCoffeeClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AsyncImage(
                model = R.drawable.rick_dev_pic,
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp).clip(MaterialShapes.Cookie4Sided.toShape()),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.dev_name),
                    style = SoloShelfTheme.typography.headlineSmall,
                    color = SoloShelfTheme.colorScheme.onSurface,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.dev_rank),
                    style = SoloShelfTheme.typography.bodyMedium,
                    color = SoloShelfTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Text(
            text = stringResource(R.string.dev_description),
            style = SoloShelfTheme.typography.bodyMedium,
            color = SoloShelfTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp,
        )

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SocialButton(
                iconRes = R.drawable.ic_github,
                label = stringResource(R.string.github),
                onClick = onGitHubClick,
            )
            SocialButton(
                iconRes = R.drawable.ic_instagram,
                label = stringResource(R.string.instagram),
                onClick = onInstagramClick,
            )
            SocialButton(
                iconRes = R.drawable.ic_threads,
                label = stringResource(R.string.threads),
                onClick = onThreadsClick,
            )
            SocialButton(
                iconRes = R.drawable.ic_twitter,
                label = stringResource(R.string.twitter),
                onClick = onTwitterClick,
            )
        }

        Button(
            onClick = onBuyCoffeeClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_bmc_logo),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = stringResource(R.string.buy_me_a_coffee),
                    style = SoloShelfTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Text(
            text = stringResource(R.string.made_with),
            style = SoloShelfTheme.typography.bodySmallEmphasized,
            color = SoloShelfTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SocialButton(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.clip(MaterialTheme.shapes.medium).clickable(onClick = onClick).padding(8.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            tint = SoloShelfTheme.colorScheme.onSurface,
            modifier = Modifier.size(28.dp),
        )
        Text(
            text = label,
            style = SoloShelfTheme.typography.bodySmall,
            color = SoloShelfTheme.colorScheme.onSurface,
        )
    }
}

@ThemePreviews
@Composable
private fun DeveloperCreditsBottomSheetPreview() {
    SoloShelfTheme {
        Surface {
            DeveloperCreditsBottomSheetContent(
                onGitHubClick = {},
                onInstagramClick = {},
                onThreadsClick = {},
                onTwitterClick = {},
                onBuyCoffeeClick = {},
            )
        }
    }
}
