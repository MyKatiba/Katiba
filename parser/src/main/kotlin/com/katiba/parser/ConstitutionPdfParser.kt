package com.katiba.parser

import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

/**
 * Main parser for the Constitution of Kenya PDF.
 *
 * This parser extracts the full text from the PDF and structures it into
 * chapters, articles, and clauses that can be serialized to YAML.
 */
class ConstitutionPdfParser(private val pdfFile: File) {

    private val chapterPattern = Regex("""CHAPTER\s+(\w+)\s*[-–—]\s*(.+?)(?=\n|$)""", RegexOption.IGNORE_CASE)
    private val partPattern = Regex("""PART\s+(\d+)\s*[-–—]\s*(.+?)(?=\n|$)""", RegexOption.IGNORE_CASE)
    private val articlePattern = Regex("""(\d+)\.\s+(.+?)(?=\n|$)""")
    private val clausePattern = Regex("""^\s*\((\d+)\)\s*(.+)""")
    private val subClausePattern = Regex("""^\s*\(([a-z])\)\s*(.+)""")
    private val romanNumerals = mapOf(
        "ONE" to 1, "TWO" to 2, "THREE" to 3, "FOUR" to 4, "FIVE" to 5,
        "SIX" to 6, "SEVEN" to 7, "EIGHT" to 8, "NINE" to 9, "TEN" to 10,
        "ELEVEN" to 11, "TWELVE" to 12, "THIRTEEN" to 13, "FOURTEEN" to 14,
        "FIFTEEN" to 15, "SIXTEEN" to 16, "SEVENTEEN" to 17, "EIGHTEEN" to 18
    )

    fun extractText(): String {
        val document = Loader.loadPDF(pdfFile)
        return document.use { doc ->
            val stripper = PDFTextStripper()
            stripper.sortByPosition = true
            stripper.getText(doc)
        }
    }

    fun parse(): Constitution {
        val text = extractText()

        // Extract preamble
        val preamble = extractPreamble(text)

        // Split into chapters
        val chapters = parseChapters(text)

        return Constitution(
            preamble = preamble,
            chapters = chapters
        )
    }

    private fun extractPreamble(text: String): String {
        val preambleStart = text.indexOf("PREAMBLE", ignoreCase = true)
        val chapterStart = text.indexOf("CHAPTER", ignoreCase = true)

        if (preambleStart == -1 || chapterStart == -1) return ""

        return text.substring(preambleStart + "PREAMBLE".length, chapterStart)
            .trim()
            .replace(Regex("""\s+"""), " ")
    }

    private fun parseChapters(text: String): List<ChapterData> {
        val chapters = mutableListOf<ChapterData>()

        // Find all chapter positions
        val chapterMatches = chapterPattern.findAll(text).toList()

        for ((index, match) in chapterMatches.withIndex()) {
            val chapterNumText = match.groupValues[1].trim().uppercase()
            val chapterNumber = romanNumerals[chapterNumText] ?: (index + 1)
            val chapterTitle = match.groupValues[2].trim()

            // Find the text for this chapter (until next chapter or end)
            val startPos = match.range.last + 1
            val endPos = if (index < chapterMatches.size - 1) {
                chapterMatches[index + 1].range.first
            } else {
                text.length
            }

            val chapterText = text.substring(startPos, endPos)

            // Parse articles in this chapter
            val articles = parseArticles(chapterText)

            chapters.add(ChapterData(
                number = chapterNumber,
                title = cleanTitle(chapterTitle),
                articles = articles
            ))
        }

        return chapters
    }

    private fun parseArticles(chapterText: String): List<ArticleData> {
        val articles = mutableListOf<ArticleData>()
        val lines = chapterText.lines()

        var currentArticleNumber: Int? = null
        var currentArticleTitle: String? = null
        var currentClauses = mutableListOf<ClauseData>()
        var currentClauseNumber: String? = null
        var currentClauseText = StringBuilder()
        var currentSubClauses = mutableListOf<SubClauseData>()

        fun saveCurrentClause() {
            if (currentClauseNumber != null && currentClauseText.isNotBlank()) {
                currentClauses.add(ClauseData(
                    number = currentClauseNumber!!,
                    text = currentClauseText.toString().trim(),
                    subClauses = currentSubClauses.toList()
                ))
                currentClauseNumber = null
                currentClauseText = StringBuilder()
                currentSubClauses = mutableListOf()
            }
        }

        fun saveCurrentArticle() {
            saveCurrentClause()
            if (currentArticleNumber != null && currentArticleTitle != null) {
                // If no clauses found, treat the whole text as one clause
                if (currentClauses.isEmpty() && currentClauseText.isNotBlank()) {
                    currentClauses.add(ClauseData(
                        number = "",
                        text = currentClauseText.toString().trim()
                    ))
                }
                articles.add(ArticleData(
                    number = currentArticleNumber!!,
                    title = currentArticleTitle!!,
                    clauses = currentClauses.toList()
                ))
                currentArticleNumber = null
                currentArticleTitle = null
                currentClauses = mutableListOf()
                currentClauseText = StringBuilder()
            }
        }

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isBlank()) continue

            // Check for new article
            val articleMatch = articlePattern.find(trimmedLine)
            if (articleMatch != null && isArticleStart(trimmedLine)) {
                saveCurrentArticle()
                currentArticleNumber = articleMatch.groupValues[1].toIntOrNull()
                currentArticleTitle = cleanTitle(articleMatch.groupValues[2])
                continue
            }

            // Check for clause
            val clauseMatch = clausePattern.find(trimmedLine)
            if (clauseMatch != null) {
                saveCurrentClause()
                currentClauseNumber = clauseMatch.groupValues[1]
                currentClauseText.append(clauseMatch.groupValues[2].trim())
                continue
            }

            // Check for sub-clause
            val subClauseMatch = subClausePattern.find(trimmedLine)
            if (subClauseMatch != null) {
                currentSubClauses.add(SubClauseData(
                    label = subClauseMatch.groupValues[1],
                    text = subClauseMatch.groupValues[2].trim()
                ))
                continue
            }

            // Continue building current clause text
            if (currentArticleNumber != null) {
                if (currentClauseText.isNotBlank()) {
                    currentClauseText.append(" ")
                }
                currentClauseText.append(trimmedLine)
            }
        }

        // Save any remaining article
        saveCurrentArticle()

        return articles
    }

    private fun isArticleStart(line: String): Boolean {
        // Check if this looks like an article header (number followed by title)
        val match = Regex("""^(\d+)\.\s+([A-Z])""").find(line)
        return match != null
    }

    private fun cleanTitle(title: String): String {
        return title
            .replace(Regex("""\s+"""), " ")
            .trim()
            .removeSuffix(".")
    }
}

