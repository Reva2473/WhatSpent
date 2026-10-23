package com.whatspent.app.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.whatspent.app.data.model.TimelineSessionItem
import com.whatspent.app.data.repository.WhatSpentRepository
import com.whatspent.app.util.TimeFormatUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel managing the chronological timeline of WhatsApp chat sessions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModel(
    private val repository: WhatSpentRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(TimeFormatUtils.todayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    /**
     * Set of available dates including at least the past 7 days and any historical recorded dates.
     */
    val availableDates: StateFlow<List<String>> = repository.observeAvailableDates()
        .map { dbDates ->
            val past7Days = (0..6).map { TimeFormatUtils.dateStringDaysAgo(it) }
            (past7Days + dbDates).distinct().sortedDescending()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = (0..6).map { TimeFormatUtils.dateStringDaysAgo(it) }
        )

    /**
     * Sessions for the currently selected date.
     */
    val sessions: StateFlow<List<TimelineSessionItem>> = _selectedDate
        .flatMapLatest { date ->
            repository.observeTimelineForDate(date)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    companion object {
        fun provideFactory(repository: WhatSpentRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TimelineViewModel(repository) as T
                }
            }
    }
}
