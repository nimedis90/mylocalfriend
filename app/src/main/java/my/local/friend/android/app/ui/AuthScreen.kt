package my.local.friend.android.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onSignIn: suspend (String, String) -> Result<Any?>,
    onSignUp: suspend (String, String) -> Result<Any?>,
    onForgotPassword: suspend (String) -> Result<Unit>,
    isLoading: Boolean
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
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
        Text(
            text = if (isLoginMode) "Welcome Back!" else "Create Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isLoginMode) "Sign in to continue" else "Sign up to start learning",
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (isLoginMode || !isLoginMode) { // Simplified check, both modes need password except for reset
             Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
        
        if (successMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = successMessage!!, color = MaterialTheme.colorScheme.primary)
        }

        if (isLoginMode) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = {
                    errorMessage = null
                    successMessage = null
                    val emailTrimmed = email.trim()
                    if (emailTrimmed.isEmpty()) {
                        errorMessage = "Please enter your email to reset password"
                        return@TextButton
                    }
                    scope.launch {
                        val result = onForgotPassword(emailTrimmed)
                        if (result.isSuccess) {
                            successMessage = "Reset email sent! Check your inbox."
                        } else {
                            errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to send reset email"
                        }
                    }
                }) {
                    Text("Forgot Password?")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                errorMessage = null
                successMessage = null
                val emailTrimmed = email.trim()
                if (emailTrimmed.isEmpty() || password.isEmpty()) {
                    errorMessage = "Please fill all fields"
                    return@Button
                }
                
                scope.launch {
                    val result = if (isLoginMode) {
                        onSignIn(emailTrimmed, password)
                    } else {
                        onSignUp(emailTrimmed, password)
                    }
                    
                    if (result.isSuccess) {
                        onAuthSuccess()
                    } else {
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Auth failed"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (isLoginMode) "Sign In" else "Sign Up")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { 
            isLoginMode = !isLoginMode 
            errorMessage = null
            successMessage = null
        }) {
            Text(if (isLoginMode) "Don't have an account? Sign Up" else "Already have an account? Sign In")
        }
    }
}
