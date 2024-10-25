package com.example.wasmapplication.features.wasmSpeech.presentation

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wasmapplication.R
import com.example.wasmapplication.features.settings.presentation.SettingViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale


//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    @ApplicationContext  context: Context,
    settingsViewModel: SettingViewModel
) {
    setLanguage(language = settingsViewModel.language.value)
    Column(
        modifier = Modifier.padding().fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {


                IconButton(onClick ={ settingsViewModel.toggleTheme() },
                    modifier = Modifier.background(MaterialTheme.colorScheme.secondary,shape = RoundedCornerShape(16.dp))) {
                    Icon(if (!settingsViewModel.isDarkTheme.value) Icons.Default.ModeNight
                    else Icons.Default.LightMode,
                        contentDescription = "switch theme",
                        tint =  Color.Black,
                    )
                }
                Text(if(settingsViewModel.isDarkTheme.value)  stringResource(id = R.string.night_mode) else stringResource(id = R.string.light_mode))
            Spacer(modifier = Modifier.height(50.dp))
            IconButton(onClick ={ settingsViewModel.saveLanguage(if(settingsViewModel.language.value=="ar") "en" else "ar") },
                modifier = Modifier.background(MaterialTheme.colorScheme.secondary,shape = RoundedCornerShape(16.dp))) {
                Icon(Icons.Default.Language,
                    contentDescription = "Switch Language",
                    tint =  Color.Black,
                )
            }



        }
    }



}


@Composable
fun setLanguage(language: String?){
//    val locale = Locale(language ?: "ar")
//    val configuration = LocalConfiguration.current
//    val updatedConfig = configuration.apply {
//        setLocale(locale)
//    }
//    val context = LocalContext.current
//    val resources = context.createConfigurationContext(updatedConfig).resources
//    (context as? Activity)?.recreate()

    val locale = Locale(language?:"ar")
    val configuration = LocalConfiguration.current
    configuration.setLocale(locale)
    val resources = LocalContext.current.resources
    resources.updateConfiguration(configuration, resources.displayMetrics)
}

fun setAppLocale(languageCode: String, context: Context) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)

    val resources = context.resources
    val config = resources.configuration
    config.setLocale(locale)

    resources.updateConfiguration(config, resources.displayMetrics)

    // إعادة تشغيل النشاط لتطبيق الترجمة الجديدة
    if (context is Activity) {
        context.recreate()
    }
}



