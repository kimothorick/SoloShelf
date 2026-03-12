package com.kimothorick.soloshelf

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kimothorick.soloshelf.data.preferences.AppTheme
import com.kimothorick.soloshelf.navigation.NavigationRoot
import com.kimothorick.soloshelf.ui.main.MainViewModel
import com.kimothorick.soloshelf.ui.onboarding.OnboardingScreen
import com.kimothorick.soloshelf.ui.onboarding.OnboardingViewModel
import com.kimothorick.soloshelf.ui.onboarding.PermissionState
import com.kimothorick.soloshelf.ui.screens.settings.SettingsViewModel
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val onboardingViewModel: OnboardingViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themePreference by settingsViewModel.themePreference.collectAsStateWithLifecycle()
            val dynamicColorPreference by settingsViewModel.dynamicColor.collectAsStateWithLifecycle()

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
                onboardingViewModel.handleNotificationPermissionResult(isGranted) {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS,
                    )
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
                dynamicColor = dynamicColorPreference,
            ) {
                val isFirstLaunch by onboardingViewModel.isFirstLaunch.collectAsStateWithLifecycle()
                val notificationPermissionState by onboardingViewModel.notificationPermissionState.collectAsStateWithLifecycle()
                val audioPermissionState by onboardingViewModel.audioPermissionState.collectAsStateWithLifecycle()

                if (isFirstLaunch) {
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
                } else {
                    NavigationRoot(
                        mainViewModel = mainViewModel,
                        settingsViewModel = settingsViewModel,
                    )
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
                if (isMinTiramisu && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    return
                }
                launcher.launch(permission)
            }
        }
    }
}
