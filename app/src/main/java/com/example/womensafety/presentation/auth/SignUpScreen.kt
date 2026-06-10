package com.example.womensafety.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.womensafety.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.success) {
        if (uiState.success) onSignUpSuccess()
    }

    // Animated Background Orbs
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")
    val orbitX by infiniteTransition.animateFloat(
        initialValue = -80f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbitX"
    )
    val orbitY by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbitY"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Drifting Glassmorphic Orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TrustPurple.copy(alpha = 0.2f), Color.Transparent),
                    radius = 350.dp.toPx()
                ),
                center = center.copy(
                    x = center.x + orbitX.dp.toPx(),
                    y = center.y - orbitY.dp.toPx()
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(WarmPink.copy(alpha = 0.18f), Color.Transparent),
                    radius = 300.dp.toPx()
                ),
                center = center.copy(
                    x = center.x - orbitX.dp.toPx(),
                    y = center.y + orbitY.dp.toPx()
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Back Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onNavigateToLogin) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnDark)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                color = OnDark
            )
            Text(
                text = "Join Smartify for your safety",
                style = MaterialTheme.typography.bodyMedium,
                color = OnDarkSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Glassmorphic Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.02f))
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.03f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = OnDarkSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TrustPurpleLight,
                            unfocusedBorderColor = Outline,
                            focusedLabelColor = TrustPurpleLight,
                            unfocusedLabelColor = OnDarkSecondary,
                            focusedTextColor = OnDark,
                            unfocusedTextColor = OnDark
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = OnDarkSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TrustPurpleLight,
                            unfocusedBorderColor = Outline,
                            focusedLabelColor = TrustPurpleLight,
                            unfocusedLabelColor = OnDarkSecondary,
                            focusedTextColor = OnDark,
                            unfocusedTextColor = OnDark
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = OnDarkSecondary) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = OnDarkSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TrustPurpleLight,
                            unfocusedBorderColor = Outline,
                            focusedLabelColor = TrustPurpleLight,
                            unfocusedLabelColor = OnDarkSecondary,
                            focusedTextColor = OnDark,
                            unfocusedTextColor = OnDark
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.LockOpen, null, tint = OnDarkSecondary) },
                        isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                        supportingText = {
                            if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                                Text("Passwords do not match", color = SafetyRed)
                            }
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TrustPurpleLight,
                            unfocusedBorderColor = Outline,
                            focusedLabelColor = TrustPurpleLight,
                            unfocusedLabelColor = OnDarkSecondary,
                            focusedTextColor = OnDark,
                            unfocusedTextColor = OnDark,
                            errorBorderColor = SafetyRed
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Local / Server Errors
                    (uiState.error ?: localError)?.let { error ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SafetyRed.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, SafetyRed.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, null, tint = SafetyRed, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(error, style = MaterialTheme.typography.bodySmall, color = SafetyRed)
                            }
                        }
                    }

                    // Register Button (Gradient)
                    Button(
                        onClick = {
                            localError = null
                            when {
                                name.isBlank() -> localError = "Name is required"
                                password.length < 6 -> localError = "Password must be at least 6 characters"
                                password != confirmPassword -> localError = "Passwords do not match"
                                else -> viewModel.signUp(email, password, name)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .background(
                                Brush.horizontalGradient(PrimaryGradient),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && !uiState.isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.White.copy(alpha = 0.02f)
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                        } else {
                            Icon(Icons.Default.PersonAdd, null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Create Account", color = Color.White, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?", color = OnDarkSecondary, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onNavigateToLogin) {
                    Text("Sign In", color = TrustPurpleLight, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
