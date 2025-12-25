import android.content.Context
import com.google.android.gms.location.LocationServices

//package com.example.womensafety.util
//
//import android.content.Context
//import android.content.pm.PackageManager
//import androidx.core.content.ContextCompat
//import com.google.android.gms.location.LocationServices
//
//object LocationHelper {
//
//    fun getLocation(context: Context, callback: (String) -> Unit) {
//
//        val permissionGranted =
//            ContextCompat.checkSelfPermission(
//                context,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//
//        if (!permissionGranted) {
//            callback("Location permission not granted")
//            return
//        }
//
//        val client = LocationServices.getFusedLocationProviderClient(context)
//
//        client.lastLocation
//            .addOnSuccessListener { location ->
//                if (location != null) {
//                    val link =
//                        "https://maps.google.com/?q=${location.latitude},${location.longitude}"
//                    callback(link)
//                } else {
//                    callback("Location unavailable")
//                }
//            }
//            .addOnFailureListener {
//                callback("Location unavailable")
//            }
//    }
//}


object LocationHelper {

    fun getLocation(context: Context, callback: (String) -> Unit) {

        val client = LocationServices.getFusedLocationProviderClient(context)

        try {
            client.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        callback(
                            "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                        )
                    } else {
                        callback("Location unavailable")
                    }
                }
                .addOnFailureListener {
                    callback("Location unavailable")
                }
        } catch (e: Exception) {
            callback("Location unavailable")
        }
    }
}

