package com.katiba.app.data.repository

import com.katiba.app.data.model.*
import kotlinx.serialization.json.Json

/**
 * Repository for accessing the Constitution of Kenya data.
 *
 * This repository provides access to the full constitution data loaded from
 * the bundled JSON resource file.
 */
object ConstitutionRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Cached constitution data - will be populated on first access
    private var cachedConstitution: Constitution? = null

    /**
     * The full Constitution data.
     * Note: This should be initialized by calling loadConstitution() first.
     */
    val constitution: Constitution
        get() = cachedConstitution ?: Constitution(preamble = "", chapters = emptyList())

    /**
     * Get the preamble of the Constitution.
     */
    val preamble: String
        get() = constitution.preamble

    /**
     * Get all chapters of the Constitution.
     */
    val chapters: List<Chapter>
        get() = constitution.chapters

    /**
     * Initialize the repository with constitution JSON data.
     * This should be called once when the app starts, typically from a composable
     * that can access Compose resources.
     */
    fun loadFromJson(jsonString: String) {
        cachedConstitution = json.decodeFromString<Constitution>(jsonString)
    }

    /**
     * Check if the constitution data has been loaded.
     */
    fun isLoaded(): Boolean = cachedConstitution != null

    /**
     * Get a specific chapter by number.
     */
    fun getChapter(number: Int): Chapter? {
        return chapters.find { it.number == number }
    }

    /**
     * Get a specific article by number.
     */
    fun getArticle(articleNumber: Int): Article? {
        for (chapter in chapters) {
            val article = chapter.articles.find { it.number == articleNumber }
            if (article != null) return article
        }
        return null
    }

    /**
     * Get the chapter that contains a specific article.
     */
    fun getChapterForArticle(articleNumber: Int): Chapter? {
        return chapters.find { chapter ->
            chapter.articles.any { it.number == articleNumber }
        }
    }

    /**
     * Search for articles containing the given query text.
     */
    fun searchArticles(query: String): List<Pair<Chapter, Article>> {
        if (query.isBlank()) return emptyList()

        val lowerQuery = query.lowercase()
        val results = mutableListOf<Pair<Chapter, Article>>()

        for (chapter in chapters) {
            for (article in chapter.articles) {
                val matches = article.title.lowercase().contains(lowerQuery) ||
                    article.clauses.any { clause ->
                        clause.text.lowercase().contains(lowerQuery) ||
                        clause.subClauses.any { sub -> sub.text.lowercase().contains(lowerQuery) }
                    }

                if (matches) {
                    results.add(chapter to article)
                }
            }
        }

        return results
    }

    /**
     * Get statistics about the constitution.
     */
    fun getStatistics(): ConstitutionStats {
        var totalArticles = 0
        var totalClauses = 0

        for (chapter in chapters) {
            totalArticles += chapter.articles.size
            for (article in chapter.articles) {
                totalClauses += article.clauses.size
            }
        }

        return ConstitutionStats(
            totalChapters = chapters.size,
            totalArticles = totalArticles,
            totalClauses = totalClauses
        )
    }

    /**
     * Get a random article for "Article of the Day" feature.
     */
    fun getRandomArticle(): Pair<Chapter, Article>? {
        val allArticles = chapters.flatMap { chapter ->
            chapter.articles.map { article -> chapter to article }
        }
        return allArticles.randomOrNull()
    }

    /**
     * Get articles related to a specific topic/keyword.
     */
    fun getArticlesByTopic(topic: String): List<Pair<Chapter, Article>> {
        return searchArticles(topic)
    }

    /**
     * Get a condensed summary of the constitution for AI context (RAG).
     * This provides key information without overwhelming token limits.
     */
    fun getContextSummary(): String {
        if (!isLoaded()) return ""
        
        val summaryBuilder = StringBuilder()
        summaryBuilder.appendLine("CONSTITUTION OF KENYA, 2010")
        summaryBuilder.appendLine("=" .repeat(50))
        summaryBuilder.appendLine()
        
        // Add preamble summary
        if (preamble.isNotBlank()) {
            summaryBuilder.appendLine("PREAMBLE:")
            summaryBuilder.appendLine(preamble.take(500))
            if (preamble.length > 500) summaryBuilder.appendLine("...")
            summaryBuilder.appendLine()
        }
        
        // Add chapters and articles summary
        for (chapter in chapters) {
            summaryBuilder.appendLine("CHAPTER ${chapter.number}: ${chapter.title}")
            for (article in chapter.articles) {
                summaryBuilder.appendLine("  Article ${article.number}: ${article.title}")
                // Include first clause of each article
                article.clauses.firstOrNull()?.let { clause ->
                    val shortText = clause.text.take(200)
                    summaryBuilder.appendLine("    ${shortText}${if (clause.text.length > 200) "..." else ""}")
                }
            }
            summaryBuilder.appendLine()
        }
        
        return summaryBuilder.toString()
    }
}

/**
 * Statistics about the Constitution.
 */
data class ConstitutionStats(
    val totalChapters: Int,
    val totalArticles: Int,
    val totalClauses: Int
)

