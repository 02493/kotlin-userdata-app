package com.example.unit1

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface LoginEvent {
    data object NavigateToHome : LoginEvent
}

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val userDataStore = UserDataStore(app)

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    fun saveAndProceed(name: String, age: Int) {
        viewModelScope.launch {
            userDataStore.saveUserData(name, age)
            _events.send(LoginEvent.NavigateToHome)
        }
    }
}