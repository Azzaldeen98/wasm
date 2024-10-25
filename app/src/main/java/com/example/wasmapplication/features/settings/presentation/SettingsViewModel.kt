package com.example.wasmapplication.features.settings.presentation


import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wasmapplication.core.local.LanguageControls
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val languageControl: LanguageControls
): ViewModel() {
    private val _isDarkTheme = mutableStateOf(false)
    private val _language = mutableStateOf("ar")


    val isDarkTheme: State<Boolean> = _isDarkTheme
    val  language: State<String> = _language

        init {
            toggleLanguage()
        }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }
    fun toggleLanguage() {
        viewModelScope.launch {
            _language.value = languageControl?.getLanguage().toString()
        }
    }

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }

    fun saveLanguage(language: String) {

        viewModelScope.launch {
            languageControl?.saveLanguage(language)
            _language.value = language
        }
    }


}