package my.local.friend.android.app

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

// --- 1. DATA MODELS ---
data class TutorResponse(
    val feedback: String = "",
    val targetText: String = "",
    val nativeText: String = "",
    val vocabulary: String = ""
)

data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String,
    val parsedResponse: TutorResponse? = null
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
            CRITICAL RULE: The target language to teach is strictly '$targetLang'.
            The user's native language is $nativeLang.
            The user wants to learn $targetLang and their current proficiency level is '$level'.
            Always talk about topics related to: $topics.

            === LANGUAGE RULES ===
            1. FEEDBACK must be in $nativeLang.
            2. NEWS_TARGET MUST BE EXCLUSIVELY WRITTEN IN $targetLang. Do NOT use $nativeLang here!
            3. NEWS_NATIVE must be the direct translation of NEWS_TARGET into $nativeLang.
            4. VOCABULARY items must pair $targetLang words with $nativeLang translations.

            === RESPONSE STRUCTURE ===
            For EVERY message, you MUST format your output using these exact section headers:

            ### FEEDBACK
            Provide gentle grammar/spelling feedback on the user's last message in $nativeLang.
            (If it's the very first message, write a warm welcome in $nativeLang introducing yourself as Thomas).

            ### NEWS_TARGET
            Write the news summary, conversation continuation, and question in $targetLang, strictly calibrated to the '$level' level.

            ### NEWS_NATIVE
            Provide the exact full translation of the 'NEWS_TARGET' section into $nativeLang.

            ### VOCABULARY
            Extract key learning materials in $nativeLang:
            - 3-4 Key nouns/words from the text with translation.
            - 2-3 Useful verbs used in the text.
            - 1 Common idiom/expression.
            - IF proficiency level is 'A1' or 'A2', ADD 1-2 basic grammar tips.
        """.trimIndent()

        return GenerativeModel(
            modelName = "gemini-3.1-flash-lite",
            apiKey = apiKey,
            systemInstruction = content { text(systemInstruction) }
        )
    }
}