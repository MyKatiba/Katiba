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
    
    // Backend API base URL
    // TODO: Update with production URL when available
    const val BASE_URL = "http://10.0.2.2:3000" // Android emulator localhost
    // For iOS simulator, use: "http://localhost:3000"
    // For production: "https://api.katiba.app"
}

