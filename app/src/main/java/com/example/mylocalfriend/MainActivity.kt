package com.example.mylocalfriend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: TutorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: TutorViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var userInputText by remember { mutableStateOf("") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                ConfigDrawerContent(viewModel = viewModel, onClose = {
                    scope.launch { drawerState.close() }
                })
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("🧑‍🏫 Thomas - AI Language Tutor") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            },
            bottomBar = {
                if (viewModel.isLessonStarted) {
                    ChatInputField(
                        text = userInputText,
                        onTextChange = { userInputText = it },
                        onSend = {
                            if (userInputText.isNotBlank()) {
                                viewModel.sendMessage(userInputText)
                                userInputText = ""
                            }
                        },
                        isLoading = viewModel.isLoading
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (!viewModel.isLessonStarted) {
                    WelcomeScreen(onStart = { scope.launch { drawerState.open() } })
                } else {
                    ChatAndVocabFeed(viewModel = viewModel)
                }

                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

// --- CONFIG DRAWER (STREAMLIT SIDEBAR EQUIVALENT) ---
@Composable
fun ConfigDrawerContent(viewModel: TutorViewModel, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("🎓 Language Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.nativeLang,
            onValueChange = { viewModel.nativeLang = it },
            label = { Text("Native Language") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.targetLang,
            onValueChange = { viewModel.targetLang = it },
            label = { Text("Language to Learn") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("🎯 Interests & Location", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.targetArea,
            onValueChange = { viewModel.targetArea = it },
            label = { Text("City / Region") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.selectedTopics,
            onValueChange = { viewModel.selectedTopics = it },
            label = { Text("Topics of Interest") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onClose()
                viewModel.startLesson()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🚀 Start News Lesson")
        }
    }
}

// --- WELCOME SCREEN ---
@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🧑‍🏫 Welcome to Thomas!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Learn languages through real-time news! Open the menu icon to set your target language, city, and topics, then start your lesson.",
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onStart) {
            Text("⚙️ Open Preferences Menu")
        }
    }
}

// --- MAIN CHAT FEED WITH VOCAB CARD ---
@Composable
fun ChatAndVocabFeed(viewModel: TutorViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Smart Cheat Sheet Card
        if (viewModel.lastVocabulary.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("💡 Smart Cheat Sheet", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(viewModel.lastVocabulary)
                    }
                }
            }
        }

        // Chat Messages
        items(viewModel.messages) { msg ->
            if (msg.role == "user") {
                UserMessageBubble(text = msg.content)
            } else {
                AssistantMessageCard(
                    response = msg.parsedResponse,
                    targetLang = viewModel.targetLang,
                    nativeLang = viewModel.nativeLang
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// --- USER BUBBLE ---
@Composable
fun UserMessageBubble(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = text,
                color = Color.White,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

// --- ASSISTANT MESSAGE CARD WITH TABS ---
@Composable
fun AssistantMessageCard(
    response: TutorResponse?,
    targetLang: String,
    nativeLang: String
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (response != null) {
                // 1. Feedback Section
                if (response.feedback.isNotEmpty()) {
                    Text("📝 Language Feedback:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(response.feedback)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }

                // 2. Tabs for Target vs Translation
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("🌐 $targetLang") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("💬 $nativeLang") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (selectedTab) {
                    0 -> Text(response.targetText)
                    1 -> Text(
                        if (response.nativeText.isNotEmpty()) response.nativeText
                        else "Translation not available."
                    )
                }
            }
        }
    }
}

// --- CHAT INPUT BAR ---
@Composable
fun ChatInputField(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("Reply to practice...") },
            modifier = Modifier.weight(1f),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            enabled = !isLoading && text.isNotBlank()
        ) {
            Icon(Icons.Default.Send, contentDescription = "Send")
        }
    }
}