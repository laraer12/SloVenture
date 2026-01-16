package si.um.feri.sloventure.sloventureandroid.sensors

import si.um.feri.sloventure.sloventureandroid.model.SensorReading

class SensorReadingCollector(
    private val locationProvider: LocationProvider,
    private val weatherProvider: WeatherProvider
) {
    fun collect(onResult: (SensorReading?) -> Unit) {
        locationProvider.getCurrentLocation { location ->
            if (location == null) {
                onResult(null)
                return@getCurrentLocation
            }

            val timestamp = System.currentTimeMillis()

            weatherProvider.getCurrentWeather(
                location.latitude,
                location.longitude
            ) { temperature, description ->

                val reading = SensorReading(
                    timestamp = timestamp,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    temperature = temperature,
                    weather = description
                )

                onResult(reading)
            }
        }
    }
}
