package si.um.feri.sloventure.sloventureandroid

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import si.um.feri.sloventure.sloventureandroid.databinding.ActivityMainBinding
import si.um.feri.sloventure.sloventureandroid.service.ProximityService
import si.um.feri.sloventure.sloventureandroid.util.PermissionHelper

class MainActivity : AppCompatActivity() {
    private val REQ_LOCATION = 100
    private val REQ_NOTIFICATIONS = 101
    private val REQ_CAMERA = 102

    private var proximityServiceStarted = false

    private lateinit var binding: ActivityMainBinding
    lateinit var app: SloVentureApplication


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestInitialPermissions()

        app = application as SloVentureApplication

        handleNavigationIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun requestInitialPermissions() {
        if (!PermissionHelper.hasLocationPermission(this)) {
            PermissionHelper.requestLocationPermission(this, REQ_LOCATION)
            return
        }
        if (!PermissionHelper.hasNotificationPermission(this)) {
            PermissionHelper.requestNotificationPermission(this, REQ_NOTIFICATIONS)
            return
        }
        if (!PermissionHelper.hasCameraPermission(this)) {
            PermissionHelper.requestCameraPermission(this, REQ_CAMERA)
            return
        }
        startProximityServiceIfNeeded()
    }

    private fun startProximityServiceIfNeeded() {
        if (proximityServiceStarted) return

        val intent = Intent(this, ProximityService::class.java)
        ContextCompat.startForegroundService(this, intent)
        proximityServiceStarted = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isEmpty()) return

        when (requestCode) {
            REQ_LOCATION,
            REQ_NOTIFICATIONS,
            REQ_CAMERA -> {
                requestInitialPermissions()
            }
        }
    }

    private fun handleNavigationIntent(intent: Intent?) {
        if (intent == null) return

        val destination = intent.getStringExtra("navigate_to")
        if (destination != "extreme_event") return

        val attractionId = intent.getStringExtra("attraction_id") ?: return
        val attractionName = intent.getStringExtra("attraction_name") ?: return
        val attractionLat =
            intent.getDoubleExtra("attraction_lat", 0.0).toFloat()
        val attractionLon =
            intent.getDoubleExtra("attraction_lon", 0.0).toFloat()


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                    as NavHostFragment

        val args = Bundle().apply {
            putString("attraction_id", attractionId)
            putString("attraction_name", attractionName)
            putFloat("attraction_lat", attractionLat)
            putFloat("attraction_lon", attractionLon)
        }

        navHostFragment.navController.navigate(
            R.id.extremeEventFragment,
            args
        )

        intent.removeExtra("navigate_to")
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                    as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}


/*
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorDataManager: SensorDataManager
    private val cameraPermissionCode = 1001
    private val notificationPermissionCode = 2001
    private lateinit var app: MyApplication
    private lateinit var mqttClient: MQTTClient

    // seznam zahtevanih permission-ov
    private val requiredPermissions = arrayOf(
        CAMERA,
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION,
        FOREGROUND_SERVICE_LOCATION
    )

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(
                arrayOf(POST_NOTIFICATIONS),
                notificationPermissionCode
            )
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(
                this,
                POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        else
            true
    }

    private fun openNotificationSettings() {
        val intent = Intent().apply {
            if (Build.VERSION.SDK_INT >= 26) {
                action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
            }
            else {
                action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = android.net.Uri.parse("package:$packageName")
            }
        }
        startActivity(intent)
    }

    val cameraController = CameraController(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.plant(Timber.DebugTree())

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!hasNotificationPermission())
            requestNotificationPermission()

        app = application as MyApplication

        mqttClient = app.mqttClient

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
                        sensorDataManager.savePhotoPayloadAsJson(
                            photoPayload,
                            this
                        ) // shranim vse podatke slike v JSON file

                        mqttClient.publishPhotoPayload(photoPayload)

                        // testiram, če so se slike pravilno shranile in preberem par podatkov
                        val allPhotosFromFile = sensorDataManager.loadAllPhotosFromFile(this)

                        allPhotosFromFile.forEach { photo ->
                            Timber.i("Loaded photo: ${photo.imageUri}, timestamp: ${photo.getFormattedTimestamp()}")
                        }

                        Toast.makeText(this, "Image saved!", Toast.LENGTH_SHORT).show()

                        app.markPhotoTaken()

                        Timber.i(
                            """
                            *** IMAGE INFO ***
                            URI: %s
                            Date and time: %s
                            Location -> LAT: %s, LON: %s
                            Phone orientation: %s
                            Temperature: %s °C
                            Weather description: %s
                            """.trimIndent(),
                            photoPayload.imageUri,
                            photoPayload.getFormattedTimestamp(),
                            photoPayload.latitude ?: "unknown",
                            photoPayload.longitude ?: "unknown",
                            photoPayload.orientation ?: "unknown",
                            photoPayload.temperature ?: "unknown",
                            photoPayload.weatherDescription ?: "unknown"
                        )
                    }
                }
            }
        }

        binding.btnSwitchCamera.setOnClickListener {
            cameraController.switchCamera(binding.previewView)
        }

        binding.btnGetAttractions.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    app.getAllAttractions()
                }
            }
            Log.i("idk", "location: ")
        }

        binding.swNotifs.isChecked = app.areNotificationsEnabled()

        binding.swNotifs.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!hasNotificationPermission()) {
                    Toast.makeText(this, getString(R.string.enable_notifs), Toast.LENGTH_LONG).show()

                    openNotificationSettings()
                    binding.swNotifs.isChecked = false
                    return@setOnCheckedChangeListener
                }
            }
            app.setNotificationsEnabled(isChecked)
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

        if (requestCode == notificationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Timber.i("Notifications allowed – starting service")

                ContextCompat.startForegroundService(
                    this,
                    Intent(this, ProximityService::class.java)
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        sensorDataManager.orientationProvider.start()
        // mqttClient.connect()
    }

    override fun onResume() {
        super.onResume()

        if (allPermissionsGranted())
            cameraController.startCamera(binding.previewView)

        if (hasNotificationPermission()) {
            ContextCompat.startForegroundService(
                this,
                Intent(this, ProximityService::class.java)
            )
        }
    }

    override fun onStop() {
        super.onStop()
        sensorDataManager.orientationProvider.stop()
        cameraController.stopCamera()
        sensorDataManager.weatherProvider.cancel()
        // mqttClient.disconnect()
    }
}*/