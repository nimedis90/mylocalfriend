package my.local.friend.android.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import my.local.friend.android.app.ui.AuthScreen
import my.local.friend.android.app.ui.OnboardingScreen

class MainActivity : ComponentActivity() {
    private val viewModel: TutorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: TutorViewModel) {
    val navController = rememberNavController()
    val currentUser = viewModel.currentUser
    val userPrefs = viewModel.userPrefs

    // Navigation logic based on Auth and Onboarding state
    LaunchedEffect(currentUser, userPrefs.isOnboarded) {
        if (currentUser == null) {
            navController.navigate("auth") {
                popUpTo(0)
            }
        } else if (!userPrefs.isOnboarded) {
            navController.navigate("onboarding") {
                popUpTo(0)
            }
        } else {
            navController.navigate("main") {
                popUpTo(0)
            }
        }
    }

    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") {
            AuthScreen(
                onAuthSuccess = { /* Handled by LaunchedEffect */ },
                onSignIn = { email, pass -> viewModel.signIn(email, pass) },
                onSignUp = { email, pass -> viewModel.signUp(email, pass) },
                isLoading = viewModel.isAuthLoading
            )
        }
        composable("onboarding") {
            OnboardingScreen(
                onComplete = { prefs -> viewModel.saveUserPreferences(prefs) },
                isLoading = viewModel.isPrefsLoading
            )
        }
        composable("main") {
            MainAppScreen(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: TutorViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var userInputText by remember { mutableStateOf("") }
    var showTopicSwitcher by remember { mutableStateOf(false) }

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
                    title = { Text("🧑‍🏫 Thomas") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Profile")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showTopicSwitcher = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Change Topic")
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
                    WelcomeScreen(
                        userName = viewModel.currentUser?.email?.split("@")?.get(0) ?: "Friend",
                        onStart = { viewModel.startLesson() }
                    )
                } else {
                    ChatAndVocabFeed(viewModel = viewModel)
                }

                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                if (showTopicSwitcher) {
                    TopicSwitcherDialog(
                        currentTopic = viewModel.userPrefs.favoriteTopics,
                        onDismiss = { showTopicSwitcher = false },
                        onConfirm = { newTopic ->
                            viewModel.saveUserPreferences(viewModel.userPrefs.copy(favoriteTopics = newTopic))
                            showTopicSwitcher = false
                            viewModel.startLesson() // Restart with new topic
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TopicSwitcherDialog(
    currentTopic: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var topic by remember { mutableStateOf(currentTopic) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Conversation Topic") },
        text = {
            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Topic") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(topic) }) {
                Text("Update & Restart")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// --- CONFIG DRAWER (PROFILE SECTION) ---
@Composable
fun ConfigDrawerContent(viewModel: TutorViewModel, onClose: () -> Unit) {
    val prefs = viewModel.userPrefs
    var nativeLang by remember { mutableStateOf(prefs.nativeLang) }
    var targetLang by remember { mutableStateOf(prefs.targetLang) }
    var targetArea by remember { mutableStateOf(prefs.targetArea) }
    var targetLevel by remember { mutableStateOf(prefs.targetLevel) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("👤 Profile & Preferences", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = nativeLang,
            onValueChange = { nativeLang = it },
            label = { Text("Native Language") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = targetLang,
            onValueChange = { targetLang = it },
            label = { Text("Language to Learn") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = targetLevel,
            onValueChange = { targetLevel = it },
            label = { Text("Level (A1, A2, B1...)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("📍 Location", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = targetArea,
            onValueChange = { targetArea = it },
            label = { Text("City / Region") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.saveUserPreferences(
                    prefs.copy(
                        nativeLang = nativeLang,
                        targetLang = targetLang,
                        targetLevel = targetLevel,
                        targetArea = targetArea
                    )
                )
                onClose()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                viewModel.signOut()
                onClose()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout")
        }
    }
}

// --- WELCOME SCREEN ---
@Composable
fun WelcomeScreen(userName: String, onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🧑‍🏫 Welcome, $userName!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Ready for today's lesson? We'll look at the latest news based on your preferences.",
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onStart) {
            Text("🚀 Start News Lesson")
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
                    targetLang = viewModel.userPrefs.targetLang,
                    nativeLang = viewModel.userPrefs.nativeLang
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
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
            .navigationBarsPadding() // Keeps distance from bottom system bar
            .imePadding()            // Pushes input bar up above the software keyboard
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
