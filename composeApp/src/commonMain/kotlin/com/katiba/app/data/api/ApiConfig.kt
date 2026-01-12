package com.katiba.app.data.api

/**
 * API Configuration - API keys are injected at build time.
 * The actual key is stored in local.properties and should not be committed to version control.
 */
expect fun getGeminiApiKey(): String

object ApiConfig {
    val geminiApiKey: String by lazy { getGeminiApiKey() }
}

