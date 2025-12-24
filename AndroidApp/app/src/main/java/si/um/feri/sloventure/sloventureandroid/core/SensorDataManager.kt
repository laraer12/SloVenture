package si.um.feri.sloventure.sloventureandroid.core

import android.net.Uri

import si.um.feri.sloventure.sloventureandroid.model.PhotoPayload
import si.um.feri.sloventure.sloventureandroid.weather.WeatherProvider
import si.um.feri.sloventure.sloventureandroid.location.LocationProvider
import si.um.feri.sloventure.sloventureandroid.sensors.OrientationProvider

import timber.log.Timber

class SensorDataManager(
    val locationProvider: LocationProvider,
    val orientationProvider: OrientationProvider,
    val weatherProvider: WeatherProvider
) {
    fun collectAllSensorData(
        imageUri: Uri,
        timestamp: Long,
        onResult: (PhotoPayload?) -> Unit
    ) {
        locationProvider.fetchLocationAsync { location ->
            if (location == null) {
                Timber.e("Location is null, cannot collect sensor data")
                onResult(null)
                return@fetchLocationAsync
            }
            val latitude = location.latitude
            val longitude = location.longitude

            val orientation = orientationProvider.getCurrentOrientation()

            weatherProvider.getCurrentWeather(latitude, longitude) { temperature, description ->
                val photoPayload = PhotoPayload(
                    imageUri = imageUri,
                    timestamp = timestamp,
                    latitude = latitude,
                    longitude = longitude,
                    orientation = orientation,
                    temperature = temperature,
                    weatherDescription = description
                )
                onResult(photoPayload)
            }
        }
    }
}