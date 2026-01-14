package si.um.feri.sloventure.sloventureandroid.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import si.um.feri.sloventure.sloventureandroid.SloVentureApplication
import si.um.feri.sloventure.sloventureandroid.camera.CameraController
import si.um.feri.sloventure.sloventureandroid.databinding.FragmentExtremeEventBinding
import si.um.feri.sloventure.sloventureandroid.location.LocationProvider
import si.um.feri.sloventure.sloventureandroid.model.ExtremeEventPayload
import si.um.feri.sloventure.sloventureandroid.util.uriToBase64
import si.um.feri.sloventure.sloventureandroid.weather.WeatherProvider
import timber.log.Timber

class ExtremeEventFragment : Fragment() {

    private var _binding: FragmentExtremeEventBinding? = null
    private val binding get() = _binding!!

    private lateinit var app: SloVentureApplication
    private lateinit var cameraController: CameraController
    private lateinit var locationProvider: LocationProvider
    private lateinit var weatherProvider: WeatherProvider

    private lateinit var attractionId: String
    private lateinit var attractionName: String
    private var attractionLat: Double = 0.0
    private var attractionLon: Double = 0.0
    private val PHOTO_COOLDOWN_MS = 5 * 60 * 1000L // 5 minutes


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        attractionId = requireArguments().getString("attraction_id")!!
        attractionName = requireArguments().getString("attraction_name")!!
        attractionLat = requireArguments().getFloat("attraction_lat").toDouble()
        attractionLon = requireArguments().getFloat("attraction_lon").toDouble()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExtremeEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireActivity().application as SloVentureApplication
        cameraController = CameraController(requireActivity())
        locationProvider = LocationProvider(requireContext())
        weatherProvider = WeatherProvider()

        binding.btnCapturePhoto.setOnClickListener {
            if (app.wasPhotoTakenRecently(PHOTO_COOLDOWN_MS)) {
                Toast.makeText(
                    requireContext(),
                    "You can report another event in a few minutes.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            captureExtremeEvent()
        }
        binding.tvAttractionName.text = attractionName
    }

    private fun captureExtremeEvent() {
        cameraController.capturePhoto { uri, timestamp ->
            if (uri == null) {
                Timber.e("Failed to capture image")
                return@capturePhoto
            }

            locationProvider.getCurrentLocation { location ->
                if (location == null) {
                    Toast.makeText(requireContext(), "Location unavailable", Toast.LENGTH_SHORT)
                        .show()
                    return@getCurrentLocation
                }

                weatherProvider.getCurrentWeather(
                    location.latitude,
                    location.longitude
                ) { temperature, description ->

                    val base64 = uriToBase64(requireContext(), uri)

                    val payload = ExtremeEventPayload(
                        eventType = "crowd_detected",
                        attractionId = attractionId,
                        attractionName = attractionName,
                        attractionLatitude = attractionLat,
                        attractionLongitude = attractionLon,
                        userLatitude = location.latitude,
                        userLongitude = location.longitude,
                        timestamp = timestamp,
                        imageBase64 = base64,
                        temperature = temperature,
                        weatherDescription = description
                    )

                    app.mqttClient.publishExtremeEvent(payload)
                    app.markPhotoTaken()

                    showConfirmationUI()

                    Toast.makeText(
                        requireContext(),
                        "Extreme event reported!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showConfirmationUI() {
        cameraController.stopCamera()

        binding.previewView.visibility = View.GONE
        binding.btnCapturePhoto.isEnabled = false
        binding.btnCapturePhoto.visibility = View.GONE
        binding.tvConfirmation.visibility = View.VISIBLE

        binding.root.postDelayed({ //zapre se po treh sekundah
            if (isAdded) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }, 3000)
    }

    override fun onResume() {
        super.onResume()
        cameraController.startCamera(binding.previewView)
    }

    override fun onStop() {
        super.onStop()
        cameraController.stopCamera()
        weatherProvider.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
