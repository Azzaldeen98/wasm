package com.example.wasmapplication.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.core.remove
import androidx.datastore.preferences.createDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map




class DataStorePreferenceRepository private constructor(context: Context) {

    private val dataStore: DataStore<Preferences> = context.createDataStore(name = "LocalDataStorage")

    companion object {
        @Volatile
        private var INSTANCE: DataStorePreferenceRepository? = null

        fun getInstance(context: Context): DataStorePreferenceRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataStorePreferenceRepository(context).also { INSTANCE = it }
            }
        }
    }

    suspend fun exists(key: String): Boolean {
        return dataStore.data.map { preferences ->
            preferences.contains(preferencesKey<String>(key))
        }.first()
    }

    suspend fun getValue(key: String): String? {
        return dataStore.data.map { preferences ->
            preferences[preferencesKey<String>(key)]
        }.first()
    }


    suspend fun setValue(key: String, value: String): Boolean {
        if (key.isNotEmpty() && value.isNotEmpty()) {
            dataStore.edit { preferences ->
                preferences[preferencesKey<String>(key)] = value
            }
            return true
        }
        return false
    }


    suspend fun setIntValue(key: String, value: Int): Boolean {
        if (key.isNotEmpty()) {
            dataStore.edit { preferences ->
                preferences[preferencesKey<Int>(key)] = value
            }
            return true
        }
        return false
    }


    suspend fun getIntValue(key: String): Int {
        return dataStore.data.map { preferences ->
            preferences[preferencesKey<Int>(key)] ?: 0
        }.first()
    }


    suspend fun getBooleanValue(key: String): Boolean {
        return dataStore.data.map { preferences ->
            preferences[preferencesKey<Boolean>(key)] ?: false
        }.first()
    }


    suspend fun setBooleanValue(key: String, value: Boolean): Boolean {
        if (key.isNotEmpty()) {
            dataStore.edit { preferences ->
                preferences[preferencesKey<Boolean>(key)] = value
            }
            return true
        }
        return false
    }


    suspend fun remove(key: String): Boolean {
        return if (exists(key)) {
            dataStore.edit { preferences ->
                preferences.remove(preferencesKey<String>(key))
            }
            true
        } else {
            false
        }
    }
}

