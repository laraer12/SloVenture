package si.um.feri.sloventure.sloventureandroid.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import si.um.feri.sloventure.sloventureandroid.R
import si.um.feri.sloventure.sloventureandroid.SloVentureApplication
import si.um.feri.sloventure.sloventureandroid.databinding.FragmentSimulationBinding
import si.um.feri.sloventure.sloventureandroid.model.SensorReading
import si.um.feri.sloventure.sloventureandroid.service.AutoCaptureService
import si.um.feri.sloventure.sloventureandroid.service.SimulationService

class SimulationFragment : Fragment() {

    private var _binding: FragmentSimulationBinding? = null
    private val binding get() = _binding!!

    private lateinit var app: SloVentureApplication

    private var selectedLat = 46.55472
    private var selectedLon = 15.64667
    private val weatherOptions = listOf(
        "Clear sky",
        "Partly cloudy",
        "Fog",
        "Drizzle",
        "Freezing Drizzle",
        "Rain",
        "Freezing Rain",
        "Snow fall",
        "Snow grains",
        "Rain showers",
        "Snow showers",
        "Thunderstorm",
        "Thunderstorm with hail"
    )

    private val simulationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val reading =
                intent?.getParcelableExtra<SensorReading>("reading") ?: return

            binding.tvLastSent.text =
                getString(R.string.last_sent, reading.getFormattedTimestamp())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSimulationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireActivity().application as SloVentureApplication
        setupMap()
        setupWeatherSpinner()

        binding.etInterval.setText((app.simulationIntervalMs / 1000).toString())
        binding.etMinTemp.setText(app.simulationMinTemp.toString())
        binding.etMaxTemp.setText(app.simulationMaxTemp.toString())
        binding.spinnerWeather.setSelection(app.simulationWeatherIndex)

        app.lastSimulationReading?.let { reading ->
            binding.tvLastSent.text =
                getString(R.string.last_sent, reading.getFormattedTimestamp())
        }

        selectedLat = app.simulationLat
        selectedLon = app.simulationLon

        binding.etLat.setText(selectedLat.toString())
        binding.etLon.setText(selectedLon.toString())

        binding.btnStartSimulation.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener
            startSimulation()
        }

        binding.btnStopSimulation.setOnClickListener {
            stopSimulation()
        }
    }

    private fun setupMap() {
        Configuration.getInstance()
            .load(requireContext(), requireActivity().getPreferences(0))

        val map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(14.0)

        val startPoint = GeoPoint(selectedLat, selectedLon)
        map.controller.setCenter(startPoint)

        val marker = Marker(map).apply {
            position = startPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        map.overlays.add(marker)

        val receiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                selectedLat = p.latitude
                selectedLon = p.longitude
                marker.position = p
                map.invalidate()
                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean = false
        }

        map.overlays.add(MapEventsOverlay(receiver))
    }

    private fun startSimulation() {

        val minTemp = binding.etMinTemp.text.toString().toDouble()
        val maxTemp = binding.etMaxTemp.text.toString().toDouble()
        val intervalMs = binding.etInterval.text.toString().toLong() * 1000
        val weatherIndex = binding.spinnerWeather.selectedItemPosition
        val weather = weatherOptions[weatherIndex]

        selectedLat =
            binding.etLat.text.toString().toDoubleOrNull() ?: selectedLat

        selectedLon =
            binding.etLon.text.toString().toDoubleOrNull() ?: selectedLon

        app.simulationIntervalMs = intervalMs
        app.simulationMinTemp = minTemp
        app.simulationMaxTemp = maxTemp
        app.simulationWeatherIndex = weatherIndex
        app.simulationLat = selectedLat
        app.simulationLon = selectedLon

        binding.etInterval.isEnabled = false
        binding.etMinTemp.isEnabled = false
        binding.etMaxTemp.isEnabled = false
        binding.spinnerWeather.isEnabled = false

        stopRealSensorCapture()

        val intent = Intent(requireContext(), SimulationService::class.java).apply {
            putExtra("minTemp", minTemp)
            putExtra("maxTemp", maxTemp)
            putExtra("weather", weather)
            putExtra("lat", selectedLat)
            putExtra("lon", selectedLon)
            putExtra("intervalMs", intervalMs)
        }

        ContextCompat.startForegroundService(requireContext(), intent)
        binding.btnStartSimulation.isEnabled = false
        binding.btnStopSimulation.isEnabled = true
    }

    private fun setupWeatherSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            weatherOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerWeather.adapter = adapter
    }

    private fun validateInputs(): Boolean {

        val minTemp = binding.etMinTemp.text.toString().toDoubleOrNull()
        val maxTemp = binding.etMaxTemp.text.toString().toDoubleOrNull()
        val interval = binding.etInterval.text.toString().toLongOrNull()

        when {
            minTemp == null || maxTemp == null -> {
                Toast.makeText(
                    requireContext(),
                    "Enter valid temperature range",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }

            minTemp >= maxTemp -> {
                Toast.makeText(
                    requireContext(),
                    "Min temperature must be lower than max",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }

            interval == null || interval <= 0 -> {
                Toast.makeText(
                    requireContext(),
                    "Enter valid interval",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }
        return true
    }


    private fun stopSimulation() {
        val intent = Intent(requireContext(), SimulationService::class.java)
        requireContext().stopService(intent)
        binding.btnStartSimulation.isEnabled = true
        binding.btnStopSimulation.isEnabled = false
        binding.etInterval.isEnabled = true
        binding.etMinTemp.isEnabled = true
        binding.etMaxTemp.isEnabled = true
        binding.spinnerWeather.isEnabled = true
    }

    private fun stopRealSensorCapture() {
        val intent = Intent(requireContext(), AutoCaptureService::class.java)
        requireContext().stopService(intent)
    }

    override fun onStart() {
        super.onStart()
        val filter =
            IntentFilter(SimulationService.ACTION_SIMULATION_UPDATE)
        LocalBroadcastManager
            .getInstance(requireContext())
            .registerReceiver(simulationReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager
            .getInstance(requireContext())
            .unregisterReceiver(simulationReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
