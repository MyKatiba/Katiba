package com.katiba.app.data.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

/**
 * Manages bookmarked clauses of the day.
 * Persists bookmarks using multiplatform-settings (SharedPreferences on Android, NSUserDefaults on iOS).
 */
object BookmarkManager {
    private val settings: Settings = Settings()
    private const val KEY_BOOKMARKS = "bookmarked_clauses"
    private const val SEPARATOR = "\u001E" // ASCII Record Separator - won't appear in text
    private const val FIELD_SEPARATOR = "\u001F" // ASCII Unit Separator - won't appear in text

    /**
     * A single bookmark entry.
     */
    data class BookmarkEntry(
        val id: String,
        val articleNumber: Int,
        val clauseNumber: String,
        val clauseText: String,
        val chapterNumber: Int,
        val chapterTitle: String,
        val articleTitle: String,
        val date: String
    )

    /**
     * Add a bookmark. If it already exists (by id), it won't be duplicated.
     */
    fun addBookmark(entry: BookmarkEntry) {
        val existing = getBookmarks().toMutableList()
        if (existing.none { it.id == entry.id }) {
            existing.add(entry)
            saveBookmarks(existing)
        }
    }

    /**
     * Remove a bookmark by id.
     */
    fun removeBookmark(id: String) {
        val existing = getBookmarks().toMutableList()
        existing.removeAll { it.id == id }
        saveBookmarks(existing)
    }

    /**
     * Check if a specific clause is bookmarked by its id.
     */
    fun isBookmarked(id: String): Boolean {
        return getBookmarks().any { it.id == id }
    }

    /**
     * Toggle bookmark state. Returns the new state (true = bookmarked).
     */
    fun toggleBookmark(entry: BookmarkEntry): Boolean {
        return if (isBookmarked(entry.id)) {
            removeBookmark(entry.id)
            false
        } else {
            addBookmark(entry)
            true
        }
    }

    /**
     * Get all bookmarks.
     */
    fun getBookmarks(): List<BookmarkEntry> {
        val raw: String = settings[KEY_BOOKMARKS, ""]
        if (raw.isEmpty()) return emptyList()
        return raw.split(SEPARATOR).mapNotNull { entryStr ->
            val parts = entryStr.split(FIELD_SEPARATOR)
            if (parts.size >= 8) {
                BookmarkEntry(
                    id = parts[0],
                    articleNumber = parts[1].toIntOrNull() ?: 0,
                    clauseNumber = parts[2],
                    clauseText = parts[3],
                    chapterNumber = parts[4].toIntOrNull() ?: 0,
                    chapterTitle = parts[5],
                    articleTitle = parts[6],
                    date = parts[7]
                )
            } else null
        }
    }

    /**
     * Get bookmark count.
     */
    fun getBookmarkCount(): Int = getBookmarks().size

    private fun saveBookmarks(bookmarks: List<BookmarkEntry>) {
        val raw = bookmarks.joinToString(SEPARATOR) { entry ->
            listOf(
                entry.id,
                entry.articleNumber.toString(),
                entry.clauseNumber,
                entry.clauseText,
                entry.chapterNumber.toString(),
                entry.chapterTitle,
                entry.articleTitle,
                entry.date
            ).joinToString(FIELD_SEPARATOR)
        }
        settings[KEY_BOOKMARKS] = raw
    }
}

