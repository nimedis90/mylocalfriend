package com.example.mylocalfriend

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

class TutorViewModel : ViewModel() {

    private val geminiHelper = GeminiHelper()
    private var chatInstance: Chat? = null

    // --- CONFIGURATION STATE ---
    var nativeLang by mutableStateOf("English")
    var targetLang by mutableStateOf("Italiano")
    var selectedLevel by mutableStateOf("A1")
    var targetArea by mutableStateOf("London")
    var selectedTopics by mutableStateOf("Soccer & Sports")

    // --- CHAT & UI STATE ---
    val messages = mutableStateListOf<ChatMessage>()
    var isLoading by mutableStateOf(false)
    var isLessonStarted by mutableStateOf(false)
    var lastVocabulary by mutableStateOf("")

    // --- START A LESSON ---
    fun startLesson() {
        viewModelScope.launch {
            isLoading = true
            messages.clear()
            lastVocabulary = ""

            val model = geminiHelper.getModel(
                area = targetArea,
                nativeLang = nativeLang,
                targetLang = targetLang,
                level = selectedLevel,
                topics = selectedTopics
            )

            // Initialize chat
            chatInstance = model.startChat()

            val initialPrompt = "Search for the latest, most interesting or relevant news today/this week about $targetArea " +
                    "specifically related to these topics: $selectedTopics. " +
                    "Choose one news item and generate the response following the exact 4 sections (FEEDBACK, NEWS_TARGET, NEWS_NATIVE, VOCABULARY)."

            try {
                val response = model.generateContent(initialPrompt)
                val responseText = response.text ?: ""
                val parsed = parseGeminiResponse(responseText)

                if (parsed.vocabulary.isNotEmpty()) {
                    lastVocabulary = parsed.vocabulary
                }

                messages.add(
                    ChatMessage(
                        role = "assistant",
                        content = responseText,
                        parsedResponse = parsed
                    )
                )
                isLessonStarted = true
            } catch (e: Exception) {
                messages.add(
                    ChatMessage(
                        role = "assistant",
                        content = "Error starting lesson: ${e.localizedMessage}"
                    )
                )
            } finally {
                isLoading = false
            }
        }
    }

    // --- SEND USER MESSAGE ---
    fun sendMessage(userText: String) {
        if (userText.isBlank() || chatInstance == null) return

        messages.add(ChatMessage(role = "user", content = userText))

        viewModelScope.launch {
            isLoading = true
            try {
                val response = chatInstance?.sendMessage(userText)
                val responseText = response?.text ?: ""
                val parsed = parseGeminiResponse(responseText)

                if (parsed.vocabulary.isNotEmpty()) {
                    lastVocabulary = parsed.vocabulary
                }

                messages.add(
                    ChatMessage(
                        role = "assistant",
                        content = responseText,
                        parsedResponse = parsed
                    )
                )
            } catch (e: Exception) {
                messages.add(
                    ChatMessage(
                        role = "assistant",
                        content = "Error: ${e.localizedMessage}"
                    )
                )
            } finally {
                isLoading = false
            }
        }
    }
}