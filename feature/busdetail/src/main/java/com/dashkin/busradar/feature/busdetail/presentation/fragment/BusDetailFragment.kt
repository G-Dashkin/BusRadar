package com.dashkin.busradar.feature.busdetail.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dashkin.busradar.feature.busdetail.databinding.FragmentBusDetailBinding

class BusDetailFragment : Fragment() {

    private var _binding: FragmentBusDetailBinding? = null
    private val binding get() = _binding!!

    private val vehicleId: String by lazy {
        requireArguments().getString(ARG_VEHICLE_ID).orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentBusDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "BusDetailFragment"
        private const val ARG_VEHICLE_ID = "vehicle_id"

        fun newInstance(vehicleId: String): BusDetailFragment =
            BusDetailFragment().apply {
                arguments = Bundle().apply { putString(ARG_VEHICLE_ID, vehicleId) }
            }
    }
}
