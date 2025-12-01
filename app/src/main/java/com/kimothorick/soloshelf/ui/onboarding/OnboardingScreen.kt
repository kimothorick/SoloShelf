package com.kimothorick.soloshelf.ui.onboarding

import android.content.res.Configuration
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

enum class PermissionState {
    IDLE,
    GRANTED,
    DENIED,
    DENIED_FOREVER,
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    nextScreen: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
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
fun NotificationPermissionScreen(
    onGrantPermissionClicked: () -> Unit,
    permissionState: PermissionState,
    onSkipClicked: () -> Unit,
    onBackClicked: () -> Unit,
) {
    val permissionStateMessage = when (permissionState) {
        PermissionState.IDLE -> R.string.grant_permission
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
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.onboarding_app_showcase_cropped),
                    contentDescription = stringResource(R.string.app_icon),
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
            Text(
                text = stringResource(R.string.onboarding_notification_title),
                style = MaterialTheme.typography.headlineLargeEmphasized,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.onboarding_notification_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onGrantPermissionClicked,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                ),
            ) {
                Text(stringResource(permissionStateMessage))
            }
        }

        Column {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onBackClicked,
                ) {
                    Text(stringResource(R.string.back))
                }
                Spacer(
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = onSkipClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                    ),
                ) {
                    Text(stringResource(R.string.skip))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showSystemUi = true, device = "spec:orientation=landscape,width=411dp,height=891dp")
@Composable
fun WelcomeScreenPreview() {
    SoloShelfTheme {
        Surface {
            WelcomeScreen(nextScreen = {})
        }
    }
}

@Composable
fun NotificationPermissionScreenPreview(
    permissionState: PermissionState,
) {
    SoloShelfTheme {
        Surface {
            NotificationPermissionScreen(
                onGrantPermissionClicked = {},
                permissionState = permissionState,
                onSkipClicked = {},
                onBackClicked = {},
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, showSystemUi = true, device = "spec:orientation=landscape,width=411dp,height=891dp")
@Composable
fun NotificationPermissionsScreenIdlePreview() {
    NotificationPermissionScreenPreview(
        permissionState = PermissionState.IDLE,
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, showSystemUi = true, device = "spec:orientation=landscape,width=411dp,height=891dp")
@Composable
fun NotificationPermissionsScreenGrantedPreview() {
    NotificationPermissionScreenPreview(
        permissionState = PermissionState.GRANTED,
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, showSystemUi = true, device = "spec:orientation=landscape,width=411dp,height=891dp")
@Composable
fun NotificationPermissionsScreenDeniedPreview() {
    NotificationPermissionScreenPreview(
        permissionState = PermissionState.DENIED,
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, showSystemUi = true, device = "spec:orientation=landscape,width=411dp,height=891dp")
@Composable
fun NotificationPermissionsScreenDeniedForeverPreview() {
    NotificationPermissionScreenPreview(
        permissionState = PermissionState.DENIED_FOREVER,
    )
}
