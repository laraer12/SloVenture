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
import si.um.feri.sloventure.sloventureandroid.R
import si.um.feri.sloventure.sloventureandroid.SloVentureApplication
import si.um.feri.sloventure.sloventureandroid.databinding.FragmentCrowdSimulationBinding
import si.um.feri.sloventure.sloventureandroid.model.CrowdSimulationPayload
import si.um.feri.sloventure.sloventureandroid.service.AutoCaptureService
import si.um.feri.sloventure.sloventureandroid.service.CrowdSimulationService
import si.um.feri.sloventure.sloventureandroid.service.SimulationService


class CrowdSimulationFragment : Fragment() {

    private var _binding: FragmentCrowdSimulationBinding? = null
    private val binding get() = _binding!!

    private lateinit var app: SloVentureApplication

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val payload =
                intent?.getParcelableExtra<CrowdSimulationPayload>("payload") ?: return

            binding.tvLastSent.text =
                getString(R.string.last_sent, payload.getFormattedTimestamp())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrowdSimulationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireActivity().application as SloVentureApplication

        setupAttractionSpinner()

        binding.etInterval.setText((app.crowdSimulationIntervalMs / 1000).toString())
        binding.etMinCrowd.setText(app.crowdMinPeople.toString())
        binding.etMaxCrowd.setText(app.crowdMaxPeople.toString())

        app.lastCrowdSimulationReading?.let {
            binding.tvLastSent.text =
                getString(R.string.last_sent, it.getFormattedTimestamp())
        }

        setCrowdControlsEnabled(!app.crowdSimulationRunning)

        binding.btnStartSimulation.setOnClickListener {
            if (!validate()) return@setOnClickListener
            startSimulation()
        }

        binding.btnStopSimulation.setOnClickListener {
            stopSimulation()
        }
    }

    private fun setupAttractionSpinner() {
        val labels = mutableListOf("Random attraction")
        labels += app.data.map { it.name }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            labels
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAttraction.adapter = adapter
    }

    private fun startSimulation() {

        val intervalMs =
            binding.etInterval.text.toString().toLong() * 1000

        val min = binding.etMinCrowd.text.toString().toInt()
        val max = binding.etMaxCrowd.text.toString().toInt().coerceAtMost(20)

        val selectedIndex = binding.spinnerAttraction.selectedItemPosition
        val attractionId =
            if (selectedIndex == 0) null
            else app.data[selectedIndex - 1].id

        app.crowdSimulationIntervalMs = intervalMs
        app.crowdMinPeople = min
        app.crowdMaxPeople = max
        app.crowdSimulationRunning = true


        // stop other simulations
        requireContext().stopService(
            Intent(requireContext(), SimulationService::class.java)
        )
        requireContext().stopService(
            Intent(requireContext(), AutoCaptureService::class.java)
        )


        val intent = Intent(requireContext(), CrowdSimulationService::class.java).apply {
            putExtra("intervalMs", intervalMs)
            putExtra("minCrowdNum", min)
            putExtra("maxCrowdNum", max)
            attractionId?.let { putExtra("attractionId", it) }
        }

        ContextCompat.startForegroundService(requireContext(), intent)
        app.crowdSimulationRunning = true
        setCrowdControlsEnabled(false)
        binding.btnStartSimulation.isEnabled = false
        binding.btnStopSimulation.isEnabled = true
    }

    private fun stopSimulation() {
        requireContext().stopService(
            Intent(requireContext(), CrowdSimulationService::class.java)
        )

        app.crowdSimulationRunning = false
        setCrowdControlsEnabled(true)

        binding.btnStartSimulation.isEnabled = true
        binding.btnStopSimulation.isEnabled = false
    }

    private fun validate(): Boolean {
        val min = binding.etMinCrowd.text.toString().toIntOrNull()
        val max = binding.etMaxCrowd.text.toString().toIntOrNull()
        val interval = binding.etInterval.text.toString().toLongOrNull()

        when {
            min == null || max == null -> {
                toast("Enter valid crowd numbers")
                return false
            }
            min < 0 || max > 20 -> {
                toast("Crowd must be between 0 and 20")
                return false
            }
            min > max -> {
                toast("Min people must be ≤ max people")
                return false
            }
            interval == null || interval <= 0 -> {
                toast("Invalid interval")
                return false
            }
            binding.spinnerAttraction.selectedItemPosition < 0 -> {
                toast("Select an attraction")
                return false
            }
        }
        return true
    }

    private fun setCrowdControlsEnabled(enabled: Boolean) {
        binding.etInterval.isEnabled = enabled
        binding.etMinCrowd.isEnabled = enabled
        binding.etMaxCrowd.isEnabled = enabled
        binding.spinnerAttraction.isEnabled = enabled

        binding.btnStartSimulation.isEnabled = enabled
        binding.btnStopSimulation.isEnabled = !enabled
    }


    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(
                updateReceiver,
                IntentFilter(CrowdSimulationService.ACTION_CROWD_SIMULATION_UPDATE)
            )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(updateReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
