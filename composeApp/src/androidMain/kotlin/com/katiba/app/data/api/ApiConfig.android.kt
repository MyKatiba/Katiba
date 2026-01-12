package com.katiba.app.data.api

import com.katiba.app.BuildConfig

/**
 * Android implementation of getGeminiApiKey.
 * Reads the Gemini API key from BuildConfig (injected at build time from local.properties).
 */
actual fun getGeminiApiKey(): String = BuildConfig.GEMINI_API_KEY

