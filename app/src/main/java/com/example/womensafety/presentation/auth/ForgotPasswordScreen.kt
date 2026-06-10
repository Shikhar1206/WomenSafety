package com.example.womensafety.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.womensafety.presentation.theme.*

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }

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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = OnDark)
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(TrustPurple.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .border(1.dp, TrustPurpleLight.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LockReset, null, tint = TrustPurpleLight, modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Reset Password",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = OnDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Enter your email address and we'll send you a link to reset your password",
                style = MaterialTheme.typography.bodyMedium,
                color = OnDarkSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.success) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SafeGreen.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, SafeGreen.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = SafeGreen)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Reset link sent! Check your email inbox.",
                                    color = SafeGreen,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Email") },
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = OnDarkSecondary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

                        uiState.error?.let { error ->
                            Spacer(Modifier.height(12.dp))
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

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.sendPasswordReset(email) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .background(
                                    Brush.horizontalGradient(PrimaryGradient),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            enabled = email.isNotBlank() && !uiState.isLoading,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.White.copy(alpha = 0.02f)
                            )
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                            } else {
                                Icon(Icons.Default.Send, null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Send Reset Link", color = Color.White, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
