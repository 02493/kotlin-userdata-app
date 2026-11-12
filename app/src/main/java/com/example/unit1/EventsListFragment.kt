package com.example.unit1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unit1.databinding.FragmentEventsListBinding
import kotlinx.coroutines.launch

class EventsListFragment : Fragment() {

    private var _binding: FragmentEventsListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventsViewModel by viewModels()
    private lateinit var calendarEventAdapter: CalendarEventAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.loadCalendarEvents(requireContext())
        } else {
            viewModel.setCalendarUnavailable()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarEventAdapter = CalendarEventAdapter()
        binding.eventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.eventsRecyclerView.adapter = calendarEventAdapter

        observeUiState()

        if (!viewModel.hasLoaded()) {
            if (hasCalendarPermission()) {
                viewModel.loadCalendarEvents(requireContext())
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val b = _binding ?: return@collect
                    when (state) {
                        is EventsUiState.Loading -> {
                            b.loadingProgressBar.visibility = View.VISIBLE
                            b.eventsRecyclerView.visibility = View.GONE
                            b.noEventsTextView.visibility = View.GONE
                            b.calendarNotAvailableTextView.visibility = View.GONE
                        }
                        is EventsUiState.Success -> {
                            b.loadingProgressBar.visibility = View.GONE
                            b.eventsRecyclerView.visibility = View.VISIBLE
                            b.noEventsTextView.visibility = View.GONE
                            b.calendarNotAvailableTextView.visibility = View.GONE
                            calendarEventAdapter.submitList(state.events)
                        }
                        is EventsUiState.Empty -> {
                            b.loadingProgressBar.visibility = View.GONE
                            b.eventsRecyclerView.visibility = View.GONE
                            b.noEventsTextView.visibility = View.VISIBLE
                            b.calendarNotAvailableTextView.visibility = View.GONE
                            calendarEventAdapter.submitList(emptyList())
                        }
                        is EventsUiState.CalendarUnavailable -> {
                            b.loadingProgressBar.visibility = View.GONE
                            b.eventsRecyclerView.visibility = View.GONE
                            b.noEventsTextView.visibility = View.GONE
                            b.calendarNotAvailableTextView.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }
}