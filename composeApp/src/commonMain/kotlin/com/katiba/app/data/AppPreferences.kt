package com.katiba.app.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

/**
 * App-wide preferences using multiplatform-settings.
 * Persists data across app sessions using SharedPreferences (Android) / NSUserDefaults (iOS).
 */
object AppPreferences {
    private val settings: Settings = Settings()
    
    // Keys
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    private const val KEY_CURRENT_USER_ID = "current_user_id"
    
    /**
     * Check if user has completed onboarding.
     */
    var hasCompletedOnboarding: Boolean
        get() = settings[KEY_ONBOARDING_COMPLETED, false]
        set(value) {
            settings[KEY_ONBOARDING_COMPLETED] = value
        }
    
    /**
     * Store the current user ID to identify returning users.
     */
    var currentUserId: String?
        get() = settings.getStringOrNull(KEY_CURRENT_USER_ID)
        set(value) {
            if (value != null) {
                settings[KEY_CURRENT_USER_ID] = value
            } else {
                settings.remove(KEY_CURRENT_USER_ID)
            }
        }
    
    /**
     * Clear all preferences (for sign out).
     */
    fun clearAll() {
        settings.clear()
    }
    
    /**
     * Clear user-specific data but keep onboarding status.
     */
    fun clearUserData() {
        currentUserId = null
    }
}
