package com.katiba.app.data.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Manages user streak tracking for daily app usage.
 * Streak increments when user opens app on consecutive days.
 * Resets to 1 if user misses a day.
 * Data is persisted using multiplatform-settings.
 */
object StreakManager {
    private val settings: Settings = Settings()
    private val json = Json { ignoreUnknownKeys = true }
    
    // Settings keys
    private const val KEY_CURRENT_STREAK = "streak_current"
    private const val KEY_BEST_STREAK = "streak_best"
    private const val KEY_LAST_ACCESS_DATE = "streak_last_access"
    private const val KEY_DAILY_REFRESH_STREAK = "streak_daily_refresh"
    private const val KEY_BEST_DAILY_REFRESH_STREAK = "streak_best_daily_refresh"
    private const val KEY_LAST_REFRESH_DATE = "streak_last_refresh"
    private const val KEY_STREAK_HISTORY = "streak_history"
    
    private var currentStreak: Int = settings[KEY_CURRENT_STREAK, 0]
    private var bestStreak: Int = settings[KEY_BEST_STREAK, 0]
    private var lastAccessDate: String? = settings.getStringOrNull(KEY_LAST_ACCESS_DATE)
    
    // Daily Refresh Streak tracking
    private var dailyRefreshStreak: Int = settings[KEY_DAILY_REFRESH_STREAK, 0]
    private var bestDailyRefreshStreak: Int = settings[KEY_BEST_DAILY_REFRESH_STREAK, 0]
    private var lastRefreshDate: String? = settings.getStringOrNull(KEY_LAST_REFRESH_DATE)
    
    // Store dates for past 7 days with streak info
    private val streakHistory: MutableMap<String, StreakData> = loadStreakHistory()
    
    @Serializable
    data class StreakData(
        val hasAppStreak: Boolean = false,
        val hasDailyRefresh: Boolean = false
    )
    
    private fun loadStreakHistory(): MutableMap<String, StreakData> {
        val historyJson = settings.getStringOrNull(KEY_STREAK_HISTORY) ?: return mutableMapOf()
        return try {
            json.decodeFromString<Map<String, StreakData>>(historyJson).toMutableMap()
        } catch (e: Exception) {
            mutableMapOf()
        }
    }
    
    private fun saveStreakHistory() {
        settings[KEY_STREAK_HISTORY] = json.encodeToString(streakHistory.toMap())
    }
    
    private fun saveState() {
        settings[KEY_CURRENT_STREAK] = currentStreak
        settings[KEY_BEST_STREAK] = bestStreak
        if (lastAccessDate != null) {
            settings[KEY_LAST_ACCESS_DATE] = lastAccessDate!!
        }
        settings[KEY_DAILY_REFRESH_STREAK] = dailyRefreshStreak
        settings[KEY_BEST_DAILY_REFRESH_STREAK] = bestDailyRefreshStreak
        if (lastRefreshDate != null) {
            settings[KEY_LAST_REFRESH_DATE] = lastRefreshDate!!
        }
        saveStreakHistory()
    }
    
    /**
     * Call this when the app launches to check and update streak.
     * Returns the updated streak count.
     */
    fun checkAndUpdateStreak(): Int {
        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()
        
        when (lastAccessDate) {
            today -> {
                // Already logged in today, keep current streak
            }
            yesterday -> {
                // Logged in yesterday, increment streak
                currentStreak++
                lastAccessDate = today
                // Update best streak if current exceeds it
                if (currentStreak > bestStreak) {
                    bestStreak = currentStreak
                }
            }
            null -> {
                // First time user, start streak at 1
                currentStreak = 1
                lastAccessDate = today
                if (bestStreak == 0) {
                    bestStreak = 1
                }
            }
            else -> {
                // Missed a day (or more), reset streak to 1
                currentStreak = 1
                lastAccessDate = today
            }
        }
        
        // Update streak history for today
        val currentData = streakHistory.getOrDefault(today, StreakData())
        streakHistory[today] = currentData.copy(hasAppStreak = true)
        
        // Persist changes
        saveState()
        
        return currentStreak
    }
    
    /**
     * Call when user completes all 4 pages of the deep dive card.
     * Updates daily refresh streak.
     */
    fun recordDailyRefreshCompletion() {
        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()
        
        when (lastRefreshDate) {
            today -> {
                // Already completed today, keep current streak
            }
            yesterday -> {
                // Completed yesterday, increment streak
                dailyRefreshStreak++
                lastRefreshDate = today
                if (dailyRefreshStreak > bestDailyRefreshStreak) {
                    bestDailyRefreshStreak = dailyRefreshStreak
                }
            }
            null -> {
                // First time completing, start streak at 1
                dailyRefreshStreak = 1
                lastRefreshDate = today
                if (bestDailyRefreshStreak == 0) {
                    bestDailyRefreshStreak = 1
                }
            }
            else -> {
                // Missed a day, reset streak to 1
                dailyRefreshStreak = 1
                lastRefreshDate = today
            }
        }
        
        // Update streak history for today
        val currentData = streakHistory.getOrDefault(today, StreakData())
        streakHistory[today] = currentData.copy(hasDailyRefresh = true)
        
        // Persist changes
        saveState()
    }
    
    /**
     * Get daily refresh streak count.
     */
    fun getDailyRefreshStreak(): Int = dailyRefreshStreak
    
    /**
     * Get best daily refresh streak.
     */
    fun getBestDailyRefreshStreak(): Int = bestDailyRefreshStreak
    
    /**
     * Get streak data for the past 7 days including today.
     * Returns list of pairs: (date string, StreakData)
     */
    fun getWeekStreakData(): List<Pair<String, StreakData>> {
        val today = getTodayDateString()
        val result = mutableListOf<Pair<String, StreakData>>()
        
        // Generate past 7 days
        for (i in 6 downTo 0) {
            val date = getDateMinusDays(i)
            val data = streakHistory.getOrDefault(date, StreakData())
            result.add(date to data)
        }
        
        return result
    }
    
    /**
     * Get the current streak count without updating.
     */
    fun getCurrentStreak(): Int = currentStreak
    
    /**
     * Get the best streak ever achieved.
     */
    fun getBestStreak(): Int = bestStreak
    
    /**
     * Get last access date (for debugging/testing).
     */
    fun getLastAccessDate(): String? = lastAccessDate
    
    /**
     * Reset streak (for testing purposes or sign out).
     */
    fun resetStreak() {
        currentStreak = 0
        bestStreak = 0
        lastAccessDate = null
        dailyRefreshStreak = 0
        bestDailyRefreshStreak = 0
        lastRefreshDate = null
        streakHistory.clear()
        
        // Clear persisted data
        settings.remove(KEY_CURRENT_STREAK)
        settings.remove(KEY_BEST_STREAK)
        settings.remove(KEY_LAST_ACCESS_DATE)
        settings.remove(KEY_DAILY_REFRESH_STREAK)
        settings.remove(KEY_BEST_DAILY_REFRESH_STREAK)
        settings.remove(KEY_LAST_REFRESH_DATE)
        settings.remove(KEY_STREAK_HISTORY)
    }
    
    /**
     * Manual override for testing (sets streak and last access date).
     */
    fun setStreakForTesting(streak: Int, dateString: String, best: Int = streak) {
        currentStreak = streak
        lastAccessDate = dateString
        bestStreak = best
        saveState()
    }
    
    /**
     * Get today's date as a string in format YYYY-MM-DD.
     */
    private fun getTodayDateString(): String {
        val now = Clock.System.now()
        val localDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return localDate.toString()
    }
    
    /**
     * Get yesterday's date as a string in format YYYY-MM-DD.
     */
    private fun getYesterdayDateString(): String {
        val now = Clock.System.now()
        val yesterday = now.minus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
        val localDate = yesterday.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return localDate.toString()
    }
    
    /**
     * Get date minus specified days in format YYYY-MM-DD.
     */
    private fun getDateMinusDays(days: Int): String {
        val now = Clock.System.now()
        val targetDate = now.minus(days, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
        val localDate = targetDate.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return localDate.toString()
    }
}
