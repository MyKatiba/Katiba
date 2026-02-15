package com.katiba.app.data.repository

import kotlinx.datetime.*

/**
 * Manages user streak tracking for daily app usage.
 * Streak increments when user opens app on consecutive days.
 * Resets to 1 if user misses a day.
 */
object StreakManager {
    private var currentStreak: Int = 0
    private var bestStreak: Int = 0
    private var lastAccessDate: String? = null
    
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
        
        return currentStreak
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
     * Reset streak (for testing purposes).
     */
    fun resetStreak() {
        currentStreak = 0
        bestStreak = 0
        lastAccessDate = null
    }
    
    /**
     * Manual override for testing (sets streak and last access date).
     */
    fun setStreakForTesting(streak: Int, dateString: String, best: Int = streak) {
        currentStreak = streak
        lastAccessDate = dateString
        bestStreak = best
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
}
