package com.riobener.sonicsoul.ui.viewmodels

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riobener.sonicsoul.data.settings.Settings
import com.riobener.sonicsoul.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext applicationContext: Context,
    private val state: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val settings: Flow<List<Settings>> = settingsRepository.findAllSettings()

    fun saveSettings(settings: Settings) {
        viewModelScope.launch {
            settingsRepository.save(settings)
        }
    }
}