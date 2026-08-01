package my.local.friend.android.app.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import my.local.friend.android.app.data.models.ChatMessage

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun saveChatMessage(userId: String, message: ChatMessage): Result<Unit> {
        return try {
            Log.d("UserRepository", "Saving message: ${message.id} for user: $userId")
            usersCollection
                .document(userId)
                .collection("messages")
                .document(message.id)
                .set(message.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error saving message", e)
            Result.failure(e)
        }
    }

    suspend fun getAllMessages(userId: String): Result<List<ChatMessage>> {
        return try {
            Log.d("UserRepository", "Fetching all messages for user: $userId")
            val snapshot = usersCollection
                .document(userId)
                .collection("messages")
                .orderBy("timestamp")
                .get()
                .await()
            
            Log.d("UserRepository", "Found ${snapshot.size()} messages")
            
            val messages = snapshot.documents.mapNotNull { doc ->
                try {
                    ChatMessage.fromMap(doc.data ?: emptyMap())
                } catch (e: Exception) {
                    Log.e("UserRepository", "Error mapping document ${doc.id}", e)
                    null
                }
            }
            Result.success(messages)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching messages", e)
            Result.failure(e)
        }
    }

    suspend fun savePreferences(userId: String, preferences: UserPreferences): Result<Unit> {
        return try {
            usersCollection.document(userId).set(preferences.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPreferences(userId: String): Result<UserPreferences?> {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            if (snapshot.exists()) {
                Result.success(UserPreferences.fromMap(snapshot.data))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
