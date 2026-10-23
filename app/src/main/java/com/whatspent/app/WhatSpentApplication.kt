package com.whatspent.app

import android.app.Application
import com.whatspent.app.data.local.WhatSpentDatabase
import com.whatspent.app.data.preferences.AppPreferencesRepository
import com.whatspent.app.data.repository.WhatSpentRepository
import com.whatspent.app.service.NotificationHelper

/**
 * Application entry point for WhatSpent.
 * Configures notification channels and exposes lazily initialized singleton dependencies.
 */
class WhatSpentApplication : Application() {

    val database: WhatSpentDatabase by lazy {
        WhatSpentDatabase.getInstance(this)
    }

    val preferences: AppPreferencesRepository by lazy {
        AppPreferencesRepository.getInstance(this)
    }

    val repository: WhatSpentRepository by lazy {
        WhatSpentRepository.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }
}
