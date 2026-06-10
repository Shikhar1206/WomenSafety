package com.example.womensafety.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.womensafety.data.preferences.UserPreferences
import com.example.womensafety.data.preferences.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataStore: UserPreferencesDataStore
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = preferencesDataStore.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setShakeEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setShakeEnabled(enabled) }
    }

    fun setShakeSensitivity(sensitivity: Float) {
        viewModelScope.launch { preferencesDataStore.setShakeSensitivity(sensitivity) }
    }

    fun setSafeWordEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setSafeWordEnabled(enabled) }
    }

    fun setSafeWord(word: String) {
        viewModelScope.launch { preferencesDataStore.setSafeWord(word) }
    }

    fun setBatterySosEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setBatterySosEnabled(enabled) }
    }

    fun setAutoCaptureEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setAutoCaptureEnabled(enabled) }
    }

    fun setFakeCallName(name: String) {
        viewModelScope.launch { preferencesDataStore.setFakeCallName(name) }
    }

    fun setCalendarUnlockDay(day: Int) {
        viewModelScope.launch { preferencesDataStore.setCalendarUnlockDay(day) }
    }
}
