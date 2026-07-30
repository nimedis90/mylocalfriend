package my.local.friend.android.app.data

data class UserPreferences(
    val nativeLang: String = "English",
    val targetLang: String = "Italiano",
    val targetLevel: String = "A1",
    val targetArea: String = "London",
    val favoriteTopics: String = "Soccer & Sports",
    val isOnboarded: Boolean = false
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "nativeLang" to nativeLang,
            "targetLang" to targetLang,
            "targetLevel" to targetLevel,
            "targetArea" to targetArea,
            "favoriteTopics" to favoriteTopics,
            "isOnboarded" to isOnboarded
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): UserPreferences {
            if (map == null) return UserPreferences()
            return UserPreferences(
                nativeLang = map["nativeLang"] as? String ?: "English",
                targetLang = map["targetLang"] as? String ?: "Italiano",
                targetLevel = map["targetLevel"] as? String ?: "A1",
                targetArea = map["targetArea"] as? String ?: "London",
                favoriteTopics = map["favoriteTopics"] as? String ?: "Soccer & Sports",
                isOnboarded = map["isOnboarded"] as? Boolean ?: false
            )
        }
    }
}
