package com.example.womensafety.util

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume

/**
 * Fixed: Added package declaration (was missing).
 * Fixed: Callback-based → suspend function for coroutine compatibility.
 * Fixed: Added getCurrentLocation() with Priority.PRIORITY_HIGH_ACCURACY for fresher results.
 */

object LocationHelper {

     // Gets last known location — fast but may be null or stale.
//    fun getLocationLink(context: Context, callback: (String) -> Unit) {
//        val hasLocation = ContextCompat.checkSelfPermission(
//            context, Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(
//                    context, Manifest.permission.ACCESS_COARSE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED
//
//        if (!hasLocation) {
//            callback("Location unavailable")
//            return
//        }
//
//        val client = LocationServices.getFusedLocationProviderClient(context)
//        try {
//            client.lastLocation
//                .addOnSuccessListener { location ->
//                    if (location != null) {
//                        callback("https://maps.google.com/?q=${location.latitude},${location.longitude}")
//                    } else {
//                        callback("Location unavailable")
//                    }
//                }
//                .addOnFailureListener { e ->
//                    Timber.e(e, "Failed to get last location")
//                    callback("Location unavailable")
//                }
//        } catch (e: Exception) {
//            Timber.e(e, "LocationHelper exception")
//            callback("Location unavailable")
//        }
//    }

    /**
     * Suspend version — use in coroutines for clean async handling.
     * Returns Pair(latitude, longitude) or null on failure.
     */
    suspend fun getCurrentLocationSuspend(context: Context): Pair<Double, Double>? {
        val hasLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (!hasLocation) {
            Timber.w("Location permission not granted. Skipping getCurrentLocationSuspend.")
            return null
        }

        return suspendCancellableCoroutine { cont ->
            val cts = CancellationTokenSource()
            val client = LocationServices.getFusedLocationProviderClient(context)
            try {
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            cont.resume(Pair(location.latitude, location.longitude))
                        } else {
                            cont.resume(null)
                        }
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "getCurrentLocation failed")
                        cont.resume(null)
                    }
                cont.invokeOnCancellation { cts.cancel() }
            } catch (e: Exception) {
                Timber.e(e, "LocationHelper suspend exception")
                cont.resume(null)
            }
        }
    }

//    fun buildMapsLink(lat: Double, lng: Double): String =
//        "https://maps.google.com/?q=$lat,$lng"
}
