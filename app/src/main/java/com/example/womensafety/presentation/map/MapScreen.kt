package com.example.womensafety.presentation.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.womensafety.presentation.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateBack: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState()
    val routePoints by viewModel.routePoints.collectAsState()

    // Get live location
    LaunchedEffect(Unit) {
        val hasLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (hasLocation) {
            val client = LocationServices.getFusedLocationProviderClient(context)
            try {
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val userLatLng = LatLng(location.latitude, location.longitude)
                            currentLocation = userLatLng
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
                        }
                    }
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException while getting location on MapScreen")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── Top App Bar ──────────────────────────────────────────────────
            TopAppBar(
                title = { Text("Safe Map", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = OnDark) },
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

            // ── Map Container ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (currentLocation != null) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = true),
                        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = true)
                    ) {
                        // Current location marker
                        Marker(
                            state = rememberMarkerState(position = currentLocation!!),
                            title = "Your Location"
                        )

                        // Draw SOS Breadcrumbs Polyline
                        if (routePoints.isNotEmpty()) {
                            Polyline(
                                points = routePoints,
                                color = Color(0xFFFF3366),
                                width = 10f
                            )
                            // Start marker
                            Marker(
                                state = rememberMarkerState(position = routePoints.first()),
                                title = "SOS Triggered Here",
                                icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                                    com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
                                )
                            )
                        }
                    }

                    // Glassmorphic Map Status Card Overlay
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .fillMaxWidth(0.92f)
                            .border(
                                1.dp,
                                brush = Brush.verticalGradient(
                                    listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.02f))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface.copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(SafeGreen.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Security, "Safe Zone Map", tint = SafeGreen, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Safe Zone Map Active",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = OnDark
                                )
                                Text(
                                    text = "Real-time SOS breadcrumb tracking active",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = OnDarkSecondary
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = TrustPurpleLight)
                            Spacer(Modifier.height(12.dp))
                            Text("Acquiring GPS Signal...", style = MaterialTheme.typography.bodySmall, color = OnDarkSecondary)
                        }
                    }
                }
            }
        }
    }
}
