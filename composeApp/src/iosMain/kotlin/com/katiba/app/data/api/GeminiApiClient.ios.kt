package com.katiba.app.data.api

actual class GeminiApiClient actual constructor() {

    actual fun setConstitutionContext(jsonContent: String) {
        // No-op on iOS
    }

    actual suspend fun sendMessage(
        userMessage: String,
        conversationHistory: List<ChatMessage>
    ): Result<String> {
        return Result.failure(Exception("AI assistant is not yet available on iOS. Please use the Android app."))
    }

    actual fun close() {
        // No-op on iOS
    }
}
