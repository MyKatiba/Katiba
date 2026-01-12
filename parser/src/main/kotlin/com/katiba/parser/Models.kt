package com.katiba.parser

import kotlinx.serialization.Serializable

/**
 * Data models matching the app's model structure for YAML serialization
 */

@Serializable
data class Constitution(
    val preamble: String = "",
    val chapters: List<ChapterData>
)

@Serializable
data class ChapterData(
    val number: Int,
    val title: String,
    val parts: List<PartData> = emptyList(),
    val articles: List<ArticleData> = emptyList()
)

@Serializable
data class PartData(
    val number: Int,
    val title: String,
    val articles: List<ArticleData>
)

@Serializable
data class ArticleData(
    val number: Int,
    val title: String,
    val clauses: List<ClauseData>
)

@Serializable
data class ClauseData(
    val number: String,
    val text: String,
    val subClauses: List<SubClauseData> = emptyList()
)

@Serializable
data class SubClauseData(
    val label: String,
    val text: String
)

