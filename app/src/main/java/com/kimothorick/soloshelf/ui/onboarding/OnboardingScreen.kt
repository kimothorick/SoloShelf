package com.kimothorick.soloshelf.ui.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.theme.extendedColors
import kotlinx.coroutines.launch

enum class PermissionState {
    IDLE,
    GRANTED,
    DENIED,
    DENIED_FOREVER,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit,
    onGrantNotificationPermissionClicked: () -> Unit,
    notificationPermissionState: PermissionState,
    onGrantAudioPermissionClicked: () -> Unit,
    audioPermissionState: PermissionState,
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            when (page) {
                0 -> WelcomeScreen(nextScreen = { scope.launch { pagerState.animateScrollToPage(1) } })

                1 -> PermissionScreenContent(
                    title = R.string.onboarding_notification_title,
                    description = R.string.onboarding_notification_description,
                    grantPermissionButtonText = R.string.grant_permission,
                    onGrantPermissionClicked = onGrantNotificationPermissionClicked,
                    permissionState = notificationPermissionState,
                    imageDrawable = R.drawable.onboarding_app_showcase_cropped,
                )

                2 -> PermissionScreenContent(
                    title = R.string.onboarding_audio_permission_title,
                    description = R.string.onboarding_audio_permission_description,
                    grantPermissionButtonText = R.string.grant_audio_permission,
                    onGrantPermissionClicked = onGrantAudioPermissionClicked,
                    permissionState = audioPermissionState,
                    imageDrawable = R.drawable.onboarding_app_showcase_cropped,
                )
            }
        }

        val currentPage = pagerState.currentPage

        if (currentPage != 0) {
            OnboardingBottomBar(
                onNextClicked = {
                    if (currentPage < pagerState.pageCount - 1) {
                        scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
                    } else {
                        onOnboardingFinished()
                    }
                },
                nextButtonText = when (currentPage) {
                    1 -> R.string.next
                    else -> R.string.finish
                },
                onBackClicked = if (currentPage > 0) {
                    {
                        scope.launch { pagerState.animateScrollToPage(currentPage - 1) }
                    }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
private fun OnboardingBottomBar(
    onNextClicked: () -> Unit,
    @StringRes nextButtonText: Int,
    onBackClicked: (() -> Unit)? = null,
) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBackClicked != null) {
                OutlinedButton(
                    onClick = onBackClicked,
                ) {
                    Text(stringResource(R.string.back))
                }
            }
            Spacer(
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = onNextClicked,
                colors = if (nextButtonText == R.string.finish) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                    )
                } else {
                    ButtonDefaults.buttonColors()
                },
            ) {
                Text(stringResource(nextButtonText))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    nextScreen: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryFixed),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.book_icon),
                    contentDescription = stringResource(R.string.app_icon),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.welcome_to_soloshelf),
                style = MaterialTheme.typography.headlineLargeEmphasized,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.soloshelf_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
        Button(
            onClick = nextScreen,
            modifier = Modifier
                .navigationBarsPadding()
                .padding(end = 16.dp),
        ) {
            Text(stringResource(R.string.get_started))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PermissionScreenContent(
    @StringRes title: Int,
    @StringRes description: Int,
    @StringRes grantPermissionButtonText: Int,
    onGrantPermissionClicked: () -> Unit,
    permissionState: PermissionState,
    modifier: Modifier = Modifier,
    @DrawableRes imageDrawable: Int? = null,
) {
    val permissionStateMessage = when (permissionState) {
        PermissionState.IDLE -> grantPermissionButtonText
        PermissionState.GRANTED -> R.string.grant_permission_success
        PermissionState.DENIED -> R.string.grant_permission_denied
        PermissionState.DENIED_FOREVER -> R.string.grant_permission_denied_forever
    }
    val (containerColor, contentColor) = when (permissionState) {
        PermissionState.IDLE -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        PermissionState.GRANTED -> extendedColors.successColor to Color.White
        PermissionState.DENIED -> extendedColors.errorColor to Color.White
        PermissionState.DENIED_FOREVER -> extendedColors.errorColor to Color.White
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (imageDrawable != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = imageDrawable),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(0.6f),
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface,
                                ),
                            ),
                        ),
                )
            }
        }
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.headlineLargeEmphasized,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onGrantPermissionClicked,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
        ) {
            Text(stringResource(permissionStateMessage))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun WelcomeScreenPreview() {
    SoloShelfTheme {
        Surface {
            Column(modifier = Modifier.fillMaxSize()) {
                WelcomeScreen(modifier = Modifier.weight(1f), nextScreen = {})
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NotificationPermissionsScreenIdlePreview() {
    SoloShelfTheme {
        Surface {
            Column(modifier = Modifier.fillMaxSize()) {
                PermissionScreenContent(
                    title = R.string.onboarding_notification_title,
                    description = R.string.onboarding_notification_description,
                    grantPermissionButtonText = R.string.grant_permission,
                    onGrantPermissionClicked = {},
                    permissionState = PermissionState.IDLE,
                    imageDrawable = R.drawable.onboarding_app_showcase_cropped,
                    modifier = Modifier.weight(1f),
                )
                OnboardingBottomBar(onNextClicked = {}, nextButtonText = R.string.next, onBackClicked = {})
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AudioPermissionsScreenIdlePreview() {
    SoloShelfTheme {
        Surface {
            Column(modifier = Modifier.fillMaxSize()) {
                PermissionScreenContent(
                    title = R.string.onboarding_audio_permission_title,
                    description = R.string.onboarding_audio_permission_description,
                    grantPermissionButtonText = R.string.grant_audio_permission,
                    onGrantPermissionClicked = {},
                    permissionState = PermissionState.IDLE,
                    imageDrawable = R.drawable.onboarding_app_showcase_cropped,
                    modifier = Modifier.weight(1f),
                )
                OnboardingBottomBar(onNextClicked = {}, nextButtonText = R.string.finish, onBackClicked = {})
            }
        }
    }
}
