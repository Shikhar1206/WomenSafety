package com.example.womensafety.service

import android.app.NotificationChannel
import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.example.womensafety.core.util.Constants
import com.example.womensafety.core.util.Constants.NOTIF_ID_EMERGENCY_SERVICE
import com.example.womensafety.receiver.SosActionReceiver
import com.example.womensafety.sensor.ShakeDetector
import com.example.womensafety.data.preferences.UserPreferencesDataStore
import com.example.womensafety.domain.usecase.sos.TriggerSosUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class EmergencyService : LifecycleService() {

    @Inject
    lateinit var triggerSosUseCase: TriggerSosUseCase

    @Inject
    lateinit var preferencesDataStore: UserPreferencesDataStore

    private lateinit var sensorManager: SensorManager
    private lateinit var shakeDetector: ShakeDetector
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isShakeEnabled = true

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        setupShakeDetector()
        
        serviceScope.launch {
            preferencesDataStore.userPreferences.collect { prefs ->
                isShakeEnabled = prefs.shakeEnabled
                Timber.d("EmergencyService preferences updated: shakeEnabled=$isShakeEnabled")
            }
        }
        Timber.d("EmergencyService started")
    }

    private fun setupShakeDetector() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        shakeDetector = ShakeDetector(threshold = Constants.SHAKE_THRESHOLD) {
            if (isShakeEnabled) {
                Timber.d("Shake detected — triggering SOS")
                // Broadcast to SosActionReceiver to trigger SOS from service context
                val intent = Intent(this, SosActionReceiver::class.java).apply {
                    action = "com.example.womensafety.SHAKE_SOS"
                }
                sendBroadcast(intent)

                // Trigger SOS to insert into Room and send SMS to all contacts
                serviceScope.launch {
                    val location = com.example.womensafety.util.LocationHelper.getCurrentLocationSuspend(this@EmergencyService)
                    val lat = location?.first
                    val lng = location?.second
                    triggerSosUseCase(
                        triggeredBy = Constants.SOS_TRIGGER_SHAKE,
                        lat = lat,
                        lng = lng
                    )
                }
            }
        }

        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(
            shakeDetector,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME  // Faster response than SENSOR_DELAY_NORMAL
        )
    }

    fun startEmergencyServiceSafely(context: Context) {
        val hasLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (hasLocation) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, EmergencyService::class.java)
            )
        } else {
            Timber.w("Cannot start EmergencyService — location permission not granted")
            // Trigger your permission request flow here instead
        }
    }

    private fun startForegroundNotification() {

        val manager = getSystemService(NotificationManager::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.CHANNEL_EMERGENCY,
                "Emergency Protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps emergency monitoring active in background"
            }
            manager.createNotificationChannel(channel)
        }

        // Cancel SOS action
        val cancelIntent = Intent(this, SosActionReceiver::class.java).apply {
            action = Constants.ACTION_CANCEL_SOS
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            this, 0, cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_EMERGENCY)
            .setContentTitle("🛡️ Smartify Active")
            .setContentText("Shake detection on • You're protected")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setOngoing(true)
            .setSilent(true)
            .addAction(android.R.drawable.ic_delete, "Cancel SOS", cancelPendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIF_ID_EMERGENCY_SERVICE,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIF_ID_EMERGENCY_SERVICE, notification)
        }

    }

    override fun onDestroy() {
        sensorManager.unregisterListener(shakeDetector)
        serviceScope.cancel()
        Timber.d("EmergencyService destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = super.onBind(intent)
}
