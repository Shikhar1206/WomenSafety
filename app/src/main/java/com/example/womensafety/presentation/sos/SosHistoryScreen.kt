package com.example.womensafety.presentation.sos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.womensafety.core.util.Constants
import com.example.womensafety.core.util.toFormattedDate
import com.example.womensafety.core.util.toRelativeTimeString
import com.example.womensafety.domain.model.SosRecord
import com.example.womensafety.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: SosViewModel = hiltViewModel()
) {
    val history by viewModel.sosHistory.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── Top App Bar ──────────────────────────────────────────────────
            TopAppBar(
                title = {
                    Column {
                        Text("SOS History", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = OnDark)
                        Text("${history.size} incidents recorded", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = OnDarkSecondary)
                    }
                },
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

            // ── History Content ──────────────────────────────────────────────
            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .background(SafeGreen.copy(0.08f), CircleShape)
                                .border(1.dp, SafeGreen.copy(0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = SafeGreen, modifier = Modifier.size(48.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "System Clear",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = OnDark
                        )
                        Text(
                            "Stay safe! Your emergency incident logs will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnDarkSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Timeline Line Indicator
                    Box(
                        modifier = Modifier
                            .padding(start = 32.dp)
                            .fillMaxHeight()
                            .width(2.dp)
                            .background(Outline.copy(alpha = 0.3f))
                    )

                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(history) { index, record ->
                            SosHistoryTimelineItem(record = record)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SosHistoryTimelineItem(record: SosRecord) {
    val statusColor = when (record.status) {
        Constants.SOS_STATUS_ACTIVE -> SafetyRed
        Constants.SOS_STATUS_CANCELLED -> StatusCancelled
        Constants.SOS_STATUS_RESOLVED -> SafeGreen
        else -> OnDarkSecondary
    }

    val triggerIcon = when (record.triggeredBy) {
        Constants.SOS_TRIGGER_SHAKE -> Icons.Default.Vibration
        Constants.SOS_TRIGGER_VOICE -> Icons.Default.Mic
        Constants.SOS_TRIGGER_BATTERY -> Icons.Default.BatteryAlert
        else -> Icons.Default.TouchApp
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline Dot & Glow
        Box(
            modifier = Modifier
                .padding(top = 18.dp)
                .size(34.dp)
                .background(Background, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(statusColor, CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            )
        }

        Spacer(Modifier.width(12.dp))

        // Card Content
        Card(
            modifier = Modifier
                .weight(1f)
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
            Column(modifier = Modifier.padding(16.dp)) {
                // Header Information
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = record.triggeredAt.toRelativeTimeString(),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = OnDark
                        )
                        Text(
                            text = record.triggeredAt.toFormattedDate(),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = OnDarkSecondary
                        )
                    }

                    // Status Badge
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .border(1.dp, statusColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = record.status,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusColor
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = GlassBorder)
                Spacer(Modifier.height(12.dp))

                // Info Chips Grid Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoChipItem(icon = triggerIcon, label = "Source", value = record.triggeredBy)
                    InfoChipItem(icon = Icons.Default.Sms, label = "SMS Sent", value = "${record.smsSentCount}")
                    InfoChipItem(
                        icon = if (record.latitude != null) Icons.Default.LocationOn else Icons.Default.LocationOff,
                        label = "GPS",
                        value = if (record.latitude != null) "Captured" else "N/A"
                    )
                }

                // Expandable Location link
                if (record.latitude != null && record.longitude != null) {
                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Map, null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Lat: ${"%.4f".format(record.latitude)}, Lng: ${"%.4f".format(record.longitude)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = OnDarkSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChipItem(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(8.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(icon, null, tint = OnDarkSecondary, modifier = Modifier.size(14.dp))
        Column {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = OnDarkSecondary)
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnDark)
        }
    }
}
