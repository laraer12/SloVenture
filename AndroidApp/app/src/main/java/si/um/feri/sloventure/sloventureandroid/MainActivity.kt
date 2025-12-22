package si.um.feri.sloventure.sloventureandroid

import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import android.content.pm.PackageManager
import android.Manifest.permission.CAMERA
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity

import si.um.feri.sloventure.sloventureandroid.model.PhotoPayload
import si.um.feri.sloventure.sloventureandroid.core.SensorDataManager
import si.um.feri.sloventure.sloventureandroid.camera.CameraController
import si.um.feri.sloventure.sloventureandroid.weather.WeatherProvider
import si.um.feri.sloventure.sloventureandroid.location.LocationProvider
import si.um.feri.sloventure.sloventureandroid.sensors.OrientationProvider
import si.um.feri.sloventure.sloventureandroid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorDataManager: SensorDataManager
    private val cameraPermissionCode = 1001 // request code za permission dialog
    private val requiredPermissions = arrayOf(CAMERA) // seznam zahtevanih permission-ov

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorDataManager = SensorDataManager(
            cameraController = CameraController(this),
            locationProvider = LocationProvider(this),
            orientationProvider = OrientationProvider(this),
            weatherProvider = WeatherProvider(this)
        )

        // dovoljenje za kamero
        if (allPermissionsGranted())
            sensorDataManager.cameraController.startCamera(binding.previewView)

        else
            requestPermissions(requiredPermissions, cameraPermissionCode)

        binding.btnTestCamera.setOnClickListener {
            if (allPermissionsGranted()) {

                // zajem slike in shranjevanje v galerijo
                sensorDataManager.cameraController.capturePhoto("test_photo.jpg") { uri, timestamp ->
                    if (uri != null) {
                        val photoPayload = PhotoPayload(
                            imageUri = uri,
                            timestamp = timestamp,

                            // TODO
                            latitude = null,
                            longitude = null,
                            orientation = null,
                            temperature = null,
                            weatherDescription = null
                        )
                        Toast.makeText(this, "Image saved: URI-$uri", Toast.LENGTH_SHORT).show()

                        // toast za testiranje, če vse dela, kratek delay da vidim podatke uri in timestamp
                        val duration = 2000L

                        Handler(mainLooper).postDelayed({
                            Toast.makeText(this, "Time-${photoPayload.getFormattedTimestamp()}", Toast.LENGTH_SHORT).show()
                        }, duration)
                    }
                    else
                        Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show()
                }
            }
            else
                Toast.makeText(this, "CAMERA permission not granted!", Toast.LENGTH_SHORT).show()
        }

        binding.btnSwitchCamera.setOnClickListener {
            sensorDataManager.cameraController.switchCamera(binding.previewView)
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
                sensorDataManager.cameraController.startCamera(binding.previewView)

            else
                Toast.makeText(this, "CAMERA permission not granted!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        sensorDataManager.cameraController.stopCamera()
    }
}