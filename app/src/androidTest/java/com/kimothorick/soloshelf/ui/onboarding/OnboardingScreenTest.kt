package com.kimothorick.soloshelf.ui.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun onboardingFlow_progressesThroughAllScreens() {
        var onOnboardingFinishedCalled = false

        composeTestRule.setContent {
            SoloShelfTheme {
                OnboardingScreen(
                    onOnboardingFinished = { onOnboardingFinishedCalled = true },
                    onGrantNotificationPermissionClicked = {},
                    notificationPermissionState = PermissionState.IDLE,
                    onGrantAudioPermissionClicked = {},
                    audioPermissionState = PermissionState.IDLE,
                )
            }
        }

        // 1. Welcome Screen
        composeTestRule.onNodeWithText(context.getString(R.string.welcome_to_soloshelf)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.get_started)).performClick()

        // 2. Notification Permission Screen
        composeTestRule.onNodeWithText(context.getString(R.string.onboarding_notification_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.next)).performClick()

        // 3. Audio Permission Screen
        composeTestRule.onNodeWithText(context.getString(R.string.onboarding_audio_permission_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.finish)).performClick()

        // 4. Verify onboarding finished callback is called
        assertTrue(onOnboardingFinishedCalled)
    }

    @Test
    fun notificationPermissionGranted_showsSuccessMessage() {
        composeTestRule.setContent {
            SoloShelfTheme {
                OnboardingScreen(
                    onOnboardingFinished = {}, 
                    onGrantNotificationPermissionClicked = {},
                    notificationPermissionState = PermissionState.GRANTED,
                    onGrantAudioPermissionClicked = {},
                    audioPermissionState = PermissionState.IDLE,
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.get_started)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.grant_permission_success)).assertIsDisplayed()
    }

    @Test
    fun notificationPermissionDenied_showsDeniedMessage() {
        composeTestRule.setContent {
            SoloShelfTheme {
                OnboardingScreen(
                    onOnboardingFinished = {},
                    onGrantNotificationPermissionClicked = {},
                    notificationPermissionState = PermissionState.DENIED,
                    onGrantAudioPermissionClicked = {},
                    audioPermissionState = PermissionState.IDLE,
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.get_started)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.grant_permission_denied)).assertIsDisplayed()
    }

    @Test
    fun notificationPermissionDeniedForever_showsDeniedForeverMessage() {
        composeTestRule.setContent {
            SoloShelfTheme {
                OnboardingScreen(
                    onOnboardingFinished = {},
                    onGrantNotificationPermissionClicked = {},
                    notificationPermissionState = PermissionState.DENIED_FOREVER,
                    onGrantAudioPermissionClicked = {},
                    audioPermissionState = PermissionState.IDLE,
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.get_started)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.grant_permission_denied_forever))
            .assertIsDisplayed()
    }

    @Test
    fun audioPermissionGranted_showsSuccessMessage() {
        composeTestRule.setContent {
            SoloShelfTheme {
                OnboardingScreen(
                    onOnboardingFinished = {},
                    onGrantNotificationPermissionClicked = {},
                    notificationPermissionState = PermissionState.IDLE,
                    onGrantAudioPermissionClicked = {},
                    audioPermissionState = PermissionState.GRANTED,
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.get_started)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.next)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.grant_permission_success)).assertIsDisplayed()
    }

    @Test
    fun audioPermissionDenied_showsDeniedMessage() {
        composeTestRule.setContent {
            SoloShelfTheme {
                OnboardingScreen(
                    onOnboardingFinished = {},
                    onGrantNotificationPermissionClicked = {},
                    notificationPermissionState = PermissionState.IDLE,
                    onGrantAudioPermissionClicked = {},
                    audioPermissionState = PermissionState.DENIED,
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.get_started)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.next)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.grant_permission_denied)).assertIsDisplayed()
    }

    @Test
    fun audioPermissionDeniedForever_showsDeniedForeverMessage() {
        composeTestRule.setContent {
            SoloShelfTheme {
                OnboardingScreen(
                    onOnboardingFinished = {},
                    onGrantNotificationPermissionClicked = {},
                    notificationPermissionState = PermissionState.IDLE,
                    onGrantAudioPermissionClicked = {},
                    audioPermissionState = PermissionState.DENIED_FOREVER,
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.get_started)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.next)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.grant_permission_denied_forever))
            .assertIsDisplayed()
    }

    @Test
    fun grantNotificationPermissionButton_isClickable() {
        var onGrantNotificationPermissionClickedCalled = false

        composeTestRule.setContent {
            SoloShelfTheme {
                OnboardingScreen(
                    onOnboardingFinished = {},
                    onGrantNotificationPermissionClicked = { onGrantNotificationPermissionClickedCalled = true },
                    notificationPermissionState = PermissionState.IDLE,
                    onGrantAudioPermissionClicked = {},
                    audioPermissionState = PermissionState.IDLE,
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.get_started)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.grant_permission)).performClick()

        assertTrue(onGrantNotificationPermissionClickedCalled)
    }

    @Test
    fun grantAudioPermissionButton_isClickable() {
        var onGrantAudioPermissionClickedCalled = false

        composeTestRule.setContent {
            SoloShelfTheme {
                OnboardingScreen(
                    onOnboardingFinished = {},
                    onGrantNotificationPermissionClicked = {},
                    notificationPermissionState = PermissionState.IDLE,
                    onGrantAudioPermissionClicked = { onGrantAudioPermissionClickedCalled = true },
                    audioPermissionState = PermissionState.IDLE,
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.get_started)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.next)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.grant_audio_permission)).performClick()

        assertTrue(onGrantAudioPermissionClickedCalled)
    }
}
