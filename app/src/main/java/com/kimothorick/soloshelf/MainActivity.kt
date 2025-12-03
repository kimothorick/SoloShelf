package com.kimothorick.soloshelf

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kimothorick.soloshelf.ui.onboarding.OnboardingScreen
import com.kimothorick.soloshelf.ui.onboarding.OnboardingViewModel
import com.kimothorick.soloshelf.ui.onboarding.PermissionState
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: OnboardingViewModel by viewModels()

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val notificationPermissionLauncher =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                ) { isGranted ->
                    viewModel.handleNotificationPermissionResult(isGranted) {
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS,
                        )
                    }
                }

            val audioPermissionLauncher =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                ) { isGranted ->
                    val audioPermission =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_AUDIO
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                    viewModel.handleAudioPermissionResult(isGranted) {
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            audioPermission,
                        )
                    }
                }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner, viewModel) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        viewModel.checkNotificationPermissionStatus()
                        viewModel.checkAudioPermissionStatus()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            SoloShelfTheme(dynamicColor = false) {
                val isFirstLaunch by viewModel.isFirstLaunch.collectAsStateWithLifecycle()
                val notificationPermissionState by viewModel.notificationPermissionState.collectAsStateWithLifecycle()
                val audioPermissionState by viewModel.audioPermissionState.collectAsStateWithLifecycle()

                if (isFirstLaunch) {
                    OnboardingScreen(
                        onOnboardingFinished = viewModel::onOnboardingFinished,
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
                            val permission =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Column(modifier = Modifier.padding(innerPadding)) {
                            Text("This is the home screen")
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
                if (isMinTiramisu && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    return
                }
                launcher.launch(permission)
            }
        }
    }
}
