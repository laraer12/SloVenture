package si.um.feri.sloventure.sloventureandroid.core

import si.um.feri.sloventure.sloventureandroid.camera.CameraController
import si.um.feri.sloventure.sloventureandroid.location.LocationProvider
import si.um.feri.sloventure.sloventureandroid.sensors.OrientationProvider
import si.um.feri.sloventure.sloventureandroid.weather.WeatherProvider

class SensorDataManager(
    val cameraController: CameraController,
    val locationProvider: LocationProvider,
    private val orientationProvider: OrientationProvider,
    private val weatherProvider: WeatherProvider
) {
    fun collectAllSensorData() {
        // TODO: implementiraj senzor za zajem vseh podatkov
    }
}