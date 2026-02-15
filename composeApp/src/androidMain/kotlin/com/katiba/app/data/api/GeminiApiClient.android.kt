package com.katiba.app.data.api

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content

actual class GeminiApiClient actual constructor() {

    private var constitutionContext: String = ""

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

    private fun buildModel() = Firebase
        .ai(backend = GenerativeBackend.googleAI())
        .generativeModel(
            modelName = "gemini-2.5-flash",
            systemInstruction = content { text(buildSystemPrompt()) }
        )

    actual fun setConstitutionContext(jsonContent: String) {
        constitutionContext = jsonContent
    }

    actual suspend fun sendMessage(
        userMessage: String,
        conversationHistory: List<ChatMessage>
    ): Result<String> {
        return try {
            val model = buildModel()

            val history = conversationHistory.map { message ->
                content(role = if (message.isUser) "user" else "model") {
                    text(message.content)
                }
            }

            val chat = model.startChat(history)
            val response = chat.sendMessage(userMessage)

            val text = response.text
            if (text != null) {
                Result.success(text)
            } else {
                Result.success("I apologize, but I couldn't generate a response. Please try again.")
            }
        } catch (e: Exception) {
            println("Gemini API Exception: ${e.message}")
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

    actual fun close() {
        // No resources to release with Firebase AI SDK
    }
}
