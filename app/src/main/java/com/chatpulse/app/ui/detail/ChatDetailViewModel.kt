package com.chatpulse.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chatpulse.app.data.model.ChatDetailUiState
import com.chatpulse.app.data.model.DetailRange
import com.chatpulse.app.data.repository.ChatPulseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel managing analytics, customization, and daily limits for a specific WhatsApp conversation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatDetailViewModel(
    private val chatName: String,
    private val repository: ChatPulseRepository
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(DetailRange.WEEK)
    val selectedRange: StateFlow<DetailRange> = _selectedRange.asStateFlow()

    val uiState: StateFlow<ChatDetailUiState> = _selectedRange
        .flatMapLatest { range ->
            repository.observeChatDetail(chatName, range)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChatDetailUiState(chatName = chatName, displayName = chatName)
        )

    fun selectRange(range: DetailRange) {
        _selectedRange.value = range
    }

    fun updateDailyLimit(minutes: Int?) {
        viewModelScope.launch {
            repository.updateChatMetadata(
                chatName = chatName,
                dailyLimitMinutes = minutes,
                clearDailyLimit = (minutes == null || minutes <= 0)
            )
        }
    }

    fun toggleExcluded(excluded: Boolean) {
        viewModelScope.launch {
            repository.updateChatMetadata(
                chatName = chatName,
                isExcluded = excluded
            )
        }
    }

    fun updateCustomAlias(alias: String?) {
        viewModelScope.launch {
            val cleanAlias = alias?.trim()?.ifBlank { null }
            repository.updateChatMetadata(
                chatName = chatName,
                customAlias = cleanAlias
            )
        }
    }

    fun updateColorTag(colorArgb: Int?) {
        viewModelScope.launch {
            repository.updateChatMetadata(
                chatName = chatName,
                colorTag = colorArgb
            )
        }
    }

    companion object {
        fun provideFactory(chatName: String, repository: ChatPulseRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChatDetailViewModel(chatName, repository) as T
                }
            }
    }
}
