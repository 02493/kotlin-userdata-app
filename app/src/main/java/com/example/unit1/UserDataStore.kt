package com.example.unit1

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class UserData(val name: String?, val age: Int?)

class UserDataStore(private val context: Context) {

    companion object {
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_AGE_KEY = intPreferencesKey("user_age")

        private const val SYNC_PREFS = "user_prefs_sync"
        private const val SYNC_NAME = "user_name"
        private const val SYNC_AGE = "user_age"
    }

    val userData: Flow<UserData> = context.userDataStore.data.map { prefs ->
        UserData(prefs[USER_NAME_KEY], prefs[USER_AGE_KEY])
    }

    val userName: Flow<String?> = userData.map { it.name }
    val userAge: Flow<Int?> = userData.map { it.age }
    val hasUserData: Flow<Boolean> = userData.map { it.name != null && it.age != null }

    suspend fun saveUserData(name: String, age: Int) {
        context.userDataStore.edit { prefs ->
            prefs[USER_NAME_KEY] = name
            prefs[USER_AGE_KEY] = age
        }
        context.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(SYNC_NAME, name)
            .putInt(SYNC_AGE, age)
            .apply()
    }

    suspend fun clearUserData() {
        context.userDataStore.edit { prefs ->
            prefs.remove(USER_NAME_KEY)
            prefs.remove(USER_AGE_KEY)
        }
        context.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    fun readUserDataSync(): Pair<String?, Int?> {
        val prefs = context.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE)
        val name = prefs.getString(SYNC_NAME, null)
        val age = if (prefs.contains(SYNC_AGE)) prefs.getInt(SYNC_AGE, 0) else null
        return name to age
    }
}