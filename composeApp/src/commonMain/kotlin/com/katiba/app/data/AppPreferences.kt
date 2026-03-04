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
    private const val KEY_THEME_PREFERENCE = "theme_preference"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_AVATAR = "user_avatar_url"
    private const val KEY_USER_EMAIL_VERIFIED = "user_email_verified"
    private const val KEY_USER_JOINED_DATE = "user_joined_date"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

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
     * Theme preference: "Light", "Dark", or "System".
     * Defaults to "System" so the device setting is respected until the user explicitly changes it.
     */
    var themePreference: String
        get() = settings[KEY_THEME_PREFERENCE, "System"]
        set(value) {
            settings[KEY_THEME_PREFERENCE] = value
        }

    // ── Persisted user session ──────────────────────────────────────────

    var userName: String?
        get() = settings.getStringOrNull(KEY_USER_NAME)
        set(value) {
            if (value != null) settings[KEY_USER_NAME] = value
            else settings.remove(KEY_USER_NAME)
        }

    var userEmail: String?
        get() = settings.getStringOrNull(KEY_USER_EMAIL)
        set(value) {
            if (value != null) settings[KEY_USER_EMAIL] = value
            else settings.remove(KEY_USER_EMAIL)
        }

    var userAvatarUrl: String?
        get() = settings.getStringOrNull(KEY_USER_AVATAR)
        set(value) {
            if (value != null) settings[KEY_USER_AVATAR] = value
            else settings.remove(KEY_USER_AVATAR)
        }

    var userEmailVerified: Boolean
        get() = settings[KEY_USER_EMAIL_VERIFIED, false]
        set(value) {
            settings[KEY_USER_EMAIL_VERIFIED] = value
        }

    var userJoinedDate: String?
        get() = settings.getStringOrNull(KEY_USER_JOINED_DATE)
        set(value) {
            if (value != null) settings[KEY_USER_JOINED_DATE] = value
            else settings.remove(KEY_USER_JOINED_DATE)
        }

    var accessToken: String?
        get() = settings.getStringOrNull(KEY_ACCESS_TOKEN)
        set(value) {
            if (value != null) settings[KEY_ACCESS_TOKEN] = value
            else settings.remove(KEY_ACCESS_TOKEN)
        }

    var refreshToken: String?
        get() = settings.getStringOrNull(KEY_REFRESH_TOKEN)
        set(value) {
            if (value != null) settings[KEY_REFRESH_TOKEN] = value
            else settings.remove(KEY_REFRESH_TOKEN)
        }

    /**
     * Whether there is a persisted logged-in session.
     */
    val isLoggedIn: Boolean
        get() = currentUserId != null

    /**
     * Save a full user session after successful login.
     */
    fun saveUserSession(
        userId: String,
        name: String,
        email: String,
        avatarUrl: String = "",
        emailVerified: Boolean = false,
        joinedDate: String = "",
        access: String? = null,
        refresh: String? = null
    ) {
        currentUserId = userId
        userName = name
        userEmail = email
        userAvatarUrl = avatarUrl
        userEmailVerified = emailVerified
        userJoinedDate = joinedDate
        if (access != null) accessToken = access
        if (refresh != null) refreshToken = refresh
    }

    /**
     * Clear the persisted session (sign out).
     */
    fun clearSession() {
        currentUserId = null
        userName = null
        userEmail = null
        userAvatarUrl = null
        userEmailVerified = false
        userJoinedDate = null
        accessToken = null
        refreshToken = null
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
