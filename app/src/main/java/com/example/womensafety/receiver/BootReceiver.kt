
package com.example.womensafety.receiver

import android.content.BroadcastReceiver
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.Context
import android.content.Intent
import com.example.womensafety.service.EmergencyService
import timber.log.Timber

//class BootReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent) {
//        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
//
//        // Only auto-start if user had the service running before
//        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
//        val sosWasEnabled = prefs.getBoolean("sos_was_running", false)
//
//        if (sosWasEnabled) {
//            Timber.d("Boot completed — restarting EmergencyService")
//            val serviceIntent = Intent(context, EmergencyService::class.java)
//            context.startForegroundService(serviceIntent)
//        }
//    }
//}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sosWasEnabled = prefs.getBoolean("sos_was_running", false)

        if (sosWasEnabled) {
            val hasLocation = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

            if (hasLocation) {
                Timber.d("Boot completed — restarting EmergencyService")
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, EmergencyService::class.java)
                )
            } else {
                Timber.w("Boot: skipping EmergencyService restart — location permission not granted")
            }
        }
    }
}