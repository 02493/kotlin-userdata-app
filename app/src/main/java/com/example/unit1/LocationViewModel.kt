package com.example.unit1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class LocationViewModel : ViewModel() {

    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude: StateFlow<Double?> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude: StateFlow<Double?> = _longitude.asStateFlow()

    val location: StateFlow<Pair<Double, Double>?> =
        combine(_latitude, _longitude) { lat, lng ->
            if (lat != null && lng != null) lat to lng else null
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val hasLocation: Boolean
        get() = _latitude.value != null && _longitude.value != null

    fun setLocation(latitude: Double, longitude: Double) {
        _latitude.value = latitude
        _longitude.value = longitude
    }

    fun clearLocation() {
        _latitude.value = null
        _longitude.value = null
    }
}