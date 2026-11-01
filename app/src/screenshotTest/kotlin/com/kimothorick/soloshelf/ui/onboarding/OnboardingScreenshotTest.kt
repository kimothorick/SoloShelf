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
            )
        }
    }
}
