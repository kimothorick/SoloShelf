package com.kimothorick.soloshelf.ui.screens.settings

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kimothorick.soloshelf.data.preferences.AppTheme
import com.kimothorick.soloshelf.data.preferences.ColorTheme
import com.kimothorick.soloshelf.data.preferences.ThemeMode
import com.kimothorick.soloshelf.data.preferences.UserPreferencesRepository
import com.materialkolor.PaletteStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val themePreference: StateFlow<AppTheme> = userPreferencesRepository.themePreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.AUTO
        )

    val themeMode: StateFlow<ThemeMode> = userPreferencesRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.MATERIAL_YOU
        )

    val colorTheme: StateFlow<ColorTheme> = userPreferencesRepository.colorTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ColorTheme.MONO
        )

    val amoledBlack: StateFlow<Boolean> = userPreferencesRepository.amoledBlack
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val autoScanOnStartup: StateFlow<Boolean> = userPreferencesRepository.autoScanOnStartup
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val customSeedColor: StateFlow<Color> = userPreferencesRepository.customSeedColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Color.Blue
        )

    val paletteStyle: StateFlow<PaletteStyle> = userPreferencesRepository.paletteStyle
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PaletteStyle.TonalSpot
        )

    @Deprecated("Use themeMode instead")
    val dynamicColor: StateFlow<Boolean> = userPreferencesRepository.dynamicColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            userPreferencesRepository.updateThemePreference(theme)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.updateThemeMode(mode)
        }
    }

    fun setColorTheme(theme: ColorTheme) {
        viewModelScope.launch {
            userPreferencesRepository.updateColorTheme(theme)
        }
    }

    fun setAmoledBlack(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateAmoledBlack(enabled)
        }
    }

    fun setAutoScanOnStartup(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateAutoScanOnStartup(enabled)
        }
    }

    fun setCustomSeedColor(color: Color) {
        viewModelScope.launch {
            userPreferencesRepository.updateCustomSeedColor(color)
        }
    }

    fun setPaletteStyle(style: PaletteStyle) {
        viewModelScope.launch {
            userPreferencesRepository.updatePaletteStyle(style)
        }
    }

    @Deprecated("Use setThemeMode instead")
    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateDynamicColor(enabled)
        }
    }
}
