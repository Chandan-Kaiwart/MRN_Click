package com.apc.smartinstallation.dataStore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.apc.smartinstallation.dataClasses.User
import com.apc.smartinstallation.dataClasses.login.LoginRes
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

// Extension to create a DataStore
val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    private val gson = Gson()

    companion object {
        private val USER_DATA_KEY = stringPreferencesKey("user_data")
    }





    // Save User Data as JSON
    suspend fun saveUser(user: LoginRes) {
        val userJson = gson.toJson(user) // Convert to JSON
        context.dataStore.edit { preferences ->
            preferences[USER_DATA_KEY] = userJson
        }
    }

    // Get User Data
    suspend fun getUser(): LoginRes? {
        return context.dataStore.data.map { preferences ->
            preferences[USER_DATA_KEY]?.let { userJson ->
                gson.fromJson(userJson, LoginRes::class.java) // Convert from JSON
            }
        }.firstOrNull()
    }

    // Retrieve user
    val user: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_DATA_KEY]
    }

    // Clear Token & User Data (Logout)
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_DATA_KEY)
        }
    }
}
