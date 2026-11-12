package com.example.unit1

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface HomeEvent {
    data object NavigateToEvents : HomeEvent
    data object NavigateToLocation : HomeEvent
    data object NavigateToLogin : HomeEvent
}

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val userDataStore = UserDataStore(app)

    val userData: StateFlow<UserData> = userDataStore.userData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserData(null, null)
    )

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events: Flow<HomeEvent> = _events.receiveAsFlow()

    fun onViewEvents() { viewModelScope.launch { _events.send(HomeEvent.NavigateToEvents) } }
    fun onViewLocation() { viewModelScope.launch { _events.send(HomeEvent.NavigateToLocation) } }

    fun onExit() {
        viewModelScope.launch {
            userDataStore.clearUserData()
            _events.send(HomeEvent.NavigateToLogin)
        }
    }
}