package si.um.feri.sloventure.sloventureandroid.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import si.um.feri.sloventure.sloventureandroid.R
import si.um.feri.sloventure.sloventureandroid.SloVentureApplication
import si.um.feri.sloventure.sloventureandroid.databinding.FragmentUserEventBinding
import si.um.feri.sloventure.sloventureandroid.model.UserEventPayload
import si.um.feri.sloventure.sloventureandroid.sensors.CameraController
import si.um.feri.sloventure.sloventureandroid.sensors.LocationProvider
import si.um.feri.sloventure.sloventureandroid.util.uriToBase64

class UserEventFragment : Fragment() {

    private var _binding: FragmentUserEventBinding? = null
    private val binding get() = _binding!!

    private lateinit var app: SloVentureApplication
    private lateinit var cameraController: CameraController
    private lateinit var locationProvider: LocationProvider

    private var capturedImageBase64: String? = null
    private var capturedTimestamp: Long = 0L

    private val eventTypes = listOf("info", "warning")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireActivity().application as SloVentureApplication
        cameraController = CameraController(requireActivity())
        locationProvider = LocationProvider(requireContext())

        setupSpinner()
        cameraController.startCamera(binding.previewView)

        binding.btnCapturePhoto.setOnClickListener {
            capturePhoto()
        }

        binding.btnSubmitEvent.setOnClickListener {
            submitEvent()
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            eventTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerEventType.adapter = adapter
    }

    private fun capturePhoto() {
        cameraController.capturePhoto { uri, timestamp ->
            if (uri == null) {
                Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
                return@capturePhoto
            }

            capturedImageBase64 = uriToBase64(requireContext(), uri)
            capturedTimestamp = timestamp

            binding.tvPhotoStatus.text = "Photo captured"
        }
    }

    private fun submitEvent() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        locationProvider.getCurrentLocation { location ->
            if (location == null) {
                Toast.makeText(requireContext(), "Location unavailable", Toast.LENGTH_SHORT).show()
                return@getCurrentLocation
            }

            val payload = UserEventPayload(
                eventType = eventTypes[binding.spinnerEventType.selectedItemPosition],
                title = title,
                description = description,
                latitude = location.latitude,
                longitude = location.longitude,
                timestamp = if (capturedTimestamp != 0L) capturedTimestamp else System.currentTimeMillis(),
                imageBase64 = capturedImageBase64
            )

            app.mqttClient.publishUserEvent(payload)

            showConfirmation()
        }
    }

    private fun showConfirmation() {
        cameraController.stopCamera()

        binding.previewView.visibility = View.GONE
        binding.btnCapturePhoto.visibility = View.GONE
        binding.btnSubmitEvent.isEnabled = false
        binding.tvConfirmation.visibility = View.VISIBLE

        binding.root.postDelayed({
            if (isAdded) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }, 3000)
    }

    override fun onStop() {
        super.onStop()
        cameraController.stopCamera()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}