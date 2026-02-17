package com.katiba.app.data.repository

import kotlinx.datetime.*

/**
 * Manages tracking of articles read in the Katiba/Constitution page.
 * Tracks unique articles opened by the user.
 */
object ArticleReadManager {
    // Set of article IDs that have been read
    private val readArticles = mutableSetOf<String>()
    
    /**
     * Record that an article was opened/read.
     * @param chapterNumber The chapter number
     * @param articleNumber The article number within the chapter
     */
    fun recordArticleRead(chapterNumber: Int, articleNumber: Int) {
        val articleId = "${chapterNumber}_${articleNumber}"
        readArticles.add(articleId)
    }
    
    /**
     * Record that a schedule was opened/read.
     * @param scheduleNumber The schedule number
     */
    fun recordScheduleRead(scheduleNumber: Int) {
        val articleId = "schedule_${scheduleNumber}"
        readArticles.add(articleId)
    }
    
    /**
     * Record that the preamble was opened/read.
     */
    fun recordPreambleRead() {
        readArticles.add("preamble")
    }
    
    /**
     * Get the total count of unique articles read.
     */
    fun getArticlesReadCount(): Int {
        return readArticles.size
    }
    
    /**
     * Check if a specific article has been read.
     */
    fun isArticleRead(chapterNumber: Int, articleNumber: Int): Boolean {
        val articleId = "${chapterNumber}_${articleNumber}"
        return readArticles.contains(articleId)
    }
    
    /**
     * Reset article read tracking (for testing or user reset).
     */
    fun reset() {
        readArticles.clear()
    }
}
