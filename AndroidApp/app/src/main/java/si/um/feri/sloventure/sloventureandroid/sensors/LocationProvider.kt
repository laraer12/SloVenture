package si.um.feri.sloventure.sloventureandroid.sensors

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import timber.log.Timber

class LocationProvider(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fun getCurrentLocation(onResult: (Location?) -> Unit) {
        // "one-shot" preverim trenutno lokacijo
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null)
                    Timber.Forest.i("Got current location: ${location.latitude}, ${location.longitude}")

                else
                    Timber.Forest.w("Current location is null")

                onResult(location)
            }
            .addOnFailureListener {
                Timber.Forest.e("Failed to get location: $it")
                onResult(null)
            }
    }
}