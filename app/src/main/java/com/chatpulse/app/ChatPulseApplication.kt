package com.chatpulse.app

import android.app.Application
import com.chatpulse.app.data.local.ChatPulseDatabase
import com.chatpulse.app.data.preferences.AppPreferencesRepository
import com.chatpulse.app.data.repository.ChatPulseRepository
import com.chatpulse.app.service.NotificationHelper

/**
 * Application entry point for ChatPulse.
 * Configures notification channels and exposes lazily initialized singleton dependencies.
 */
class ChatPulseApplication : Application() {

    val database: ChatPulseDatabase by lazy {
        ChatPulseDatabase.getInstance(this)
    }

    val preferences: AppPreferencesRepository by lazy {
        AppPreferencesRepository.getInstance(this)
    }

    val repository: ChatPulseRepository by lazy {
        ChatPulseRepository.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }
}
