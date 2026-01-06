package com.kimothorick.soloshelf.ui.onboarding

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import org.junit.Rule
import org.junit.Test

class OnboardingScreenSnapshotTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material.Light.NoActionBar",
    )

    @Test
    fun welcomeScreen() {
        paparazzi.snapshot {
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
    }

    @Test
    fun notificationPermissionScreen_Idle() {
        paparazzi.snapshot {
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
    }

    @Test
    fun notificationPermissionScreen_Granted() {
        paparazzi.snapshot {
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
    }

    @Test
    fun notificationPermissionScreen_Denied() {
        paparazzi.snapshot {
            SoloShelfTheme {
                OnboardingScreen(
                    onOnboardingFinished = {},
                    onGrantNotificationPermissionClicked = {},
                    notificationPermissionState = PermissionState.DENIED,
                    onGrantAudioPermissionClicked = {},
                    audioPermissionState = PermissionState.IDLE,
                    initialPage = 1,
                )
            }
        }
    }

    @Test
    fun notificationPermissionScreen_DeniedForever() {
        paparazzi.snapshot {
            SoloShelfTheme {
                OnboardingScreen(
                    onOnboardingFinished = {},
                    onGrantNotificationPermissionClicked = {},
                    notificationPermissionState = PermissionState.DENIED_FOREVER,
                    onGrantAudioPermissionClicked = {},
                    audioPermissionState = PermissionState.IDLE,
                    initialPage = 1,
                )
            }
        }
    }

    @Test
    fun audioPermissionScreen_Idle() {
        paparazzi.snapshot {
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

    @Test
    fun audioPermissionScreen_Granted() {
        paparazzi.snapshot {
            SoloShelfTheme {
                OnboardingScreen(
                    onOnboardingFinished = {},
                    onGrantNotificationPermissionClicked = {},
                    notificationPermissionState = PermissionState.IDLE,
                    onGrantAudioPermissionClicked = {},
                    audioPermissionState = PermissionState.GRANTED,
                    initialPage = 2,
                )
            }
        }
    }

    @Test
    fun audioPermissionScreen_Denied() {
        paparazzi.snapshot {
            SoloShelfTheme {
                OnboardingScreen(
                    onOnboardingFinished = {},
                    onGrantNotificationPermissionClicked = {},
                    notificationPermissionState = PermissionState.IDLE,
                    onGrantAudioPermissionClicked = {},
                    audioPermissionState = PermissionState.DENIED,
                    initialPage = 2,
                )
            }
        }
    }

    @Test
    fun audioPermissionScreen_DeniedForever() {
        paparazzi.snapshot {
            SoloShelfTheme {
                OnboardingScreen(
                    onOnboardingFinished = {},
                    onGrantNotificationPermissionClicked = {},
                    notificationPermissionState = PermissionState.IDLE,
                    onGrantAudioPermissionClicked = {},
                    audioPermissionState = PermissionState.DENIED_FOREVER,
                    initialPage = 2,
                )
            }
        }
    }
}
