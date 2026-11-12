package com.example.unit1

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface EventsUiState {
    data object Loading : EventsUiState
    data class Success(val events: List<CalendarEvent>) : EventsUiState
    data object Empty : EventsUiState
    data object CalendarUnavailable : EventsUiState
}

class EventsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    fun hasLoaded(): Boolean = _uiState.value !is EventsUiState.Loading

    fun loadCalendarEvents(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _uiState.value = EventsUiState.CalendarUnavailable
            return
        }

        _uiState.value = EventsUiState.Loading
        viewModelScope.launch {
            val events = withContext(Dispatchers.IO) { queryEvents(context) }
            _uiState.value = if (events.isEmpty()) {
                EventsUiState.Empty
            } else {
                EventsUiState.Success(events)
            }
        }
    }

    fun setCalendarUnavailable() {
        _uiState.value = EventsUiState.CalendarUnavailable
    }

    private fun queryEvents(context: Context): List<CalendarEvent> {
        val eventsList = mutableListOf<CalendarEvent>()

        val calendar = Calendar.getInstance()
        val startOfMonth = (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endOfMonth = (startOfMonth.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startOfMonth.timeInMillis)
        ContentUris.appendId(builder, endOfMonth.timeInMillis)

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN
        )

        val cursor = try {
            context.contentResolver.query(
                builder.build(), projection, null, null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )
        } catch (e: Exception) {
            null
        }

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        cursor?.use {
            val idIndex = it.getColumnIndex(CalendarContract.Instances.EVENT_ID)
            val titleIndex = it.getColumnIndex(CalendarContract.Instances.TITLE)
            val beginIndex = it.getColumnIndex(CalendarContract.Instances.BEGIN)

            while (it.moveToNext()) {
                val id = if (idIndex >= 0) it.getLong(idIndex) else 0L
                val title = if (titleIndex >= 0) it.getString(titleIndex) ?: "No Title" else "No Title"
                val begin = if (beginIndex >= 0) it.getLong(beginIndex) else 0L
                eventsList.add(CalendarEvent(id, title, formatter.format(Date(begin))))
            }
        }
        return eventsList
    }
}