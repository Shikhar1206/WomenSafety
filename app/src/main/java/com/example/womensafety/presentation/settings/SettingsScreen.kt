package com.example.womensafety.presentation.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.womensafety.presentation.theme.*
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.userPreferences.collectAsState()
    val context = LocalContext.current

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setSafeWordEnabled(isGranted)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setAutoCaptureEnabled(isGranted)
    }

    val shakeEnabled = prefs.shakeEnabled
    val shakeSensitivity = prefs.shakeSensitivity
    val voiceSosEnabled = prefs.safeWordEnabled
    val safeWord = prefs.safeWord
    val batterySosEnabled = prefs.batterySosEnabled
    val autoCaptureEnabled = prefs.autoCaptureEnabled
    val fakeCallName = prefs.fakeCallName
    val calendarUnlockDay = prefs.calendarUnlockDay

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── Top App Bar ──────────────────────────────────────────────────
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = OnDark) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            .border(1.dp, GlassBorder, CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = OnDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── SOS Triggers ─────────────────────────────────────────────
                SettingsSection(title = "SOS Triggers", icon = Icons.Default.Warning) {
                    // Shake Detection
                    SettingsSwitchRow(
                        icon = Icons.Default.Vibration,
                        title = "Shake to SOS",
                        subtitle = "Shake your phone to trigger emergency SOS",
                        checked = shakeEnabled,
                        onCheckedChange = { viewModel.setShakeEnabled(it) }
                    )
                    if (shakeEnabled) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Shake Sensitivity", style = MaterialTheme.typography.bodySmall, color = OnDarkSecondary)
                                Text(
                                    when {
                                        shakeSensitivity < 0.33f -> "Low"
                                        shakeSensitivity < 0.66f -> "Medium"
                                        else -> "High"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = TrustPurpleLight
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Slider(
                                value = shakeSensitivity,
                                onValueChange = { viewModel.setShakeSensitivity(it) },
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = TrustPurpleLight,
                                    inactiveTrackColor = Outline
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    // Voice SOS
                    SettingsSwitchRow(
                        icon = Icons.Default.Mic,
                        title = "Voice Activated SOS",
                        subtitle = "Say your safe word to trigger SOS",
                        checked = voiceSosEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                val hasMic = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasMic) {
                                    viewModel.setSafeWordEnabled(true)
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            } else {
                                viewModel.setSafeWordEnabled(false)
                            }
                        }
                    )
                    if (voiceSosEnabled) {
                        OutlinedTextField(
                            value = safeWord,
                            onValueChange = { viewModel.setSafeWord(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp),
                            label = { Text("Safe Word") },
                            leadingIcon = { Icon(Icons.Default.RecordVoiceOver, null, tint = OnDarkSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TrustPurpleLight,
                                unfocusedBorderColor = Outline,
                                focusedTextColor = OnDark,
                                unfocusedTextColor = OnDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    // Battery SOS
                    SettingsSwitchRow(
                        icon = Icons.Default.BatteryAlert,
                        title = "Low Battery SOS",
                        subtitle = "Auto-send location when battery is critical",
                        checked = batterySosEnabled,
                        onCheckedChange = { viewModel.setBatterySosEnabled(it) }
                    )
                }

                // ── Evidence Capture ─────────────────────────────────────────
                SettingsSection(title = "Evidence Capture", icon = Icons.Default.CameraAlt) {
                    SettingsSwitchRow(
                        icon = Icons.Default.Camera,
                        title = "Auto Photo Capture",
                        subtitle = "Silently capture photos periodically during SOS",
                        checked = autoCaptureEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                val hasCamera = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasCamera) {
                                    viewModel.setAutoCaptureEnabled(true)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            } else {
                                viewModel.setAutoCaptureEnabled(false)
                            }
                        }
                    )
                }

                // Fake Call
                SettingsSection(title = "Fake Call", icon = Icons.Default.PhoneCallback) {
                    OutlinedTextField(
                        value = fakeCallName,
                        onValueChange = { viewModel.setFakeCallName(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        label = { Text("Caller Name") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = OnDarkSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TrustPurpleLight,
                            unfocusedBorderColor = Outline,
                            focusedTextColor = OnDark,
                            unfocusedTextColor = OnDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // ── Disguise Settings ──────────────────────────────────────────
                SettingsSection(title = "Disguise Settings", icon = Icons.Default.VisibilityOff) {
                    OutlinedTextField(
                        value = if (calendarUnlockDay == 0) "" else calendarUnlockDay.toString(),
                        onValueChange = { value ->
                            val parsed = value.toIntOrNull()
                            if (parsed != null && parsed in 1..31) {
                                viewModel.setCalendarUnlockDay(parsed)
                            } else if (value.isEmpty()) {
                                viewModel.setCalendarUnlockDay(0)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        label = { Text("Unlock Day (1-31)") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = OnDarkSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TrustPurpleLight,
                            unfocusedBorderColor = Outline,
                            focusedTextColor = OnDark,
                            unfocusedTextColor = OnDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // ── About ─────────────────────────────────────────────────────
                SettingsSection(title = "About", icon = Icons.Default.Info) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Version", style = MaterialTheme.typography.bodyMedium, color = OnDarkSecondary)
                        Text("1.0.0", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = OnDark)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Developer", style = MaterialTheme.typography.bodyMedium, color = OnDarkSecondary)
                        Text("Shikhar Agarwal", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TrustPurpleLight)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = TrustPurpleLight, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnDark
                )
            }
            HorizontalDivider(color = GlassBorder)
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = OnDarkSecondary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = OnDark
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = OnDarkSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = TrustPurple,
                uncheckedThumbColor = OnDarkSecondary,
                uncheckedTrackColor = Outline.copy(alpha = 0.5f)
            )
        )
    }
}
