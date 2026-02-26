package com.dashkin.busradar.feature.map.presentation.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dashkin.busradar.feature.map.R
import com.dashkin.busradar.feature.map.databinding.FragmentMapBinding
import com.dashkin.busradar.feature.map.di.MapComponentProvider
import com.dashkin.busradar.feature.map.domain.model.BusPosition
import com.dashkin.busradar.feature.map.presentation.state.MapUiState
import com.dashkin.busradar.feature.map.presentation.viewmodel.MapViewModel
import com.dashkin.busradar.feature.map.presentation.viewmodel.MapViewModelFactory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import javax.inject.Inject

// Integrates Google Maps with a SupportMapFragment child, polls bus positions
// every 10 seconds via MapViewModel, and renders markers on the map.
// Location permission is requested at runtime; the My Location FAB centres
// the camera on the user's position once permission is granted.
class MapFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: MapViewModelFactory

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MapViewModel by viewModels { viewModelFactory }

    private var googleMap: GoogleMap? = null
    private val busMarkers = mutableMapOf<String, Marker>()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            enableMyLocation()
        } else {
            showLocationPermissionDeniedMessage()
        }
    }

    // region Lifecycle

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as MapComponentProvider)
            .injectMapFragment(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMapFragment()
        setupRetryButton()
        setupFab()
    }

    // Issue #1 fix: clear markers before nulling googleMap so Marker.remove() can succeed
    override fun onDestroyView() {
        super.onDestroyView()
        busMarkers.values.forEach { it.remove() }
        busMarkers.clear()
        googleMap = null
        _binding = null
    }

    // endregion

    // region Map initialisation

    private fun initMapFragment() {
        val existingMap = childFragmentManager
            .findFragmentByTag(TAG_MAP_FRAGMENT) as? SupportMapFragment
        val mapFragment = existingMap ?: SupportMapFragment.newInstance().also { fragment ->
            childFragmentManager.beginTransaction()
                .replace(R.id.map_container, fragment, TAG_MAP_FRAGMENT)
                .commit()
        }
        mapFragment.getMapAsync(::onMapReady)
    }

    private fun onMapReady(map: GoogleMap) {
        googleMap = map
        configureMap(map)
        requestLocationPermissionIfNeeded()
        observeUiState()
    }

    private fun configureMap(map: GoogleMap) {
        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = false
        }
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(LONDON_LATLNG, DEFAULT_ZOOM),
        )
        // Issue #2 fix: replaced labeled return with if/else expression
        map.setOnMarkerClickListener { marker ->
            val vehicleId = marker.tag as? String
            if (vehicleId == null) {
                false
            } else {
                (activity as? OnBusSelectedListener)?.onBusSelected(vehicleId)
                true
            }
        }
    }

    // endregion

    // region UI state

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::renderState)
            }
        }
    }

    private fun renderState(state: MapUiState) {
        when (state) {
            is MapUiState.Loading -> showLoading()
            is MapUiState.Success -> showBuses(state.buses)
            is MapUiState.Empty -> showEmpty()
            is MapUiState.Error -> showError(state.message)
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.errorView.isVisible = false
    }

    private fun showBuses(buses: List<BusPosition>) {
        binding.progressBar.isVisible = false
        binding.errorView.isVisible = false
        updateBusMarkers(buses)
    }

    private fun showEmpty() {
        binding.progressBar.isVisible = false
        binding.errorView.isVisible = true
        binding.errorText.text = getString(R.string.map_no_buses)
    }

    private fun showError(message: String) {
        binding.progressBar.isVisible = false
        binding.errorView.isVisible = true
        binding.errorText.text = message
    }

    // endregion

    // region Marker management

    private fun updateBusMarkers(buses: List<BusPosition>) {
        val map = googleMap ?: return
        val incomingIds = buses.map { it.vehicleId }.toSet()
        removeStaleMarkers(incomingIds)
        buses.forEach { bus -> addOrUpdateMarker(map, bus) }
    }

    private fun removeStaleMarkers(activeIds: Set<String>) {
        val staleIds = busMarkers.keys - activeIds
        staleIds.forEach { id -> busMarkers.remove(id)?.remove() }
    }

    private fun addOrUpdateMarker(map: GoogleMap, bus: BusPosition) {
        val position = LatLng(bus.latitude, bus.longitude)
        val existing = busMarkers[bus.vehicleId]
        if (existing != null) {
            existing.position = position
            existing.rotation = bus.bearing ?: 0f
        } else {
            val marker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title("Route ${bus.routeId}")
                    .snippet(getString(R.string.map_bus_marker_snippet))
                    .rotation(bus.bearing ?: 0f)
                    .flat(true),
            )
            if (marker != null) {
                marker.tag = bus.vehicleId
                busMarkers[bus.vehicleId] = marker
            }
        }
    }

    // endregion

    // region Location permission

    private fun requestLocationPermissionIfNeeded() {
        if (hasLocationPermission()) {
            enableMyLocation()
        } else {
            locationPermissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        googleMap?.isMyLocationEnabled = true
    }

    private fun showLocationPermissionDeniedMessage() {
        Snackbar.make(
            binding.root,
            R.string.map_location_permission_denied,
            Snackbar.LENGTH_LONG,
        ).show()
    }

    // endregion

    // region FAB

    // Issue #3 fix: FAB now moves camera to user's last known location via FusedLocationProviderClient
    private fun setupFab() {
        binding.fabMyLocation.setOnClickListener {
            if (hasLocationPermission()) {
                moveToMyLocation()
            } else {
                locationPermissionLauncher.launch(LOCATION_PERMISSIONS)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun moveToMyLocation() {
        enableMyLocation()
        val map = googleMap ?: return
        LocationServices.getFusedLocationProviderClient(requireActivity())
            .lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            DEFAULT_ZOOM,
                        ),
                    )
                }
            }
    }

    // endregion

    // region Retry

    private fun setupRetryButton() {
        binding.retryButton.setOnClickListener { viewModel.retry() }
    }

    // endregion

    // Callback interface for notifying the host Activity that a bus marker was tapped.
    // MainActivity implements this to open BusDetailFragment.
    interface OnBusSelectedListener {
        fun onBusSelected(vehicleId: String)
    }

    companion object {
        const val TAG = "MapFragment"
        private const val TAG_MAP_FRAGMENT = "SupportMapFragment"

        private val LONDON_LATLNG = LatLng(51.5074, -0.1278)
        private const val DEFAULT_ZOOM = 12f

        private val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }
}
