package si.um.feri.sloventure.sloventureandroid

import android.os.Bundle
import android.widget.Toast
import android.content.pm.PackageManager
import android.Manifest.permission.CAMERA
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION

import si.um.feri.sloventure.sloventureandroid.core.SensorDataManager
import si.um.feri.sloventure.sloventureandroid.camera.CameraController
import si.um.feri.sloventure.sloventureandroid.weather.WeatherProvider
import si.um.feri.sloventure.sloventureandroid.location.LocationProvider
import si.um.feri.sloventure.sloventureandroid.sensors.OrientationProvider
import si.um.feri.sloventure.sloventureandroid.databinding.ActivityMainBinding

import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorDataManager: SensorDataManager
    private val cameraPermissionCode = 1001 // request code za permission dialog

    // seznam zahtevanih permission-ov
    private val requiredPermissions = arrayOf(
        CAMERA,
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION
    )

    val cameraController = CameraController(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.plant(Timber.DebugTree())

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorDataManager = SensorDataManager(
            locationProvider = LocationProvider(this),
            orientationProvider = OrientationProvider(this),
            weatherProvider = WeatherProvider()
        )

        // dovoljenje za kamero
        if (allPermissionsGranted())
            cameraController.startCamera(binding.previewView)

        else
            requestPermissions(requiredPermissions, cameraPermissionCode)

        binding.btnTestCamera.setOnClickListener {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, "Please grant all permissions!", Toast.LENGTH_SHORT).show()
                requestPermissions(requiredPermissions, cameraPermissionCode)
                return@setOnClickListener
            }

            // preverim, če je GPS omogočen, drugače slike ne zajamem
            sensorDataManager.locationProvider.isLocationEnabled { enabled ->
                if (!enabled) {
                    Toast.makeText(
                        this,
                        "Location is OFF. Please enable GPS to save photo location.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@isLocationEnabled
                }

                // zajem slike in shranjevanje v galerijo
                cameraController.capturePhoto { uri, timestamp ->
                    if (uri == null) {
                        Timber.e("Error saving image")
                        return@capturePhoto
                    }

                    // namesto, da vse posebej kličem, uporabim collectAllSensorData
                    sensorDataManager.collectAllSensorData(uri, timestamp) { photoPayload ->
                        if (photoPayload == null) {
                            Timber.e("Failed to collect sensor data")
                            return@collectAllSensorData
                        }
                        Toast.makeText(this, "Image saved!", Toast.LENGTH_SHORT).show()
                        Timber.i("*** IMAGE INFO ***\n" +
                                    "URI: ${photoPayload.imageUri}\n" +
                                    "Date and time: ${photoPayload.getFormattedTimestamp()}\n" +
                                    "Location -> LAT: ${photoPayload.latitude}, LON: ${photoPayload.longitude}\n" +
                                    "Phone orientation: ${photoPayload.orientation}\n" +
                                    "Temperature: ${photoPayload.temperature} °C\n" +
                                    "Weather description: ${photoPayload.weatherDescription}")
                    }
                }
            }
        }

        binding.btnSwitchCamera.setOnClickListener {
            cameraController.switchCamera(binding.previewView)
        }
    }

    private fun allPermissionsGranted() = requiredPermissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    // callback po uporabnikovem odgovoru na permission dialog
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // preverim če gre za naš request
        if (requestCode == cameraPermissionCode) {
            if (allPermissionsGranted())
                cameraController.startCamera(binding.previewView)

            else
                Toast.makeText(this, "Permissions not granted!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        sensorDataManager.orientationProvider.start()
    }

    override fun onStop() {
        super.onStop()
        sensorDataManager.orientationProvider.stop()
        cameraController.stopCamera()
        sensorDataManager.weatherProvider.cancel()
    }
}