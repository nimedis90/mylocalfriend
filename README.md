Here is a complete, polished `README.md` file tailored for your GitHub repository. It clearly explains what the app does, the tech stack used, features, setup instructions, and architecture.

---

# 🧑‍🏫 Thomas - AI News Language Tutor

**Thomas** is an interactive Android application built with **Jetpack Compose** and powered by **Google Gemini** (`gemini-3.1-flash-lite`). It helps users learn a target language through real-time, localized news updates calibrated to their specific proficiency level.

---

## 🌟 Key Features

* **Localized News Learning**: Fetches and translates current news tailored to a selected city or region and topics of interest.
* **Level Calibration**: Formats news content, vocabulary, and conversation according to CEFR language levels (e.g., A1, A2, B1, etc.).
* **Bilingual UI Feed**: Toggle effortlessly between the **Target Language** text and **Native Language** translation.
* **Smart Cheat Sheet**: Dynamically extracts key nouns, useful verbs, idioms, and grammar tips into an on-screen reference card.
* **Grammar & Feedback**: Provides immediate feedback in your native language on your target language responses.

---

## 🛠 Tech Stack & Architecture

* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3 Design)
* **Language**: [Kotlin](https://kotlinlang.org/)
* **AI Model**: Google Gemini API (`gemini-3.1-flash-lite`) via `com.google.ai.client.generativeai`
* **Architecture**: MVVM (Model-View-ViewModel) using `ViewModel` and Kotlin Coroutines/State for asynchronous operations.

---

## 📂 Project Structure

```
app/src/main/java/com/example/thomaslanguagetutor/
├── MainActivity.kt        # Jetpack Compose UI (Drawer, Feed, Input, Tabs)
├── TutorViewModel.kt      # ViewModel for managing app state & Gemini requests
└── GeminiHelper.kt        # Gemini API client configuration & response parsing

```

---

## 🚀 Getting Started

### Prerequisites

* **Android Studio** (Jellyfish / Koala or newer recommended)
* Android SDK **API level 24** (Android 7.0) or higher
* A **Google Gemini API Key** (Get one at [Google AI Studio](https://aistudio.google.com/))

### Installation & Setup

1. **Clone the repository**:
```bash
git clone https://github.com/YOUR_USERNAME/Thomas-AI-Language-Tutor.git
cd Thomas-AI-Language-Tutor

```


2. **Open in Android Studio**:
Open Android Studio and choose **Open**, then select the project directory.
3. **Configure your API Key**:
Open `GeminiHelper.kt` and replace `"YOUR_API_KEY_HERE"` with your actual Gemini API key:
```kotlin
private val apiKey = "YOUR_GEMINI_API_KEY"

```


4. **Run the App**:
Select an Emulator or a physical device connected via USB with **USB Debugging** enabled, and press **Run ▶**.

---

## 📱 How to Use

1. Tap the **⚙️ Open Preferences Menu** (or the top-left ☰ icon) on startup.
2. Enter your **Native Language**, **Target Language**, target **City/Region**, and **Topics of Interest**.
3. Tap **🚀 Start News Lesson**.
4. Read the news item in your target language or switch to the native language tab for help.
5. Use the **Smart Cheat Sheet** card at the top to review new vocabulary.
6. Type a response in the chat bar to practice your conversation skills and receive feedback!

---

## 📜 License

This project is open-source and available under the [MIT License](https://www.google.com/search?q=LICENSE).
