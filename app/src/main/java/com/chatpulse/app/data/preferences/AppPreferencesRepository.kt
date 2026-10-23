package com.chatpulse.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "chatpulse_preferences")

/**
 * Manages user preferences using Jetpack DataStore with a lightweight synchronous
 * SharedPreferences cache so foreground services and accessibility services can
 * query critical flags with zero blocking/coroutine overhead.
 */
class AppPreferencesRepository(private val context: Context) {

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        PREFS_CACHE_NAME,
        Context.MODE_PRIVATE
    )

    private object PreferencesKeys {
        val FLOATING_PILL = booleanPreferencesKey("floating_pill_enabled")
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color_enabled")
    }

    /**
     * Flow observing whether the floating pill overlay is enabled. Default: false.
     */
    val floatingPillEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.FLOATING_PILL]
                ?: sharedPrefs.getBoolean(KEY_FLOATING_PILL_CACHE, false)
        }

    /**
     * Flow observing whether biometric app lock is enabled. Default: false.
     */
    val biometricLockEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_LOCK]
                ?: sharedPrefs.getBoolean(KEY_BIOMETRIC_LOCK_CACHE, false)
        }

    /**
     * Flow observing whether the user has finished onboarding. Default: false.
     */
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED]
                ?: sharedPrefs.getBoolean(KEY_ONBOARDING_COMPLETED_CACHE, false)
        }

    /**
     * Flow observing whether Material You dynamic colors are enabled. Default: true.
     */
    val dynamicColorEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR]
                ?: sharedPrefs.getBoolean(KEY_DYNAMIC_COLOR_CACHE, true)
        }

    // Synchronous accessors for AccessibilityService and background workers

    fun isFloatingPillEnabledSync(): Boolean {
        return sharedPrefs.getBoolean(KEY_FLOATING_PILL_CACHE, false)
    }

    fun isBiometricLockEnabledSync(): Boolean {
        return sharedPrefs.getBoolean(KEY_BIOMETRIC_LOCK_CACHE, false)
    }

    fun isOnboardingCompletedSync(): Boolean {
        return sharedPrefs.getBoolean(KEY_ONBOARDING_COMPLETED_CACHE, false)
    }

    fun isDynamicColorEnabledSync(): Boolean {
        return sharedPrefs.getBoolean(KEY_DYNAMIC_COLOR_CACHE, true)
    }

    // Suspend mutating functions updating both DataStore and SharedPreferences cache

    suspend fun setFloatingPillEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_FLOATING_PILL_CACHE, enabled).apply()
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FLOATING_PILL] = enabled
        }
    }

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_BIOMETRIC_LOCK_CACHE, enabled).apply()
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_LOCK] = enabled
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED_CACHE, completed).apply()
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_DYNAMIC_COLOR_CACHE, enabled).apply()
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }

    // Daily limit alert deduplication

    /**
     * Checks if a daily limit alert notification has already been triggered for this chat today.
     */
    fun hasFiredDailyLimitAlertToday(chatName: String, todayDate: String): Boolean {
        val lastFiredDate = sharedPrefs.getString(PREFIX_ALERT_FIRED + chatName, null)
        return lastFiredDate == todayDate
    }

    /**
     * Records that a daily limit alert notification has fired today for this chat.
     */
    fun markDailyLimitAlertFiredToday(chatName: String, todayDate: String) {
        sharedPrefs.edit().putString(PREFIX_ALERT_FIRED + chatName, todayDate).apply()
    }

    /**
     * Clears the alert record for this chat, e.g. when limit is changed or reset.
     */
    fun clearDailyLimitAlertForChat(chatName: String) {
        sharedPrefs.edit().remove(PREFIX_ALERT_FIRED + chatName).apply()
    }

    /**
     * Clears all daily alert deduplication entries (e.g., at midnight or data reset).
     */
    fun clearAllDailyAlertRecords() {
        val editor = sharedPrefs.edit()
        sharedPrefs.all.keys.filter { it.startsWith(PREFIX_ALERT_FIRED) }.forEach { key ->
            editor.remove(key)
        }
        editor.apply()
    }

    companion object {
        private const val PREFS_CACHE_NAME = "chatpulse_prefs_cache"
        private const val KEY_FLOATING_PILL_CACHE = "floating_pill_enabled"
        private const val KEY_BIOMETRIC_LOCK_CACHE = "biometric_lock_enabled"
        private const val KEY_ONBOARDING_COMPLETED_CACHE = "onboarding_completed"
        private const val KEY_DYNAMIC_COLOR_CACHE = "dynamic_color_enabled"
        private const val PREFIX_ALERT_FIRED = "alert_fired_"

        @Volatile
        private var INSTANCE: AppPreferencesRepository? = null

        fun getInstance(context: Context): AppPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPreferencesRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
