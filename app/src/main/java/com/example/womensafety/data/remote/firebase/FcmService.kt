package com.example.womensafety.data.remote.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.example.womensafety.core.util.Constants
import com.example.womensafety.data.local.dao.NotificationDao
import com.example.womensafety.data.local.entity.NotificationEntity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationDao: NotificationDao

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("FCM Token refreshed: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("FCM message received: ${message.notification?.title}")

        val title = message.notification?.title ?: message.data["title"] ?: "Alert"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val type = message.data["type"] ?: "ALERT"

        // Save to local DB
        serviceScope.launch {
            notificationDao.insert(
                NotificationEntity(title = title, body = body, type = type)
            )
        }

        // Show system notification
        showNotification(title, body, type)
    }

    private fun showNotification(title: String, body: String, type: String) {
        val manager = getSystemService(NotificationManager::class.java)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.CHANNEL_GENERAL,
                "Safety Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_GENERAL)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
