package com.kimothorick.soloshelf.ui.onboarding

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kimothorick.soloshelf.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OnboardingUiState {
    data object Loading : OnboardingUiState

    data object ShowOnboarding : OnboardingUiState

    data object Completed : OnboardingUiState
}

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val userPreferencesRepository: UserPreferencesRepository,
        private val application: Application,
    ) : ViewModel() {
        val uiState: StateFlow<OnboardingUiState> =
            userPreferencesRepository.isFirstLaunch
                .map { isFirstLaunch ->
                    if (isFirstLaunch) OnboardingUiState.ShowOnboarding else OnboardingUiState.Completed
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = OnboardingUiState.Loading,
                )

        val isFirstLaunch =
            userPreferencesRepository.isFirstLaunch.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null,
            )

        fun onOnboardingFinished() {
            viewModelScope.launch { userPreferencesRepository.updateFirstLaunch() }
        }
    }
