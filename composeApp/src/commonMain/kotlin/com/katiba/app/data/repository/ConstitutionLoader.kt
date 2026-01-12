package com.katiba.app.data.repository

import com.katiba.app.data.model.Constitution
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Loader for Constitution JSON data from resources.
 *
 * This object provides methods to load the constitution JSON file
 * that was parsed from the Constitution of Kenya PDF.
 */
object ConstitutionLoader {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Load constitution from embedded JSON resource.
     * This should be called once at app startup.
     *
     * @param jsonContent The JSON string content of the constitution
     */
    fun loadConstitution(jsonContent: String): Constitution {
        return json.decodeFromString<Constitution>(jsonContent)
    }

    /**
     * Initialize the ConstitutionRepository with JSON content.
     */
    fun initializeRepository(jsonContent: String) {
        ConstitutionRepository.loadFromJson(jsonContent)
    }
}

