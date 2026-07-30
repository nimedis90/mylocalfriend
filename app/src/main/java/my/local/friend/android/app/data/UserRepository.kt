package my.local.friend.android.app.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

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
