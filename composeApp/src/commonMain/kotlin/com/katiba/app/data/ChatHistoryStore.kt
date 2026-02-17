package com.katiba.app.data

import com.katiba.app.data.api.ChatMessage
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Represents a single chat session with the Mzalendo AI.
 */
@Serializable
data class ChatSession(
    val id: String,
    val title: String,
    val messages: List<ChatMessage>,
    val timestamp: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
)

/**
 * Persistent store for chat history sessions.
 * Data is saved using multiplatform-settings and persists across app sessions.
 */
object ChatHistoryStore {
    private val settings: Settings = Settings()
    private val json = Json { ignoreUnknownKeys = true }
    
    private const val KEY_CHAT_SESSIONS = "chat_sessions"
    private const val KEY_NEXT_ID = "chat_next_id"
    
    private val sessions: MutableList<ChatSession> = loadSessions()
    private var nextId: Int = settings[KEY_NEXT_ID, 1]
    
    private fun loadSessions(): MutableList<ChatSession> {
        val sessionsJson = settings.getStringOrNull(KEY_CHAT_SESSIONS) ?: return mutableListOf()
        return try {
            json.decodeFromString<List<ChatSession>>(sessionsJson).toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
    }
    
    private fun saveSessions() {
        settings[KEY_CHAT_SESSIONS] = json.encodeToString(sessions.toList())
        settings[KEY_NEXT_ID] = nextId
    }

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
        saveSessions()
        return session
    }

    fun getChatSessions(): List<ChatSession> = sessions.toList()

    fun loadChatSession(id: String): ChatSession? = sessions.find { it.id == id }

    fun deleteChatSession(id: String) {
        sessions.removeAll { it.id == id }
        saveSessions()
    }

    fun updateChatSession(id: String, messages: List<ChatMessage>) {
        val index = sessions.indexOfFirst { it.id == id }
        if (index >= 0) {
            val existing = sessions[index]
            sessions[index] = existing.copy(
                messages = messages.toList(),
                timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            )
            saveSessions()
        }
    }
    
    /**
     * Clear all chat history (for sign out).
     */
    fun clearAll() {
        sessions.clear()
        nextId = 1
        settings.remove(KEY_CHAT_SESSIONS)
        settings.remove(KEY_NEXT_ID)
    }
}
