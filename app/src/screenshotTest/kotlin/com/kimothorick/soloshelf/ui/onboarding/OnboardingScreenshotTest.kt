package com.kimothorick.soloshelf.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme

class OnboardingScreenshotTest {

    @PreviewTest
    @Preview(showSystemUi = true)
    @Composable
    fun WelcomeScreen() {
        SoloShelfTheme {
            OnboardingScreen(
                onOnboardingFinished = {},
                onGrantNotificationPermissionClicked = {},
                notificationPermissionState = PermissionState.IDLE,
                onGrantAudioPermissionClicked = {},
                audioPermissionState = PermissionState.IDLE,
                initialPage = 0,
            )
        }
    }

    @PreviewTest
    @Preview(showSystemUi = true)
    @Composable
    fun NotificationPermissionScreen_Idle() {
        SoloShelfTheme {
            OnboardingScreen(
                onOnboardingFinished = {},
                onGrantNotificationPermissionClicked = {},
                notificationPermissionState = PermissionState.IDLE,
                onGrantAudioPermissionClicked = {},
                audioPermissionState = PermissionState.IDLE,
                initialPage = 1,
            )
        }
    }

    @PreviewTest
    @Preview(showSystemUi = true)
    @Composable
    fun NotificationPermissionScreen_Granted() {
        SoloShelfTheme {
            OnboardingScreen(
                onOnboardingFinished = {},
                onGrantNotificationPermissionClicked = {},
                notificationPermissionState = PermissionState.GRANTED,
                onGrantAudioPermissionClicked = {},
                audioPermissionState = PermissionState.IDLE,
                initialPage = 1,
            )
        }
    }

    @PreviewTest
    @Preview(showSystemUi = true)
    @Composable
    fun AudioPermissionScreen_Idle() {
        SoloShelfTheme {
            OnboardingScreen(
                onOnboardingFinished = {},
                onGrantNotificationPermissionClicked = {},
                notificationPermissionState = PermissionState.IDLE,
                onGrantAudioPermissionClicked = {},
                audioPermissionState = PermissionState.IDLE,
                initialPage = 2,
            )
        }
    }
}
