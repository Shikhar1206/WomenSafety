import android.content.Context
import com.google.android.gms.location.LocationServices

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

