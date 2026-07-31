package my.local.friend.android.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import my.local.friend.android.app.data.UserPreferences

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun OnboardingScreen(
    onComplete: (UserPreferences) -> Unit,
    isLoading: Boolean = false
) {
    var nativeLang by remember { mutableStateOf("") }
    var targetLang by remember { mutableStateOf("") }
    var targetArea by remember { mutableStateOf("") }
    var selectedTopics by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Tell us about yourself", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("We'll customize your learning experience", color = MaterialTheme.colorScheme.secondary)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = nativeLang,
            onValueChange = { nativeLang = it },
            label = { Text("What is your mother tongue?") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = targetLang,
            onValueChange = { targetLang = it },
            label = { Text("What language do you want to learn?") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = targetArea,
            onValueChange = { targetArea = it },
            label = { Text("Which city/region are you interested in?") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = selectedTopics,
            onValueChange = { selectedTopics = it },
            label = { Text("What topics do you like? (e.g. Sports, Art)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (nativeLang.isNotBlank() && targetLang.isNotBlank() && targetArea.isNotBlank() && selectedTopics.isNotBlank()) {
                    onComplete(
                        UserPreferences(
                            nativeLang = nativeLang,
                            targetLang = targetLang,
                            targetArea = targetArea,
                            favoriteTopics = selectedTopics,
                            isOnboarded = true
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && nativeLang.isNotBlank() && targetLang.isNotBlank() && targetArea.isNotBlank() && selectedTopics.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Let's Start!")
            }
        }
    }
}
