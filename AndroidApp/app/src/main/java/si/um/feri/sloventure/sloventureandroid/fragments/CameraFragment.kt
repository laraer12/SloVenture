package si.um.feri.sloventure.sloventureandroid.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import si.um.feri.sloventure.sloventureandroid.SloVentureApplication
import si.um.feri.sloventure.sloventureandroid.sensors.CameraController
import si.um.feri.sloventure.sloventureandroid.sensors.SensorDataManager
import si.um.feri.sloventure.sloventureandroid.databinding.FragmentCameraBinding
import si.um.feri.sloventure.sloventureandroid.sensors.LocationProvider
import si.um.feri.sloventure.sloventureandroid.sensors.OrientationProvider
import si.um.feri.sloventure.sloventureandroid.sensors.WeatherProvider

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraController: CameraController
    private lateinit var sensorDataManager: SensorDataManager
    private lateinit var app: SloVentureApplication

    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val cameraPermissionCode = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireActivity().application as SloVentureApplication
        sensorDataManager = SensorDataManager(
            locationProvider = LocationProvider(requireContext()),
            orientationProvider = OrientationProvider(requireContext()),
            weatherProvider = WeatherProvider()
        )
        cameraController = CameraController(requireActivity())

        if (allPermissionsGranted()) {
            cameraController.startCamera(binding.previewView)
        } else {
            requestPermissions(requiredPermissions, cameraPermissionCode)
        }

        binding.btnSwitchCamera.setOnClickListener {
            cameraController.switchCamera(binding.previewView)
        }

        binding.btnCapturePhoto.setOnClickListener {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    requireContext(),
                    "Please grant all permissions!",
                    Toast.LENGTH_SHORT
                ).show()
                requestPermissions(requiredPermissions, cameraPermissionCode)
                return@setOnClickListener
            }

            sensorDataManager.locationProvider.isLocationEnabled { enabled ->
                if (!enabled) {
                    Toast.makeText(
                        requireContext(),
                        "Location is OFF. Enable GPS.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@isLocationEnabled
                }

                capturePhotoAndPublish()
            }
        }
    }

    private fun capturePhotoAndPublish() {
        cameraController.capturePhoto { uri, timestamp ->
            if (uri == null) {
                Log.e("Camera", "Failed to capture photo")
                return@capturePhoto
            }
            requireContext()
            sensorDataManager.collectAllSensorData(
                requireContext(),
                uri,
                timestamp
            ) { photoPayload ->
                if (photoPayload != null) {
                    sensorDataManager.savePhotoPayloadAsJson(photoPayload, requireContext())
                    app.mqttClient.publishPhotoPayload(photoPayload)
                    app.markPhotoTaken()
                    app.photoData.add(photoPayload)

                    Toast.makeText(requireContext(), "Photo saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("Camera", "Failed to collect sensor data")
                }
            }
        }
    }


    private fun allPermissionsGranted() = requiredPermissions.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onStop() {
        super.onStop()
        cameraController.stopCamera()
        sensorDataManager.orientationProvider.stop()
        sensorDataManager.weatherProvider.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
