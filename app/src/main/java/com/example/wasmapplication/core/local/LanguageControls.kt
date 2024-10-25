package com.example.wasmapplication.core.local

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.preferencesKey
import com.example.wasmapplication.core.data.local.DataStorePreferenceRepository
import java.util.Locale

class LanguageControls private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: LanguageControls? = null
        private var dataStorePreferenceRepository: DataStorePreferenceRepository? = null
        private val defaultLanguage = "en"

        fun getInstance(context: Context): LanguageControls {
            dataStorePreferenceRepository= DataStorePreferenceRepository.getInstance(context)
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LanguageControls(context).also { INSTANCE = it }
            }
        }

        private val PREF_LANGUAGE = preferencesKey<Int>("language")




    }


    suspend fun saveLanguage(language: String) {
        dataStorePreferenceRepository?.setValue(PREF_LANGUAGE.toString(),language)
    }

    suspend fun  getLanguage():String? {
        return dataStorePreferenceRepository?.getValue(PREF_LANGUAGE.toString())
    }

}