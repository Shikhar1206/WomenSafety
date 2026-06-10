package com.example.womensafety.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.womensafety.core.util.Constants
import com.example.womensafety.data.local.dao.LocationHistoryDao
import com.example.womensafety.data.local.entity.LocationHistoryEntity
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Background location tracking service — runs during active SOS.
 * Stores location breadcrumbs to Room for SOS route history.
 * Uses PRIORITY_HIGH_ACCURACY with 10-second intervals.
 */

@AndroidEntryPoint
class LocationTrackingService : LifecycleService() {

    @Inject
    lateinit var locationHistoryDao: LocationHistoryDao

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val EXTRA_SOS_ID = "sos_id"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startForegroundNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val sosId = intent?.getStringExtra(EXTRA_SOS_ID) ?: return START_NOT_STICKY
        startTrackingForSos(sosId)
        return START_STICKY
    }

    private fun startTrackingForSos(sosId: String) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000L  // every 10 seconds
        )
            .setMinUpdateIntervalMillis(5_000L)
            .setMaxUpdateDelayMillis(15_000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    Timber.d("Location update: ${location.latitude}, ${location.longitude}")
                    serviceScope.launch {
                        locationHistoryDao.insert(
                            LocationHistoryEntity(
                                sosRecordId = sosId,
                                latitude = location.latitude,
                                longitude = location.longitude,
                                accuracy = location.accuracy,
                                speed = location.speed
                            )
                        )
                    }
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                mainLooper
            )
        } catch (e: SecurityException) {
            Timber.e(e, "Location permission not granted")
            stopSelf()
        }
    }

    private fun startForegroundNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            Constants.CHANNEL_LOCATION,
            "Location Tracking",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_LOCATION)
            .setContentTitle("📍 Location Tracking")
            .setContentText("Tracking your location for emergency SOS")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setSilent(true)
            .build()

        startForeground(
            Constants.NOTIF_ID_LOCATION_SERVICE,
            notification,
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )
    }

    override fun onDestroy() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        serviceScope.cancel()
        Timber.d("LocationTrackingService destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = super.onBind(intent)
}
