package com.kimothorick.soloshelf.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.ui.components.SoloShelfTopAppBar
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.style.m3VariantColors
import com.mikepenz.aboutlibraries.ui.compose.m3.style.m3VariantTextStyles
import com.mikepenz.aboutlibraries.ui.compose.style.ContrastLevel
import com.mikepenz.aboutlibraries.ui.compose.style.LibrariesStyle
import com.mikepenz.aboutlibraries.ui.compose.style.VariantColors
import com.mikepenz.aboutlibraries.ui.compose.style.VariantShapes
import com.mikepenz.aboutlibraries.ui.compose.style.VariantTextStyles
import com.mikepenz.aboutlibraries.ui.compose.style.defaultVariantDimensions
import com.mikepenz.aboutlibraries.ui.compose.style.defaultVariantPadding
import com.mikepenz.aboutlibraries.ui.compose.style.defaultVariantShapes
import com.mikepenz.aboutlibraries.ui.compose.util.author
import com.mikepenz.aboutlibraries.ui.compose.variant.DefaultLibraryBadges
import com.mikepenz.aboutlibraries.ui.compose.variant.LibraryBadges

@Composable
fun LicensesScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val libsState by produceLibraries(R.raw.aboutlibraries)
    val libraries = libsState?.libraries.orEmpty()
    val style = LibrariesStyle(
        colors = LibraryDefaults.m3VariantColors(),
        padding = LibraryDefaults.defaultVariantPadding(),
        dimensions = LibraryDefaults.defaultVariantDimensions(),
        textStyles = LibraryDefaults.m3VariantTextStyles(),
        shapes = LibraryDefaults.defaultVariantShapes(),
    )

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SoloShelfTopAppBar(
                title = stringResource(R.string.licenses_and_libraries),
                onBackClick = onBack,
                scrollBehavior = scrollBehavior,
            )
        },
        containerColor = SoloShelfTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(color = SoloShelfTheme.colorScheme.surfaceContainer)
                .padding(innerPadding),
        ) {
            items(libraries) { library ->
                LibraryRow(
                    library = library,
                    style = style,
                )
                Spacer(modifier = Modifier.height(2.dp))
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun LibraryRow(
    library: Library,
    badges: LibraryBadges = DefaultLibraryBadges,
    style: LibrariesStyle,
) {
    val colors = style.colors
    val licenses = library.licenses
    val firstLicense = remember(licenses) { licenses.firstOrNull() }
    val licenseHue = remember(licenses, colors.licenseHueResolver) {
        firstLicense?.spdxId?.let { colors.licenseHueResolver.colorFor(it) }
            ?: firstLicense?.name?.let { colors.licenseHueResolver.colorFor(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {})
            .background(
                color = SoloShelfTheme.colorScheme.surfaceContainer,
            ).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    library.name,
                    style = SoloShelfTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                    color = SoloShelfTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val version = library.artifactVersion
                if (badges.version && !version.isNullOrBlank()) {
                    Text(
                        version,
                        style = SoloShelfTheme.typography.bodySmall,
                        color = SoloShelfTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            val authorText = library.author.takeIf { it.isNotBlank() && badges.author }

            if (authorText != null) {
                Text(
                    authorText,
                    style = SoloShelfTheme.typography.bodySmall,
                    color = SoloShelfTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            val description = library.description?.takeIf { it.isNotBlank() && badges.description }
            if (description != null) {
                Text(
                    description,
                    style = SoloShelfTheme.typography.bodySmall,
                    color = SoloShelfTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (badges.license && library.licenses.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                library.licenses.forEach { license ->
                    val hue = license.spdxId?.let { colors.licenseHueResolver.colorFor(it) }
                        ?: license.name.let { colors.licenseHueResolver.colorFor(it) }
                        ?: licenseHue
                    LicenseBadge(
                        text = license.name,
                        hue = hue,
                        colors = colors,
                        shapes = style.shapes,
                        textStyles = style.textStyles,
                    )
                }
            }
        }
    }
}

@Composable
fun LicenseBadge(
    text: String,
    hue: Color?,
    colors: VariantColors,
    shapes: VariantShapes,
    textStyles: VariantTextStyles,
) {
    val onBg = colors.rowOnBackground.let { if (it == Color.Unspecified) SoloShelfTheme.colorScheme.onSurface else it }
    val badgeAlpha = if (colors.contrastLevel == ContrastLevel.High) 0.22f else 0.15f
    val fallbackContainer = (
        hue ?: (
            colors.actionFilledContainer.takeIf { it != Color.Unspecified }
                ?: SoloShelfTheme.colorScheme.primary
        )
    ).copy(alpha = badgeAlpha)
    val fallbackContent = hue ?: (colors.actionFilledContent.takeIf { it != Color.Unspecified } ?: onBg)

    val container =
        if (colors.licenseBadgeContainer == Color.Unspecified) fallbackContainer else colors.licenseBadgeContainer
    val content = if (colors.licenseBadgeContent == Color.Unspecified) fallbackContent else colors.licenseBadgeContent

    Box(
        modifier = Modifier
            .clip(shapes.licenseTokenShape)
            .background(container)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = textStyles.licenseTextStyle.copy(color = content),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
