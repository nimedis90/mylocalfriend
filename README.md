# 🧑‍🏫 Thomas - AI News Language Tutor

**Thomas** is an interactive Android application built with **Jetpack Compose** and powered by **Google Gemini** (`gemini-3.1-flash-lite`). It helps users learn a target language through real-time, localized news updates calibrated to their specific proficiency level.

---

## 🌟 Key Features

* **Localized News Learning**: Fetches and translates current news tailored to a selected city or region and topics of interest.
* **Secure Authentication**: User accounts with Login, Sign Up, and Password Reset functionality via Firebase.
* **Persistent Profiles**: User preferences (languages, interests, level) are stored securely in Firestore.
* **Bilingual UI Feed**: Toggle effortlessly between the **Target Language** text and **Native Language** translation.
* **Daily Push Notifications**: Receive a fresh summary of local weather and events every morning at 9:00 AM, powered by Gemini.
* **Smart Cheat Sheet**: Dynamically extracts key nouns, useful verbs, idioms, and grammar tips.

---

## 🛠 Tech Stack

* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
* **Backend**: [Firebase](https://firebase.google.com/) (Auth & Firestore)
* **Background Processing**: [Android WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for daily scheduled notifications.
* **AI Model**: Google Gemini API (`gemini-3.1-flash-lite`)
* **Architecture**: MVVM with Kotlin Coroutines & Flow.

---

## 🚀 Installation & Setup

> [!IMPORTANT]
> For security reasons, API keys and Firebase configuration files are **not** included in this repository. You must add them manually for the app to build and run.

### 1. Configure Gemini API Key
1. Get a free API Key at [Google AI Studio](https://aistudio.google.com/).
2. In the root directory of this project, open (or create) the `local.properties` file.
3. Add the following line:
   ```properties
   GEMINI_API_KEY=your_actual_key_here
   ```

### 2. Configure Firebase (google-services.json)
This app requires Firebase for authentication and database storage.
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create a new project named **My local friend**.
3. Add an **Android App** to the project:
   - Package name: `my.local.friend.android.app`
4. Download the `google-services.json` file.
5. **Move the file** into the `app/` folder of this project:
   ```text
   Mylocalfriend/
   └── app/
       └── google-services.json  <-- Place it here
   ```
6. In the Firebase Console, enable the following services:
   - **Authentication**: Enable the "Email/Password" sign-in method.
   - **Firestore Database**: Create a database in "Production" or "Test" mode and add these rules:
     ```
     rules_version = '2';
     service cloud.firestore {
       match /databases/{database}/documents {
         match /users/{userId} {
           allow read, write: if request.auth != null && request.auth.uid == userId;
         }
       }
     }
     ```

---

## 📱 How to Use

1. **Sign Up**: Create an account with your email.
2. **Onboarding**: Complete the one-time setup (Mother tongue, Target language, etc.).
3. **Practice**: Thomas will fetch news based on your profile.
4. **Daily Updates**: Stay informed with a notification every morning at 9:00 AM containing local news in your target language.
5. **Update Profile**: Tap the menu (☰) icon to change your location, topics, or level anytime. Tap "Save & Restart" to refresh the lesson.

---

## 📜 License

This project is open-source and available under the [MIT License](https://opensource.org/licenses/MIT).
