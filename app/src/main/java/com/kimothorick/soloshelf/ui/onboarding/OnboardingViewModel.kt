package com.kimothorick.soloshelf.ui.onboarding

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kimothorick.soloshelf.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val userPreferencesRepository: UserPreferencesRepository,
        private val application: Application,
    ) : ViewModel() {
        val isFirstLaunch =
            userPreferencesRepository.isFirstLaunch.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = true,
            )

        private val _notificationPermissionState = MutableStateFlow(PermissionState.IDLE)
        val notificationPermissionState = _notificationPermissionState.asStateFlow()

        private val _audioPermissionState = MutableStateFlow(PermissionState.IDLE)
        val audioPermissionState = _audioPermissionState.asStateFlow()

        init {
            checkNotificationPermissionStatus()
            checkAudioPermissionStatus()
        }

        fun onOnboardingFinished() {
            viewModelScope.launch { userPreferencesRepository.updateFirstLaunch() }
        }

        fun handleNotificationPermissionResult(
            isGranted: Boolean,
            shouldShowRationale: () -> Boolean,
        ) {
            handlePermissionResult(isGranted, shouldShowRationale, _notificationPermissionState)
        }

        fun handleAudioPermissionResult(
            isGranted: Boolean,
            shouldShowRationale: () -> Boolean,
        ) {
            handlePermissionResult(isGranted, shouldShowRationale, _audioPermissionState)
        }

        private fun handlePermissionResult(
            isGranted: Boolean,
            shouldShowRationale: () -> Boolean,
            state: MutableStateFlow<PermissionState>,
        ) {
            if (isGranted) {
                state.value = PermissionState.GRANTED
            } else {
                if (shouldShowRationale()) {
                    state.value = PermissionState.DENIED
                } else {
                    state.value = PermissionState.DENIED_FOREVER
                }
            }
        }

        fun checkNotificationPermissionStatus() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkPermissionStatus(
                    Manifest.permission.POST_NOTIFICATIONS,
                    _notificationPermissionState,
                )
            } else {
                _notificationPermissionState.value = PermissionState.GRANTED
            }
        }

        fun checkAudioPermissionStatus() {
            val permission =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
            checkPermissionStatus(permission, _audioPermissionState)
        }

        private fun checkPermissionStatus(
            permission: String,
            state: MutableStateFlow<PermissionState>,
        ) {
            val isGranted =
                ContextCompat.checkSelfPermission(
                    application,
                    permission,
                ) == PackageManager.PERMISSION_GRANTED

            if (isGranted) {
                state.value = PermissionState.GRANTED
            }
        }
    }
