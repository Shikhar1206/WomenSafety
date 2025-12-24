package com.example.womensafety.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import kotlin.math.sqrt

class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {

    private var lastShakeTime = 0L

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val acceleration = sqrt((x * x + y * y + z * z).toDouble())

        Log.d("SHAKE", "Acceleration: $acceleration")

        if (acceleration > 12) { // lowered threshold
            val now = System.currentTimeMillis()
            if (now - lastShakeTime > 1500) {
                lastShakeTime = now
                Log.d("SHAKE", "SHAKE DETECTED")
                onShake()
            }
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
