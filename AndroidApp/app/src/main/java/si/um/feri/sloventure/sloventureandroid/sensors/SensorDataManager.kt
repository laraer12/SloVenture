package si.um.feri.sloventure.sloventureandroid.sensors

import android.content.Context
import android.net.Uri
import kotlinx.serialization.json.Json
import si.um.feri.sloventure.sloventureandroid.sensors.LocationProvider
import si.um.feri.sloventure.sloventureandroid.model.PhotoPayload
import si.um.feri.sloventure.sloventureandroid.util.uriToBase64
import si.um.feri.sloventure.sloventureandroid.sensors.WeatherProvider
import timber.log.Timber
import java.io.File

class SensorDataManager(
    val locationProvider: LocationProvider,
    val orientationProvider: OrientationProvider,
    val weatherProvider: WeatherProvider
) {

    fun collectAllSensorData(
        context: Context,
        imageUri: Uri,
        timestamp: Long,
        onResult: (PhotoPayload?) -> Unit
    ) {
        locationProvider.fetchLocationAsync { location ->
            if (location == null) {
                Timber.Forest.e("Location is null, cannot collect sensor data")
                onResult(null)
                return@fetchLocationAsync
            }
            val latitude = location.latitude
            val longitude = location.longitude

            val orientation = orientationProvider.getCurrentOrientation()

            weatherProvider.getCurrentWeather(latitude, longitude) { temperature, description ->
                val photoPayload = PhotoPayload(
                    imageUri = imageUri.toString(),
                    timestamp = timestamp,
                    latitude = latitude,
                    longitude = longitude,
                    orientation = orientation,
                    temperature = temperature,
                    weatherDescription = description
                )
                // TU DODANO ZA BASE64
                val base64Image = uriToBase64(context, imageUri)
                val photoPayloadWithImage = photoPayload.copy(imageBase64 = base64Image)

                onResult(photoPayloadWithImage) // TO SPREMENJENO
            }
        }
    }

    fun collectSensorDataOnly(onResult: (PhotoPayload?) -> Unit) {
        locationProvider.fetchLocationAsync { location ->
            if (location == null) {
                Timber.Forest.e("Location is null, cannot collect sensor data")
                onResult(null)
                return@fetchLocationAsync
            }
            val latitude = location.latitude
            val longitude = location.longitude


            weatherProvider.getCurrentWeather(latitude, longitude) { temperature, description ->
                val sensorPayload = PhotoPayload(
                    imageUri = "", // brez slike
                    timestamp = System.currentTimeMillis(),
                    latitude = latitude,
                    longitude = longitude,
                    orientation = null, //brez orientacije
                    temperature = temperature,
                    weatherDescription = description
                )
                onResult(sensorPayload)
            }
        }
    }

    fun savePhotoPayloadAsJson(
        photoPayload: PhotoPayload,
        context: Context,
        filename: String = "photos.json"
    ) {
        try {
            val jsonString = Json.Default.encodeToString(photoPayload)
            val file = File(context.filesDir, filename)

            if (!file.exists())
                file.createNewFile()

            // dodam vrstico po vrstico novo sliko, ne prepisujem podatkov
            file.appendText(jsonString + "\n")

            Timber.Forest.e("Photo saved successfully! JSON: $jsonString")
        } catch (ex: Exception) {
            Timber.Forest.e("Failed to save JSON: $ex")
        }
    }

    fun loadAllPhotosFromFile(
        context: Context,
        filename: String = "photos.json"
    ): List<PhotoPayload> {
        val file = File(context.filesDir, filename)

        if (!file.exists()) {
            Timber.Forest.w("$filename does not exist, returning empty list")
            return emptyList()
        }
        val photos = mutableListOf<PhotoPayload>()

        try {
            file.forEachLine { line ->
                if (line.isNotBlank()) {
                    try {
                        val photo = Json.Default.decodeFromString<PhotoPayload>(line)
                        photos.add(photo)
                    } catch (ex: Exception) {
                        Timber.Forest.e(ex, "Failed to parse line: $line")
                    }
                }
            }
        } catch (ex: Exception) {
            Timber.Forest.e(ex, "Failed to read $filename")
        }
        return photos
    }
}