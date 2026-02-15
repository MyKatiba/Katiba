package com.katiba.app.data.api

/**
 * Represents a chat message in the conversation.
 */
data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
)

/**
 * Gemini AI API Client for the Mzalendo assistant.
 * Platform-specific implementations handle the actual AI communication.
 */
expect class GeminiApiClient() {
    fun setConstitutionContext(jsonContent: String)
    suspend fun sendMessage(userMessage: String, conversationHistory: List<ChatMessage> = emptyList()): Result<String>
    fun close()
}
