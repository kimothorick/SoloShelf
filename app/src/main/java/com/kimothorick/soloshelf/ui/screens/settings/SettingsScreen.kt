package com.kimothorick.soloshelf.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.graphics.shapes.RoundedPolygon
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.ui.components.DeveloperCreditsBottomSheet
import com.kimothorick.soloshelf.ui.components.SoloShelfSegmentedListItem
import com.kimothorick.soloshelf.ui.components.SoloShelfTopAppBar
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.ComponentPreviews
import com.kimothorick.soloshelf.ui.tooling.DevicePreviews
import com.kimothorick.soloshelf.util.Constants

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToLibraryStorage: () -> Unit,
    onNavigateToLicenses: () -> Unit,
) {
    SettingsScreenContent(
        modifier = modifier,
        onBack = onBack,
        onNavigateToAppearance = onNavigateToAppearance,
        onNavigateToLibraryStorage = onNavigateToLibraryStorage,
        onNavigateToLicenses = onNavigateToLicenses,
    )
}

@Composable
fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToLibraryStorage: () -> Unit,
    onNavigateToLicenses: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var isDeveloperCreditsVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SoloShelfTopAppBar(
                title = stringResource(R.string.settings_title),
                onBackClick = onBack,
                scrollBehavior = scrollBehavior,
            )
        },
        containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        val context = LocalContext.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            generalSection(
                onNavigateToAppearance = onNavigateToAppearance,
                onNavigateToLibraryStorage = onNavigateToLibraryStorage,
            )
            otherSection(
                onNavigateToLicenses,
            )
            aboutAppSection(
                onGitHubLinkClicked = {
                    val intent = Intent(Intent.ACTION_VIEW, Constants.SOLOSHELF_GITHUB_URL.toUri())
                    context.startActivity(intent)
                },
                onReportBugClicked = {
                    val intent = Intent(Intent.ACTION_VIEW, Constants.SOLOSHELF_GITHUB_ISSUES_URL.toUri())
                    context.startActivity(intent)
                },
                onNavigateToDeveloperCredits = { isDeveloperCreditsVisible = true },
                onSupportDevelopmentClicked = {
                    val intent = Intent(Intent.ACTION_VIEW, Constants.DONATION_URL.toUri())
                    context.startActivity(intent)
                },
            )

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (isDeveloperCreditsVisible) {
            DeveloperCreditsBottomSheet(
                onDismiss = { isDeveloperCreditsVisible = false },
            )
        }
    }
}

private fun LazyListScope.generalSection(
    onNavigateToAppearance: () -> Unit,
    onNavigateToLibraryStorage: () -> Unit,
) {
    item {
        SettingsSectionHeader(title = stringResource(R.string.settings_general))
    }

    item {
        Column(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            modifier = Modifier.clip(
                shape = MaterialTheme.shapes.extraLarge,
            ),
        ) {
            SoloShelfSegmentedListItem(
                onClick = onNavigateToAppearance,
                shapes = ListItemDefaults.segmentedShapes(0, 3),
                leadingContent = { Icon(painterResource(R.drawable.ic_palette), contentDescription = null) },
                headlineText = stringResource(R.string.appearance),
                supportingText = stringResource(R.string.appearance_desc),
            )
            SoloShelfSegmentedListItem(
                onClick = { /* TODO: Language Selection */ },
                shapes = ListItemDefaults.segmentedShapes(1, 3),
                leadingContent = { Icon(painterResource(R.drawable.ic_language), contentDescription = null) },
                headlineText = stringResource(R.string.language),
                supportingText = stringResource(R.string.language_english),
            )
            SoloShelfSegmentedListItem(
                onClick = onNavigateToLibraryStorage,
                shapes = ListItemDefaults.segmentedShapes(2, 3),
                leadingContent = { Icon(painterResource(R.drawable.ic_folder), contentDescription = null) },
                headlineText = stringResource(R.string.library_storage),
                supportingText = stringResource(R.string.library_storage_desc),
            )
        }
    }
}

private fun LazyListScope.otherSection(
    onNavigateToLicenses: () -> Unit,
) {
    item {
        SettingsSectionHeader(title = stringResource(R.string.settings_other))
    }

    item {
        Column(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            modifier = Modifier.clip(
                shape = MaterialTheme.shapes.extraLarge,
            ),
        ) {
            SoloShelfSegmentedListItem(
                onClick = { /* TODO: Privacy Policy */ },
                shapes = ListItemDefaults.segmentedShapes(0, 2),
                leadingContent = { Icon(painterResource(R.drawable.ic_privacy_tip), contentDescription = null) },
                headlineText = stringResource(R.string.privacy_policy),
                supportingText = "",
            )
            SoloShelfSegmentedListItem(
                onClick = onNavigateToLicenses,
                shapes = ListItemDefaults.segmentedShapes(1, 2),
                leadingContent = { Icon(painterResource(R.drawable.ic_globe), contentDescription = null) },
                headlineText = stringResource(R.string.licenses),
                supportingText = stringResource(R.string.open_source_licenses),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.aboutAppSection(
    onGitHubLinkClicked: () -> Unit,
    onReportBugClicked: () -> Unit,
    onNavigateToDeveloperCredits: () -> Unit,
    onSupportDevelopmentClicked: () -> Unit,
) {
    item {
        SettingsSectionHeader(title = stringResource(R.string.about_app))
    }

    item {
        Surface(
            color = SoloShelfTheme.colorScheme.surfaceContainerHighest,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AboutAppGridButton(
                        title = stringResource(R.string.github),
                        iconRes = R.drawable.ic_github,
                        onClick = onGitHubLinkClicked,
                        modifier = Modifier.weight(1f),
                        shape = MaterialShapes.Cookie4Sided,
                    )
                    AboutAppGridButton(
                        title = stringResource(R.string.rate_app),
                        iconRes = R.drawable.ic_thumb_up,
                        onClick = { /* TODO: rate app */ },
                        modifier = Modifier.weight(1f),
                        shape = MaterialShapes.Pentagon,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AboutAppGridButton(
                        title = stringResource(R.string.feedback),
                        iconRes = R.drawable.ic_bug_report,
                        onClick = onReportBugClicked,
                        modifier = Modifier.weight(1f),
                        shape = MaterialShapes.Arch,
                    )
                    AboutAppGridButton(
                        title = stringResource(R.string.developer_credits),
                        iconRes = R.drawable.ic_info,
                        onClick = onNavigateToDeveloperCredits,
                        modifier = Modifier.weight(1f),
                        shape = MaterialShapes.Clover8Leaf,
                    )
                }

                Text(
                    text = stringResource(R.string.open_source_note),
                    style = SoloShelfTheme.typography.bodySmall,
                    color = SoloShelfTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    item {
        Spacer(modifier = Modifier.height(8.dp))
        SupportDevelopmentBanner(
            onClick = {
                onSupportDevelopmentClicked()
            },
        )
    }

    item {
        Spacer(modifier = Modifier.height(16.dp))
        AppVersionFooter()
    }
}

@Composable
private fun AppVersionFooter(
    showIcon: Boolean? = false,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showIcon == true) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SoloShelfTheme.colorScheme.onSurface),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        Text(
            text = stringResource(R.string.app_name),
            style = SoloShelfTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = SoloShelfTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "v${com.kimothorick.soloshelf.BuildConfig.VERSION_NAME}",
            style = SoloShelfTheme.typography.bodyMedium,
            color = SoloShelfTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AboutAppGridButton(
    title: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedPolygon? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                shape = MaterialTheme.shapes.extraLarge,
            )
            .background(color = SoloShelfTheme.colorScheme.surfaceContainer)
            .clickable(
                onClick = onClick,
            )
            .padding(vertical = 10.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(48.dp)
                .clip(shape = shape?.toShape() ?: CircleShape)
                .background(SoloShelfTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = SoloShelfTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = SoloShelfTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = SoloShelfTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

@Composable
fun SettingsSectionHeader(
    title: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = SoloShelfTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp, top = 24.dp, bottom = 8.dp),
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SupportDevelopmentBanner(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                shape = MaterialTheme.shapes.extraLarge,
            )
            .background(color = SoloShelfTheme.colorScheme.surfaceContainerHighest)
            .clickable(
                onClick = onClick,
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = modifier
                .size(48.dp)
                .background(
                    color = Color(0xFFFFDD00),
                    shape = MaterialShapes.Pill.toShape(),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_bmc_logo),
                contentDescription = null,
                tint = Color.White,
                modifier = modifier.height(24.dp),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Support development",
                style = SoloShelfTheme.typography.titleMedium,
                color = SoloShelfTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                text = "Donations keeps the caffeine flowing and app free",
                style = SoloShelfTheme.typography.bodyMedium,
                color = SoloShelfTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }
    }
}

@ComponentPreviews
@Composable
fun SupportDevelopmentBannerPreview() {
    SoloShelfTheme {
        SupportDevelopmentBanner()
    }
}

@DevicePreviews
@Composable
fun SettingsScreenPreview() {
    SoloShelfTheme {
        SettingsScreenContent(
            onBack = {},
            onNavigateToAppearance = {},
            onNavigateToLibraryStorage = {},
            onNavigateToLicenses = {},
        )
    }
}
