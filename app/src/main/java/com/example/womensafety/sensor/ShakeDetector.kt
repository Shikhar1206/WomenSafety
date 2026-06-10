package com.example.womensafety.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import timber.log.Timber
import kotlin.math.sqrt

/**
 * Enhanced ShakeDetector with configurable threshold and cooldown.
 * Bug fix: gravity component subtracted to avoid false positives from device tilt.
 */

class ShakeDetector(
    private val threshold: Float = 13f,
    private val cooldownMs: Long = 1500L,
    private val onShake: () -> Unit
) : SensorEventListener {

    private var lastShakeTime = 0L

    // Gravity values for filtering
    private val gravity = FloatArray(3) { 0f }
    private val alpha = 0.8f  // Low-pass filter constant

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        // Apply low-pass filter to isolate gravity
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

        // High-pass filter to get linear acceleration (without gravity)
        val x = event.values[0] - gravity[0]
        val y = event.values[1] - gravity[1]
        val z = event.values[2] - gravity[2]

        val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        if (acceleration > threshold) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime > cooldownMs) {
                lastShakeTime = now
                Timber.d("Shake detected! Acceleration: $acceleration")
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
