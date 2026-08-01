package my.local.friend.android.app

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.Chat
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import my.local.friend.android.app.data.AuthRepository
import my.local.friend.android.app.data.UserPreferences
import my.local.friend.android.app.data.UserRepository
import my.local.friend.android.app.data.models.ChatMessage as DataChatMessage
import java.util.UUID

class TutorViewModel : ViewModel() {

    private val geminiHelper = GeminiHelper()
    private var chatInstance: Chat? = null
    
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    // --- AUTH STATE ---
    var currentUser by mutableStateOf<FirebaseUser?>(authRepository.currentUser)
    var isAuthLoading by mutableStateOf(false)

    // --- CONFIGURATION STATE ---
    var userPrefs by mutableStateOf(UserPreferences())
    var isPrefsLoading by mutableStateOf(false)

    // --- CHAT & UI STATE ---
    val messages = mutableStateListOf<UIChatMessage>()
    var isLoading by mutableStateOf(false)
    var isLessonStarted by mutableStateOf(false)
    var lastVocabulary by mutableStateOf("")

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.observeAuthState().collectLatest { user ->
                currentUser = user
                Log.d("TutorViewModel", "Auth state changed: user=${user?.email}")
                if (user != null) {
                    loadUserPreferences(user.uid)
                } else {
                    userPrefs = UserPreferences()
                    isLessonStarted = false
                    messages.clear()
                }
            }
        }
    }

    private fun loadUserPreferences(userId: String) {
        viewModelScope.launch {
            isPrefsLoading = true
            Log.d("TutorViewModel", "Loading preferences for $userId")
            val result = userRepository.getPreferences(userId)
            if (result.isSuccess) {
                val prefs = result.getOrNull() ?: UserPreferences()
                Log.d("TutorViewModel", "Preferences loaded: $prefs")
                userPrefs = prefs
            } else {
                Log.e("TutorViewModel", "Error loading preferences", result.exceptionOrNull())
            }
            isPrefsLoading = false
        }
    }

    fun saveUserPreferences(prefs: UserPreferences) {
        val userId = currentUser?.uid ?: run {
            Log.e("TutorViewModel", "Cannot save preferences: User is null")
            return
        }
        viewModelScope.launch {
            isPrefsLoading = true
            Log.d("TutorViewModel", "Saving preferences for $userId: $prefs")
            val result = userRepository.savePreferences(userId, prefs)
            if (result.isSuccess) {
                Log.d("TutorViewModel", "Preferences saved successfully")
                userPrefs = prefs
            } else {
                Log.e("TutorViewModel", "Error saving preferences", result.exceptionOrNull())
            }
            isPrefsLoading = false
        }
    }

    suspend fun signIn(email: String, pass: String): Result<FirebaseUser?> {
        isAuthLoading = true
        val result = authRepository.signIn(email, pass)
        isAuthLoading = false
        return result
    }

    suspend fun signUp(email: String, pass: String): Result<FirebaseUser?> {
        isAuthLoading = true
        val result = authRepository.signUp(email, pass)
        isAuthLoading = false
        return result
    }

    fun signOut() {
        authRepository.signOut()
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return authRepository.sendPasswordResetEmail(email)
    }

    // --- START A LESSON ---
    fun startLesson() {
        viewModelScope.launch {
            isLoading = true
            messages.clear()
            lastVocabulary = ""

            val model = geminiHelper.getModel(
                area = userPrefs.targetArea,
                nativeLang = userPrefs.nativeLang,
                targetLang = userPrefs.targetLang,
                level = userPrefs.targetLevel,
                topics = userPrefs.favoriteTopics
            )

            // Initialize chat
            chatInstance = model.startChat()

            val initialPrompt = "Search for the latest, most interesting or relevant news today/this week about ${userPrefs.targetArea} " +
                    "specifically related to these topics: ${userPrefs.favoriteTopics}. " +
                    "Choose one news item and generate the response following the exact 4 sections (FEEDBACK, NEWS_TARGET, NEWS_NATIVE, VOCABULARY)."

            try {
                val response = model.generateContent(initialPrompt)
                val responseText = response.text ?: ""
                val jsonResponse = parseGeminiJsonResponse(responseText)
                
                // Map to existing UI model for compatibility
                val parsed = TutorResponse(
                    feedback = jsonResponse.corrections.joinToString("\n"),
                    targetText = jsonResponse.reply
                )

                val assistantMessage = UIChatMessage(
                    role = "assistant",
                    content = responseText,
                    parsedResponse = parsed,
                    jsonResponse = jsonResponse
                )
                messages.add(assistantMessage)

                // Persist Assistant message
                currentUser?.uid?.let { uid ->
                    userRepository.saveChatMessage(
                        uid,
                        DataChatMessage(
                            id = UUID.randomUUID().toString(),
                            text = jsonResponse.reply,
                            isUser = false,
                            timestamp = System.currentTimeMillis(),
                            errorCount = jsonResponse.errorCount,
                            corrections = jsonResponse.corrections,
                            topics = jsonResponse.topics
                        )
                    )
                }

                isLessonStarted = true
            } catch (e: Exception) {
                messages.add(
                    UIChatMessage(
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

        val userMessage = UIChatMessage(role = "user", content = userText)
        messages.add(userMessage)

        val uid = currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                userRepository.saveChatMessage(
                    uid,
                    DataChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = userText,
                        isUser = true,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }

        viewModelScope.launch {
            isLoading = true
            try {
                val response = chatInstance?.sendMessage(userText)
                val responseText = response?.text ?: ""
                val jsonResponse = parseGeminiJsonResponse(responseText)

                // Map to existing UI model for compatibility
                val parsed = TutorResponse(
                    feedback = jsonResponse.corrections.joinToString("\n"),
                    targetText = jsonResponse.reply
                )

                val assistantMessage = UIChatMessage(
                    role = "assistant",
                    content = responseText,
                    parsedResponse = parsed,
                    jsonResponse = jsonResponse
                )
                messages.add(assistantMessage)

                // Persist Assistant response
                if (uid != null) {
                    userRepository.saveChatMessage(
                        uid,
                        DataChatMessage(
                            id = UUID.randomUUID().toString(),
                            text = jsonResponse.reply,
                            isUser = false,
                            timestamp = System.currentTimeMillis(),
                            errorCount = jsonResponse.errorCount,
                            corrections = jsonResponse.corrections,
                            topics = jsonResponse.topics
                        )
                    )
                }
            } catch (e: Exception) {
                messages.add(
                    UIChatMessage(
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
