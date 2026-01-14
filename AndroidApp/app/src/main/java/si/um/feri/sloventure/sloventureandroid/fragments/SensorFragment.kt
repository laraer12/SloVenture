
package si.um.feri.sloventure.sloventureandroid.fragments

import android.os.Bundle
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.Manifest.permission.POST_NOTIFICATIONS
import android.view.LayoutInflater
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import si.um.feri.sloventure.sloventureandroid.SloVentureApplication
import si.um.feri.sloventure.sloventureandroid.R
import si.um.feri.sloventure.sloventureandroid.core.MQTTClient
import si.um.feri.sloventure.sloventureandroid.databinding.FragmentSensorSettingsBinding
import si.um.feri.sloventure.sloventureandroid.model.SensorReading
import si.um.feri.sloventure.sloventureandroid.sensors.SensorAutoCaptureService
import java.util.Locale

class SensorFragment : Fragment() {

    private var _binding: FragmentSensorSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var app: SloVentureApplication
    private lateinit var mqttClient: MQTTClient

    private var captureIntervalMs: Long = 60000 // default 60s
    private val cameraPermissionCode = 1001
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    private val sensorUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val reading = intent?.getParcelableExtra<SensorReading>("reading")
            if (reading != null) {
                updateSensorUI(reading)}
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireActivity().application as SloVentureApplication
        mqttClient = app.mqttClient
        binding.btnStartAutoCapture.setOnClickListener {
            val intervalSec = binding.etInterval.text.toString().toLongOrNull()
            if (intervalSec == null || intervalSec <= 0) {
                Toast.makeText(requireContext(), "Enter a valid interval (>0)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            captureIntervalMs = intervalSec * 1000
            startAutoCaptureService(captureIntervalMs)
        }

        binding.btnStopAutoCapture.setOnClickListener {
            stopAutoCaptureService()
        }

    }

    private fun startAutoCaptureService(intervalMs: Long) {
        if (!allPermissionsGranted()) {
            Toast.makeText(requireContext(), "Please grant camera and location permissions!", Toast.LENGTH_SHORT).show()
            requestPermissions(requiredPermissions, cameraPermissionCode)
            return
        }

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(requireContext(), POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(POST_NOTIFICATIONS), cameraPermissionCode)
            Toast.makeText(requireContext(), "Please allow notifications to run auto capture.", Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent(requireContext(), SensorAutoCaptureService::class.java)
        intent.putExtra("intervalMs", intervalMs)
        ContextCompat.startForegroundService(requireContext(), intent)
    }


    private fun stopAutoCaptureService() {
        val intent = Intent(requireContext(), SensorAutoCaptureService::class.java)
        requireContext().stopService(intent)
    }


    private fun allPermissionsGranted(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun updateSensorUI(reading: SensorReading) {
        binding.tvLatitude.text =
            getString(R.string.latitude, formatCoord(reading.latitude))

        binding.tvLongitude.text =
            getString(R.string.longitude, formatCoord(reading.longitude))

        binding.tvTemperature.text =
            getString(
                R.string.temperature,
                reading.temperature?.let { "$it °C" } ?: "N/A"
            )

        binding.tvWeatherDescription.text =
            getString(R.string.weather, reading.weather ?: "N/A")

        binding.tvLastCapture.text =
            getString(
                R.string.last_sensor_update,
                reading.getFormattedTimestamp()
            )
    }

    private fun formatCoord(value: Double): String =
        String.format(Locale.US, "%.4f", value)

    override fun onStart() {
        super.onStart()

        val intentFilter = IntentFilter(SensorAutoCaptureService.ACTION_SENSOR_UPDATE)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(sensorUpdateReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(sensorUpdateReceiver)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
