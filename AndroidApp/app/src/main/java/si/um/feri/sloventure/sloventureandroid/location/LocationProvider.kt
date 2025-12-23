package si.um.feri.sloventure.sloventureandroid.location

import android.content.Context
import android.location.Location
import android.annotation.SuppressLint

import com.google.android.gms.location.*

import timber.log.Timber

class LocationProvider(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission") // suppressam, ker permission preverjam že drugje
    fun getCurrentLocation(onResult: (Location?) -> Unit) {

        // "one-shot" preverim trenutno lokacijo
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null)
                    Timber.i("Got current location: ${location.latitude}, ${location.longitude}")

                else
                    Timber.w("Current location is null")

                onResult(location)
            }
            .addOnFailureListener {
                Timber.e("Failed to get location: $it")
                onResult(null)
            }
    }

    // s tem preverim, če je lokacija res vključena, da potem lahko sliko zajamem
    fun isLocationEnabled(onResult: (Boolean) -> Unit) {
        val settingsClient = LocationServices.getSettingsClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0).build()
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(request)
            .setAlwaysShow(false)
            .build()

        settingsClient.checkLocationSettings(settingsRequest)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    // da uporabnik ne čaka na zajem slike medtem ko se shranjuje lokacija, zato se to zgodi naknadno
    fun fetchLocationAsync(onResult: (Location?) -> Unit) {
        getCurrentLocation { location ->
            onResult(location)
        }
    }
}