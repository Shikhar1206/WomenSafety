package com.example.womensafety.presentation.home

import android.content.Intent
import android.Manifest
import android.media.MediaPlayer
import androidx.compose.foundation.lazy.grid.GridItemSpan
import com.example.womensafety.R
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.womensafety.presentation.fakecall.FakeCallActivity
import com.example.womensafety.presentation.theme.*
import com.example.womensafety.data.preferences.UserPreferences
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToContacts: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFakeCall: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val sosState by viewModel.sosState.collectAsState()
    val contactCount by viewModel.contactCount.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()
    val context = LocalContext.current

    // Siren state
    var isSirenPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val toggleSiren = {
        if (isSirenPlaying) {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            } catch (e: Exception) {
                Timber.e(e, "Error stopping media player")
            }
            mediaPlayer = null
            isSirenPlaying = false
        } else {
            mediaPlayer = MediaPlayer.create(context, R.raw.siren).apply {
                isLooping = true
                setVolume(1.0f, 1.0f)
                start()
            }
            isSirenPlaying = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            } catch (e: Exception) {
                Timber.e(e, "Error disposing media player")
            }
        }
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val micGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        val callGranted = permissions[Manifest.permission.CALL_PHONE] ?: false
        Timber.d("HomeScreen startup permissions check: SMS=$smsGranted, Location=$locationGranted, Camera=$cameraGranted, Mic=$micGranted, Call=$callGranted")
    }

    LaunchedEffect(Unit) {
        val list = mutableListOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionsLauncher.launch(list.toTypedArray())
    }

    // Drifting Background Orbs Animation
    val infiniteTransition = rememberInfiniteTransition(label = "bg_orbs")
    val driftX by infiniteTransition.animateFloat(
        initialValue = -60f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftX"
    )
    val driftY by infiniteTransition.animateFloat(
        initialValue = -80f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftY"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Glowing Canvas Orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TrustPurple.copy(alpha = 0.15f), Color.Transparent),
                    radius = 350.dp.toPx()
                ),
                center = center.copy(x = center.x + driftX.dp.toPx(), y = center.y - driftY.dp.toPx())
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(WarmPink.copy(alpha = 0.12f), Color.Transparent),
                    radius = 300.dp.toPx()
                ),
                center = center.copy(x = center.x - driftX.dp.toPx(), y = center.y + driftY.dp.toPx())
            )
        }

        // Flashing police lights overlay when siren is playing
        if (isSirenPlaying) {
            val sirenTransition = rememberInfiniteTransition(label = "siren_lights")
            val overlayColor by sirenTransition.animateColor(
                initialValue = Color.Red.copy(alpha = 0.12f),
                targetValue = Color.Blue.copy(alpha = 0.12f),
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "overlayColor"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(overlayColor)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // ── Top Header ───────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Smartify",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = OnDark
                    )
                    Text(
                        text = if (contactCount > 0) "● Secure & Guarded" else "⚠️ Action required",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (contactCount > 0) SafeGreen else SafetyRed
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onNavigateToMap,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            .border(1.dp, GlassBorder, CircleShape)
                    ) {
                        Icon(Icons.Default.Map, "Map", tint = OnDark)
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            .border(1.dp, GlassBorder, CircleShape)
                    ) {
                        Icon(Icons.Default.Settings, "Settings", tint = OnDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Service Status Banner ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(name = "SHAKE", active = prefs.shakeEnabled)
                VerticalDivider(color = GlassBorder, modifier = Modifier.height(16.dp))
                StatusChip(name = "VOICE", active = prefs.safeWordEnabled)
                VerticalDivider(color = GlassBorder, modifier = Modifier.height(16.dp))
                StatusChip(name = "EVIDENCE", active = prefs.autoCaptureEnabled)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Warning Banner ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = contactCount == 0,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SafetyRed.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = SafetyRed.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = SafetyRed, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "No emergency contacts added",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = SafetyRed
                            )
                            Text(
                                "SOS cannot send alert SMS without contacts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnDarkSecondary
                            )
                        }
                        Button(
                            onClick = onNavigateToContacts,
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Add", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // ── SOS Button ───────────────────────────────────────────────────
            SosButton(
                sosState = sosState,
                onSosPress = { viewModel.startSosCountdown() },
                onCancelCountdown = { viewModel.cancelCountdown() },
                onCancelActive = { viewModel.cancelActiveSos() }
            )

            Spacer(modifier = Modifier.weight(0.1f))

            // ── Quick Actions Grid ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    "Quick Actions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    QuickActionCard("Contacts", Icons.Default.Contacts, TrustPurpleLight, onNavigateToContacts)
                }
                item {
                    QuickActionCard("Fake Call", Icons.Default.PhoneCallback, SafeGreen) {
                        context.startActivity(Intent(context, FakeCallActivity::class.java))
                    }
                }
                item {
                    QuickActionCard("SOS History", Icons.Default.History, WarmPinkLight, onNavigateToHistory)
                }
                item {
                    QuickActionCard("Safe Map", Icons.Default.Map, ElectricBlue, onNavigateToMap)
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    QuickActionCard(
                        title = if (isSirenPlaying) "Stop Siren" else "Siren (Loud)",
                        icon = if (isSirenPlaying) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        color = if (isSirenPlaying) SafetyRed else WarmPinkLight,
                        onClick = toggleSiren
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Error message overlay
        if (sosState is SosUiState.Error) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { viewModel.dismissError() },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .border(1.dp, SafetyRed.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = SafetyRed, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "SOS Alert Error",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = OnDark
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            (sosState as SosUiState.Error).message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnDarkSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.dismissError() },
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text("Dismiss", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(name: String, active: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(if (active) SafeGreen else Color.Gray, CircleShape)
        )
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) OnDark else OnDarkSecondary
        )
    }
}

@Composable
fun SosButton(
    sosState: SosUiState,
    onSosPress: () -> Unit,
    onCancelCountdown: () -> Unit,
    onCancelActive: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sos_glow")
    
    // Pulse animation properties for Active state
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale1"
    )
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, delayMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale2"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, delayMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha2"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            // Concentric Glow Pulse circles (Active state)
            if (sosState is SosUiState.Active) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .scale(scale2)
                        .background(SafetyRed.copy(alpha = alpha2), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .scale(scale1)
                        .background(SafetyRed.copy(alpha = alpha1), CircleShape)
                )
            }

            // Countdown outer stroke circle
            if (sosState is SosUiState.Countdown) {
                val animatedProgress by animateFloatAsState(
                    targetValue = sosState.secondsLeft / 10f,
                    animationSpec = tween(500, easing = LinearOutSlowInEasing),
                    label = "countdown_progress"
                )
                Canvas(modifier = Modifier.size(210.dp)) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = size.minDimension / 2 - 4.dp.toPx(),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    drawArc(
                        brush = Brush.sweepGradient(listOf(SafetyRed, WarmPink)),
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx())
                    )
                }
            }

            // Central Glowing Button Card
            Card(
                onClick = {
                    when (sosState) {
                        is SosUiState.Idle -> onSosPress()
                        is SosUiState.Countdown -> onCancelCountdown()
                        is SosUiState.Active -> onCancelActive()
                        is SosUiState.Error -> onSosPress()
                    }
                },
                modifier = Modifier
                    .size(180.dp)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.02f))
                        ),
                        shape = CircleShape
                    ),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = when (sosState) {
                                    is SosUiState.Active -> DangerGradient
                                    is SosUiState.Countdown -> listOf(
                                        SafetyRed.copy(alpha = 0.6f),
                                        WarmPink.copy(alpha = 0.6f)
                                    )
                                    else -> listOf(
                                        SafetyRedDark,
                                        Color(0xFF881111)
                                    )
                                }
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        when (sosState) {
                            is SosUiState.Countdown -> {
                                Text(
                                    "${sosState.secondsLeft}",
                                    fontSize = 52.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = (-1).sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("CANCEL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.8f))
                            }
                            is SosUiState.Active -> {
                                Icon(Icons.Default.Shield, null, tint = Color.White, modifier = Modifier.size(42.dp))
                                Spacer(Modifier.height(6.dp))
                                Text("SOS ACTIVE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("TAP TO CANCEL", fontSize = 9.sp, color = Color.White.copy(0.7f))
                            }
                            else -> {
                                Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(38.dp))
                                Spacer(Modifier.height(6.dp))
                                Text("SOS", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (sosState) {
                is SosUiState.Idle -> "Tap to activate • Shake anytime"
                is SosUiState.Countdown -> "Sending distress alert in ${sosState.secondsLeft}s..."
                is SosUiState.Active -> "🚨 Alert broadcasted to contacts"
                is SosUiState.Error -> "SOS failed — tap to retry"
            },
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = when (sosState) {
                is SosUiState.Active -> SafeGreen
                is SosUiState.Error -> SafetyRed
                else -> OnDarkSecondary
            },
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(94.dp)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.01f))
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.02f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                    .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = OnDark
            )
        }
    }
}
