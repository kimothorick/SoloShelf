package com.kimothorick.soloshelf.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kimothorick.soloshelf.BuildConfig
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.data.preferences.AppTheme
import com.kimothorick.soloshelf.ui.components.CircularIconButton
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.DevicePreviews

@Composable
fun Settings(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themePreference by viewModel.themePreference.collectAsStateWithLifecycle()
    val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle()

    SettingsContent(
        themePreference = themePreference,
        dynamicColor = dynamicColor,
        onBack = onBack,
        onThemeChange = viewModel::setTheme,
        onDynamicColorChange = viewModel::setDynamicColor,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsContent(
    themePreference: AppTheme,
    dynamicColor: Boolean,
    onBack: () -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                titleHorizontalAlignment = Alignment.CenterHorizontally,
                subtitle = { null },
                navigationIcon = {
                    CircularIconButton(
                        modifier = Modifier.padding(start = 12.dp),
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        label = stringResource(R.string.back),
                        onClick = { onBack() },
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SettingsSection(title = stringResource(R.string.appearance)) {
                    ThemeOptionsSettingsItem(
                        selectedTheme = themePreference,
                        setTheme = onThemeChange,
                    )
                    SettingsItem(
                        title = stringResource(R.string.dynamic_color),
                        description = stringResource(R.string.dynamic_color_desc),
                        trailingContent = {
                            Switch(
                                checked = dynamicColor,
                                onCheckedChange = onDynamicColorChange,
                            )
                        },
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.language)) {
                    SettingsItem(
                        title = stringResource(R.string.preferred_language),
                        description = stringResource(R.string.upcoming_feature),
                        onClick = null,
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.support_feedback)) {
                    SettingsItem(
                        title = stringResource(R.string.feedback),
                        description = stringResource(R.string.feedback_desc),
                        onClick = { /* TODO: Implement feedback */ },
                    )
                    SettingsItem(
                        title = stringResource(R.string.rate_app),
                        description = stringResource(R.string.rate_app_desc),
                        onClick = { /* TODO: Implement rate app */ },
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.about_soloshelf)) {
                    SettingsItem(
                        title = stringResource(R.string.privacy_policy),
                        onClick = { /* TODO: Implement privacy policy */ },
                    )
                    SettingsItem(
                        title = stringResource(R.string.app_version),
                        description = "v${BuildConfig.VERSION_NAME}",
                    )
                    SettingsItem(
                        title = stringResource(R.string.developer_credits),
                        description = stringResource(R.string.developer_credits_desc),
                        onClick = { /* TODO: Implement developer credits */ },
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemeOptionsSettingsItem(
    modifier: Modifier = Modifier,
    selectedTheme: AppTheme,
    setTheme: (AppTheme) -> Unit,
) {
    val themeOptions = AppTheme.entries
    val themeOptionsLabels = listOf(
        stringResource(R.string.theme_auto),
        stringResource(R.string.theme_light),
        stringResource(R.string.theme_dark),
    )
    val selectedIndex = themeOptions.indexOf(selectedTheme)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.theme),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(12.dp))
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
            themeOptions.forEachIndexed { index, theme ->
                ToggleButton(
                    checked = selectedIndex == index,
                    onCheckedChange = {
                        setTheme(theme)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics { role = Role.RadioButton },
                    shapes = ToggleButtonDefaults.shapes(MaterialTheme.shapes.medium),
                    border = null,
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Text(themeOptionsLabels[index])
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (trailingContent != null) {
            trailingContent()
        }
    }
}

@DevicePreviews
@Composable
fun SettingsPreview() {
    SoloShelfTheme {
        SettingsContent(
            themePreference = AppTheme.AUTO,
            dynamicColor = true,
            onBack = {},
            onThemeChange = {},
            onDynamicColorChange = {},
        )
    }
}
