package my.local.friend.android.app

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import my.local.friend.android.app.data.UserPreferences
import my.local.friend.android.app.data.UserRepository

class DailyUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("DailyUpdateWorker", "Starting daily update work")
        
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return Result.success() // No user, no update

        val userRepository = UserRepository()
        val prefsResult = userRepository.getPreferences(userId)
        
        if (prefsResult.isFailure) {
            Log.e("DailyUpdateWorker", "Failed to fetch preferences", prefsResult.exceptionOrNull())
            return Result.retry()
        }

        val prefs = prefsResult.getOrNull() ?: UserPreferences()
        
        val geminiHelper = GeminiHelper()
        val model = geminiHelper.getModel(
            area = prefs.targetArea,
            nativeLang = prefs.nativeLang,
            targetLang = prefs.targetLang,
            level = prefs.targetLevel,
            topics = prefs.favoriteTopics
        )

        val prompt = "Give me a very short, catchy 1-sentence news headline and a 1-sentence summary of today's interesting events and weather in ${prefs.targetArea} related to ${prefs.favoriteTopics} and find some related activities in the same area or close areas. Format it as: HEADLINE: [headline] SUMMARY: [summary]. Use the user's target language (${prefs.targetLang}) for this summary."

        return try {
            val response = model.generateContent(prompt)
            val text = response.text ?: ""
            
            val headline = text.substringAfter("HEADLINE:").substringBefore("SUMMARY:").trim()
            val summary = text.substringAfter("SUMMARY:").trim()

            if (headline.isNotEmpty() && summary.isNotEmpty()) {
                val notificationHelper = NotificationHelper(applicationContext)
                notificationHelper.showNotification(headline, summary)
            } else {
                // Fallback if parsing fails
                val notificationHelper = NotificationHelper(applicationContext)
                notificationHelper.showNotification("Daily Local Update", text.take(100))
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("DailyUpdateWorker", "Error generating Gemini content", e)
            Result.retry()
        }
    }
}
