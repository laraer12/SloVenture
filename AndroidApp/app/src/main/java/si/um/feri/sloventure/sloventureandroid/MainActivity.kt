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

        startService(Intent(this, ProximityService::class.java))
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