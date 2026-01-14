package si.um.feri.sloventure.sloventureandroid.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import si.um.feri.sloventure.sloventureandroid.R
import si.um.feri.sloventure.sloventureandroid.SloVentureApplication
import si.um.feri.sloventure.sloventureandroid.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var app: SloVentureApplication

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireActivity().application as SloVentureApplication

        binding.btnUpdateAttractionCount.setOnClickListener {
            updateAttractionCount()
        }


        binding.btnGoToSensorSettings.setOnClickListener {
            findNavController().navigate(R.id.sensorSettingsFragment)
        }

        binding.btnGoToEvents.setOnClickListener {
            findNavController().navigate(R.id.eventsFragment)
        }

        binding.btnGoToSimulation.setOnClickListener {
            findNavController().navigate(R.id.simulationFragment)
        }
    }

    private fun updateAttractionCount() {
        lifecycleScope.launch {
            app.getAllAttractions()
            binding.tvAttractionCount.text =
                getString(R.string.attractions_loaded, app.data.size)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
