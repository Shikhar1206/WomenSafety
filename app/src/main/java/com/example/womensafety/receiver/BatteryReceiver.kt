package com.example.womensafety.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log

class BatteryReceiver : BroadcastReceiver() {

    private var lastSent = false
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            if (level == -1 || scale == -1) return

            val batteryPercent = (level * 100) / scale

            Log.d("BATTERY", "Battery: $batteryPercent%")

            if (batteryPercent <= 2 && !lastSent) {
                lastSent = true

                Log.d("BATTERY", "Battery critical — sending SOS")

                LocationHelper.getLocation(context) { location ->
                    SmsUtil.sendSOS(context, location)
                }
            }
        }
    }
}
