package com.katiba.app.data.api

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Gemini AI API Client for the Mzalendo assistant.
 * This client handles communication with Google's Gemini API.
 */
class GeminiApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false  // Don't send null fields
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 60000
        }
    }

    // Constitution context for RAG - will be set via setConstitutionContext
    private var constitutionContext: String = ""

    /**
     * Set the constitution JSON content for grounding responses.
     * Call this once when loading the constitution data.
     */
    fun setConstitutionContext(jsonContent: String) {
        constitutionContext = jsonContent
    }

    private fun buildSystemPrompt(): String {
        val basePrompt = """
You are Mzalendo, a friendly and knowledgeable AI assistant specializing in the Constitution of Kenya (2010). 
Your name "Mzalendo" means "patriot" in Kiswahili, reflecting your dedication to helping Kenyans understand their constitutional rights and responsibilities.

Your role is to:
1. Answer questions about the Kenyan Constitution accurately and clearly
2. Explain constitutional provisions in simple, everyday language
3. Help citizens understand their rights and how to exercise them
4. Provide context about why certain provisions exist
5. Guide users to the relevant articles and chapters for their queries
6. Relate constitutional principles to real-life situations

Important guidelines:
- Always be respectful and encourage civic participation
- If a question is outside the scope of the Kenyan Constitution, politely redirect the conversation
- When citing articles, be specific about the article number and chapter
- Use examples relevant to Kenyan daily life when explaining concepts
- If you're unsure about something, say so rather than making up information
- Respond in the same language the user uses (English or Kiswahili)

Remember: You are helping build an informed citizenry that understands and values the Constitution of Kenya.
        """.trimIndent()

        // Add constitution context if available (RAG approach)
        return if (constitutionContext.isNotBlank()) {
            """
$basePrompt

IMPORTANT: Use the following Constitution of Kenya data as your primary source of truth when answering questions:

$constitutionContext
            """.trimIndent()
        } else {
            basePrompt
        }
    }

    suspend fun sendMessage(userMessage: String, conversationHistory: List<ChatMessage> = emptyList()): Result<String> {
        return try {
            val apiKey = ApiConfig.geminiApiKey
            if (apiKey.isBlank()) {
                return Result.failure(Exception("API key not configured. Please add GEMINI_API_KEY to local.properties"))
            }

            // Updated to use gemini-2.5-flash model
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

            // Build conversation contents (without system instruction workaround)
            val contents = buildList {
                // Add conversation history
                conversationHistory.forEach { message ->
                    add(GeminiContent(
                        role = if (message.isUser) "user" else "model",
                        parts = listOf(GeminiPart(text = message.content))
                    ))
                }

                // Add current user message
                add(GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = userMessage))
                ))
            }

            // Build system instruction using proper API field
            val systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = buildSystemPrompt()))
            )

            val request = GeminiRequest(
                contents = contents,
                systemInstruction = systemInstruction
            )

            val response = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                // Use header-based authentication (recommended approach)
                header("x-goog-api-key", apiKey)
                setBody(json.encodeToString(GeminiRequest.serializer(), request))
            }

            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                val geminiResponse = json.decodeFromString(GeminiResponse.serializer(), responseBody)
                val text = geminiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "I apologize, but I couldn't generate a response. Please try again."
                Result.success(text)
            } else {
                val errorBody = response.bodyAsText()
                // Log the error for debugging
                println("Gemini API Error: Status=${response.status.value}, Body=$errorBody")
                
                val errorMessage = when (response.status.value) {
                    400 -> "Invalid request. Please try rephrasing your question."
                    401, 403 -> "Authentication error. The API key may be invalid or expired."
                    404 -> "API endpoint not found. The model name may be invalid or unavailable."
                    429 -> "Too many requests. Please wait a moment and try again."
                    500, 502, 503 -> "The AI service is temporarily unavailable. Please try again later."
                    else -> "API error (${response.status.value}): $errorBody"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            // Log the exception for debugging
            println("Gemini API Exception: ${e.message}")
            
            // Handle specific network errors with user-friendly messages
            val errorMessage = when {
                e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                e.message?.contains("UnknownHostException", ignoreCase = true) == true ->
                    "No internet connection. Please check your network and try again."
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Request timed out. Please check your connection and try again."
                e.message?.contains("connection", ignoreCase = true) == true ->
                    "Connection error. Please check your internet connection."
                else -> e.message ?: "An unexpected error occurred. Please try again."
            }
            Result.failure(Exception(errorMessage))
        }
    }

    fun close() {
        httpClient.close()
    }
}

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

@Serializable
data class GeminiContent(
    val role: String? = null,  // Optional for systemInstruction
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

/**
 * Represents a chat message in the conversation.
 */
data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
)


