package com.katiba.app.data

import com.katiba.app.data.api.ChatMessage

/**
 * Represents a single chat session with the Mzalendo AI.
 */
data class ChatSession(
    val id: String,
    val title: String,
    val messages: List<ChatMessage>,
    val timestamp: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
)

/**
 * In-memory store for chat history sessions.
 * Persists across screen navigations within the same app session.
 */
object ChatHistoryStore {
    private val sessions = mutableListOf<ChatSession>()
    private var nextId = 1

    fun saveChatSession(messages: List<ChatMessage>): ChatSession? {
        // Only save if there are actual messages
        val userMessages = messages.filter { it.isUser }
        if (userMessages.isEmpty()) return null

        // Auto-generate title from first user message
        val title = userMessages.first().content.take(50).let {
            if (it.length >= 50) "$it..." else it
        }

        val session = ChatSession(
            id = "chat_${nextId++}",
            title = title,
            messages = messages.toList()
        )
        sessions.add(0, session) // Most recent first
        return session
    }

    fun getChatSessions(): List<ChatSession> = sessions.toList()

    fun loadChatSession(id: String): ChatSession? = sessions.find { it.id == id }

    fun deleteChatSession(id: String) {
        sessions.removeAll { it.id == id }
    }

    fun updateChatSession(id: String, messages: List<ChatMessage>) {
        val index = sessions.indexOfFirst { it.id == id }
        if (index >= 0) {
            val existing = sessions[index]
            sessions[index] = existing.copy(
                messages = messages.toList(),
                timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            )
        }
    }
}
