package com.katiba.app.data.api

/**
 * API Configuration - API keys can be injected at build time or use embedded fallback.
 * 
 * Priority:
 * 1. Key from local.properties (for developers who want to use their own key)
 * 2. Embedded obfuscated key (for end users cloning the repo)
 */
expect fun getGeminiApiKey(): String

object ApiConfig {
    val geminiApiKey: String by lazy { 
        val buildConfigKey = getGeminiApiKey()
        if (buildConfigKey.isNotBlank()) {
            buildConfigKey
        } else {
            // Fallback to embedded key for users who clone the repo
            ApiKeyProvider.getKey()
        }
    }
}

