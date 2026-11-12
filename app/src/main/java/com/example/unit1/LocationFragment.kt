package com.example.unit1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.unit1.databinding.FragmentLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class LocationFragment : Fragment() {

    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LocationViewModel by viewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getLastLocation()
        } else {
            showNoPermission()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (hasLocationPermission()) {
            getLastLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        binding.viewOnMapButton.setOnClickListener { openMap() }

        observeLocation()
    }

   private fun observeLocation() {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.location.collect { pair ->
                if (pair == null) return@collect
                val (lat, lng) = pair
                renderLocation(lat, lng)
            }
        }
    }
}

    override fun onResume() {
        super.onResume()
        if (::fusedLocationClient.isInitialized && hasLocationPermission()) {
            getLastLocation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getLastLocation() {
        try {
            if (!::fusedLocationClient.isInitialized) return
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        viewModel.setLocation(location.latitude, location.longitude)
                    } else if (!viewModel.hasLocation) {
                        showLocationUnavailable()
                    }
                }
                .addOnFailureListener {
                    if (!viewModel.hasLocation) showLocationUnavailable()
                }
        } catch (e: SecurityException) {
            if (!viewModel.hasLocation) showNoPermission()
        }
    }

    private fun renderLocation(latitude: Double, longitude: Double) {
        val b = _binding ?: return
        b.locationNotAvailableTextView.visibility = View.GONE
        b.locationCoordinatesTextView.visibility = View.VISIBLE
        b.viewOnMapButton.visibility = View.VISIBLE
        b.locationCoordinatesTextView.text = "Latitude: $latitude\nLongitude: $longitude"
    }

    private fun showNoPermission() {
        val b = _binding ?: return
        b.locationNotAvailableTextView.visibility = View.VISIBLE
        b.locationCoordinatesTextView.visibility = View.GONE
        b.viewOnMapButton.visibility = View.GONE
    }

    private fun showLocationUnavailable() {
        val b = _binding ?: return
        b.locationNotAvailableTextView.visibility = View.VISIBLE
        b.locationCoordinatesTextView.visibility = View.GONE
        b.viewOnMapButton.visibility = View.GONE
    }

    private fun openMap() {
        val latitude = viewModel.latitude.value
        val longitude = viewModel.longitude.value
        if (latitude == null || longitude == null) {
            Toast.makeText(requireContext(), "Location is not available yet", Toast.LENGTH_SHORT).show()
            return
        }
        val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(Current location)")
        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
        val pm = requireActivity().packageManager
        if (mapIntent.resolveActivity(pm) != null) {
            startActivity(Intent.createChooser(mapIntent, "View location"))
        } else {
            Toast.makeText(requireContext(), "No map applications available", Toast.LENGTH_SHORT).show()
        }
    }
}