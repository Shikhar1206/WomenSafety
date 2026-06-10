package com.example.womensafety.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.womensafety.core.util.Constants
import com.example.womensafety.data.preferences.UserPreferencesDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

/**
 * BatteryReceiver — listens to ACTION_BATTERY_CHANGED and triggers Battery SOS if enabled.
 * Uses Hilt to inject UserPreferencesDataStore.
 */

@AndroidEntryPoint
class BatteryReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferencesDataStore: UserPreferencesDataStore

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BATTERY_CHANGED) return

        val level = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
        if (level == -1 || scale == -1) return

        val batteryPercent = (level * 100) / scale
        Timber.d("Battery level changed: $batteryPercent%")

        val pendingResult = goAsync()
        val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        coroutineScope.launch {
            try {
                // Fetch preference from Datastore
                val prefs = preferencesDataStore.userPreferences.first()
                if (!prefs.batterySosEnabled) {
                    Timber.d("Battery SOS feature is disabled in user preferences")
                    return@launch
                }

                val sharedPrefs = context.getSharedPreferences("battery_prefs", Context.MODE_PRIVATE)
                val lastSent = sharedPrefs.getBoolean("last_sent", false)

                if (batteryPercent <= Constants.BATTERY_CRITICAL_PERCENT && !lastSent) {
                    Timber.d("Battery level $batteryPercent% is below critical threshold (${Constants.BATTERY_CRITICAL_PERCENT}%)!")
                    sharedPrefs.edit().putBoolean("last_sent", true).apply()

                    // Broadcast SOS trigger to SosActionReceiver
                    val sosIntent = Intent(context, SosActionReceiver::class.java).apply {
                        action = "com.example.womensafety.BATTERY_SOS"
                    }
                    context.sendBroadcast(sosIntent)
                } else if (batteryPercent > Constants.BATTERY_CRITICAL_PERCENT && lastSent) {
                    // Reset threshold check when battery is charged back up
                    Timber.d("Battery charged above critical threshold. Resetting last_sent flag.")
                    sharedPrefs.edit().putBoolean("last_sent", false).apply()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error processing battery check")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
