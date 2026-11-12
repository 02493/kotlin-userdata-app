package com.example.unit1

import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.unit1.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private val chargerReceiver = ChargerReceiver()
    private var receiverRegistered = false

    private val chargerFilter = IntentFilter().apply {
        addAction(Intent.ACTION_POWER_CONNECTED)
        addAction(Intent.ACTION_POWER_DISCONNECTED)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.deviceInfoTextView.text = "Device: ${Build.MANUFACTURER} ${Build.MODEL}"

        // Новая кнопка-плашка для поиска на главном экране
        binding.searchBarLayout.setOnClickListener {
            openSearchEngine()
        }

        // Обработка нажатий на нижнюю навигацию
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_email -> {
                    openEmailClient()
                    true
                }
                R.id.nav_search -> {
                    openSearchEngine()
                    true
                }
                R.id.nav_events -> {
                    viewModel.onViewEvents()
                    true
                }
                R.id.nav_location -> {
                    viewModel.onViewLocation()
                    true
                }
                R.id.nav_exit -> {
                    viewModel.onExit()
                    true
                }
                else -> false
            }
        }

        observeUserData()
        observeNavigation()
    }

    private fun openEmailClient() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_SUBJECT, "Hello")
        }
        startActivity(Intent.createChooser(emailIntent, "Send email"))
    }

    private fun openSearchEngine() {
        val searchIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(searchIntent)
    }

    private fun observeUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userData.collect { data ->
                    val b = _binding ?: return@collect
                    b.userInfoTextView.text = "User: ${data.name ?: "N/A"}, Age: ${data.age ?: 0}"
                }
            }
        }
    }

    private fun observeNavigation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    if (_binding == null) return@collect
                    when (event) {
                        is HomeEvent.NavigateToEvents -> parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, EventsListFragment())
                            .addToBackStack(null)
                            .commit()
                        is HomeEvent.NavigateToLocation -> parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, LocationFragment())
                            .addToBackStack(null)
                            .commit()
                        is HomeEvent.NavigateToLogin -> parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, LoginFragment())
                            .commit()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!receiverRegistered) {
            ContextCompat.registerReceiver(
                requireContext(), chargerReceiver, chargerFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            receiverRegistered = true
        }
    }

    override fun onStop() {
        super.onStop()
        if (receiverRegistered) {
            try {
                requireContext().unregisterReceiver(chargerReceiver)
            } catch (e: IllegalArgumentException) {
                android.util.Log.w("HomeFragment", "Receiver not registered", e)
            }
            receiverRegistered = false
        }
    }

    override fun onResume() {
        super.onResume()
        updateBatteryLevel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateBatteryLevel() {
        val batteryIntent = requireActivity().registerReceiver(
            null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else -1
        if (_binding != null) {
            binding.batteryLevelTextView.text =
                if (batteryPct >= 0) "Battery level: $batteryPct%" else "Battery level: N/A"
        }
    }
}