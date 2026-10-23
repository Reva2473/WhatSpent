package com.whatspent.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.whatspent.app.data.repository.WhatSpentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel managing user preferences, permission status triggers, and data controls.
 */
class SettingsViewModel(
    private val repository: WhatSpentRepository
) : ViewModel() {

    val floatingPillEnabled: StateFlow<Boolean> = repository.preferences.floatingPillEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val biometricLockEnabled: StateFlow<Boolean> = repository.preferences.biometricLockEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val dynamicColorEnabled: StateFlow<Boolean> = repository.preferences.dynamicColorEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun setFloatingPillEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.preferences.setFloatingPillEnabled(enabled)
        }
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.preferences.setBiometricLockEnabled(enabled)
        }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.preferences.setDynamicColorEnabled(enabled)
        }
    }

    fun seedDemoData() {
        viewModelScope.launch {
            repository.seedDemoData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            repository.preferences.setOnboardingCompleted(false)
        }
    }

    companion object {
        fun provideFactory(repository: WhatSpentRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(repository) as T
                }
            }
    }
}
