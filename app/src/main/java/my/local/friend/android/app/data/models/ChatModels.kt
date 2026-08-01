package my.local.friend.android.app.data.models

import kotlinx.serialization.Serializable

/**
 * Represents a single chat message with linguistic analysis for gamification.
 */
@Serializable
data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long,
    val errorCount: Int = 0,
    val corrections: List<String> = emptyList(),
    val topics: List<String> = emptyList()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "text" to text,
            "isUser" to isUser,
            "timestamp" to timestamp,
            "errorCount" to errorCount,
            "corrections" to corrections,
            "topics" to topics
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): ChatMessage {
            return ChatMessage(
                id = map["id"] as? String ?: "",
                text = map["text"] as? String ?: "",
                isUser = map["isUser"] as? Boolean ?: false,
                timestamp = (map["timestamp"] as? Long) ?: 0L,
                errorCount = (map["errorCount"] as? Long)?.toInt() ?: 0,
                corrections = (map["corrections"] as? List<String>) ?: emptyList(),
                topics = (map["topics"] as? List<String>) ?: emptyList()
            )
        }
    }
}

/**
 * Data model for parsing Gemini's JSON response containing learning progress analysis.
 */
@Serializable
data class ProgressResponse(
    val summary: String,
    val recommendations: List<String>
)

/**
 * Data model for parsing Gemini's JSON response containing linguistic analysis.
 */
@Serializable
data class GeminiChatResponse(
    val reply: String,
    val errorCount: Int,
    val corrections: List<String>,
    val topics: List<String>,
    val vocabulary: String = "",
    val translation: String = ""
)
