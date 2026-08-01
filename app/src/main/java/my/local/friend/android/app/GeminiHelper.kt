package my.local.friend.android.app

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.serialization.json.Json
import my.local.friend.android.app.data.models.GeminiChatResponse

// --- 1. DATA MODELS ---
data class TutorResponse(
    val feedback: String = "",
    val targetText: String = "",
    val nativeText: String = "",
    val vocabulary: String = ""
)

data class UIChatMessage(
    val role: String, // "user" or "assistant"
    val content: String,
    val parsedResponse: TutorResponse? = null,
    val jsonResponse: GeminiChatResponse? = null
)

// --- 2. PARSER FUNCTION ---
fun parseGeminiResponse(rawText: String): TutorResponse {
    var feedback = ""
    var targetText = ""
    var nativeText = ""
    var vocabulary = ""

    if (rawText.contains("### FEEDBACK") && rawText.contains("### NEWS_TARGET")) {
        val parts = rawText.split("### ")
        for (part in parts) {
            when {
                part.startsWith("FEEDBACK") -> feedback = part.removePrefix("FEEDBACK").trim()
                part.startsWith("NEWS_TARGET") -> targetText = part.removePrefix("NEWS_TARGET").trim()
                part.startsWith("NEWS_NATIVE") -> nativeText = part.removePrefix("NEWS_NATIVE").trim()
                part.startsWith("VOCABULARY") -> vocabulary = part.removePrefix("VOCABULARY").trim()
            }
        }
    } else {
        targetText = rawText
    }

    return TutorResponse(feedback, targetText, nativeText, vocabulary)
}

/**
 * Parses the structured JSON response from Gemini into a [GeminiChatResponse] object.
 */
fun parseGeminiJsonResponse(jsonString: String): GeminiChatResponse {
    return try {
        Json.decodeFromString<GeminiChatResponse>(jsonString)
    } catch (e: Exception) {
        // Fallback or error handling
        GeminiChatResponse(
            reply = "Error parsing response: ${e.message}",
            errorCount = 0,
            corrections = emptyList(),
            topics = emptyList()
        )
    }
}

// --- 3. GEMINI SERVICE ---
class GeminiHelper {

    // ⚠️ Replace with your actual Gemini API key
    private val apiKey = BuildConfig.GEMINI_API_KEY

    fun getModel(
        area: String,
        nativeLang: String,
        targetLang: String,
        level: String,
        topics: String
    ): GenerativeModel {

        val systemInstruction = """
            Your name is Thomas. You are a friendly language tutor and local news expert for $area.
            The user wants to learn $targetLang and their current proficiency level is '$level'.
            The user's native language is $nativeLang.
            Always talk about topics related to: $topics.

            === CRITICAL RULE ===
            You MUST respond ONLY with a valid JSON object. Do not include any text outside the JSON.
            The JSON structure MUST follow this schema:
            {
              "reply": "Your response to the user in '$targetLang'. This should include feedback on their previous message in '$nativeLang', followed by localized news or conversation in '$targetLang'.",
              "errorCount": 0, // Number of linguistic errors found in the user's last message
              "corrections": ["Correction 1", "Correction 2"], // Specific linguistic corrections in '$nativeLang'
              "topics": ["topic1", "topic2"] // List of relevant topics discussed
            }

            === LANGUAGE RULES ===
            1. Provide feedback and corrections in $nativeLang.
            2. The main conversation 'reply' should be primarily in $targetLang, calibrated to the '$level' level.
        """.trimIndent()

        return GenerativeModel(
            modelName = "gemini-3.1-flash-lite",
            apiKey = apiKey,
            systemInstruction = content { text(systemInstruction) },
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )
    }
}