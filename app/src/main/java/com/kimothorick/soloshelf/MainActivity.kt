package com.kimothorick.soloshelf

import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kimothorick.soloshelf.data.preferences.AppTheme
import com.kimothorick.soloshelf.navigation.NavigationRoot
import com.kimothorick.soloshelf.ui.main.MainViewModel
import com.kimothorick.soloshelf.ui.onboarding.OnboardingScreen
import com.kimothorick.soloshelf.ui.onboarding.OnboardingUiState
import com.kimothorick.soloshelf.ui.onboarding.OnboardingViewModel
import com.kimothorick.soloshelf.ui.screens.LibraryViewModel
import com.kimothorick.soloshelf.ui.screens.settings.SettingsViewModel
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val onboardingViewModel: OnboardingViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val libraryViewModel: LibraryViewModel by viewModels()

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        splashScreen.setKeepOnScreenCondition {
            onboardingViewModel.uiState.value is OnboardingUiState.Loading ||
                !libraryViewModel.isInitialLoadComplete.value
        }

        setContent {
            val themePreference by settingsViewModel.themePreference.collectAsStateWithLifecycle()
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
            val colorTheme by settingsViewModel.colorTheme.collectAsStateWithLifecycle()
            val amoledBlack by settingsViewModel.amoledBlack.collectAsStateWithLifecycle()
            val customSeedColor by settingsViewModel.customSeedColor.collectAsStateWithLifecycle()
            val paletteStyle by settingsViewModel.paletteStyle.collectAsStateWithLifecycle()

            val darkTheme = when (themePreference) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                else -> isSystemInDarkTheme()
            }

            LaunchedEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme },
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
            }

            SoloShelfTheme(
                darkTheme = darkTheme,
                amoledBlack = amoledBlack,
                themeMode = themeMode,
                colorTheme = colorTheme,
                customSeedColor = customSeedColor,
                paletteStyle = paletteStyle,
            ) {
                val uiState by onboardingViewModel.uiState.collectAsStateWithLifecycle()

                when (uiState) {
                    OnboardingUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                        )
                    }

                    OnboardingUiState.ShowOnboarding -> {
                        OnboardingScreen(
                            onOnboardingFinished = onboardingViewModel::onOnboardingFinished,
                        )
                    }

                    OnboardingUiState.Completed -> {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background,
                        ) {
                            NavigationRoot(
                                mainViewModel = mainViewModel,
                                settingsViewModel = settingsViewModel,
                            )
                        }
                    }
                }
            }
        }
    }
}
