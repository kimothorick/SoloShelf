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
    fun onboardingFlow_completesSuccessfully() {
        var onOnboardingFinishedCalled = false

        composeTestRule.setContent {
            SoloShelfTheme {
                OnboardingScreen(
                    onOnboardingFinished = { onOnboardingFinishedCalled = true },
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.welcome_to_soloshelf)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.get_started)).performClick()

        assertTrue(onOnboardingFinishedCalled)
    }
}
