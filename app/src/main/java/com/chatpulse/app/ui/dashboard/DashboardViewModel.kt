package com.chatpulse.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chatpulse.app.data.model.DashboardUiState
import com.chatpulse.app.data.repository.ChatPulseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel powering the main ChatPulse dashboard.
 */
class DashboardViewModel(
    private val repository: ChatPulseRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = repository.observeDashboardState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState(isLoading = true)
        )

    /**
     * Seeds realistic sample conversations and sessions to preview charts and features.
     */
    fun seedDemoData() {
        viewModelScope.launch {
            repository.seedDemoData()
        }
    }

    companion object {
        fun provideFactory(repository: ChatPulseRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DashboardViewModel(repository) as T
                }
            }
    }
}
