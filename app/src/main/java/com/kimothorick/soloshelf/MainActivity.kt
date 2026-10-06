package com.kimothorick.soloshelf

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kimothorick.soloshelf.data.preferences.AppTheme
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.kimothorick.soloshelf.navigation.NavigationRoot
import com.kimothorick.soloshelf.ui.main.MainViewModel
import com.kimothorick.soloshelf.ui.onboarding.OnboardingScreen
import com.kimothorick.soloshelf.ui.onboarding.OnboardingUiState
import com.kimothorick.soloshelf.ui.onboarding.OnboardingViewModel
import com.kimothorick.soloshelf.ui.onboarding.PermissionState
import com.kimothorick.soloshelf.ui.screens.settings.SettingsViewModel
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import dagger.hilt.android.AndroidEntryPoint

@OptIn(UnstableApi::class)
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val onboardingViewModel: OnboardingViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            onboardingViewModel.uiState.value is OnboardingUiState.Loading
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
            }

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
            ) { isGranted ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    onboardingViewModel.handleNotificationPermissionResult(isGranted) {
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS,
                        )
                    }
                }
            }

            val audioPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
            ) { isGranted ->
                val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
                onboardingViewModel.handleAudioPermissionResult(isGranted) {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        audioPermission,
                    )
                }
            }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner, onboardingViewModel) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        onboardingViewModel.checkNotificationPermissionStatus()
                        onboardingViewModel.checkAudioPermissionStatus()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
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
                val notificationPermissionState by onboardingViewModel.notificationPermissionState.collectAsStateWithLifecycle()
                val audioPermissionState by onboardingViewModel.audioPermissionState.collectAsStateWithLifecycle()

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
                            onGrantNotificationPermissionClicked = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    handlePermissionClick(
                                        state = notificationPermissionState,
                                        permission = Manifest.permission.POST_NOTIFICATIONS,
                                        launcher = notificationPermissionLauncher,
                                        isMinTiramisu = true,
                                    )
                                }
                            },
                            notificationPermissionState = notificationPermissionState,
                            onGrantAudioPermissionClicked = {
                                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    Manifest.permission.READ_MEDIA_AUDIO
                                } else {
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                }
                                handlePermissionClick(
                                    state = audioPermissionState,
                                    permission = permission,
                                    launcher = audioPermissionLauncher,
                                )
                            },
                            audioPermissionState = audioPermissionState,
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

    private fun handlePermissionClick(
        state: PermissionState,
        permission: String,
        launcher: androidx.activity.compose.ManagedActivityResultLauncher<String, Boolean>,
        isMinTiramisu: Boolean = false,
    ) {
        when (state) {
            PermissionState.DENIED_FOREVER -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            else -> {
                if (isMinTiramisu) {
                    launcher.launch(permission)
                } else {
                    launcher.launch(permission)
                }
            }
        }
    }
}
