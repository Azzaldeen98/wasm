package com.example.wasmapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wasmapplication.core.components.BottomNavigationBar
import com.example.wasmapplication.features.home.presentation.HomeScreen
import com.example.wasmapplication.features.settings.presentation.SettingViewModel
import com.example.wasmapplication.features.wasmSpeech.presentation.RobotSpeechScreen
import com.example.wasmapplication.features.wasmSpeech.presentation.SettingsScreen
import com.example.wasmapplication.ui.theme.WasmApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

//    var result by rememberSaveable { mutableStateOf("placeholderResult") }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MyAppBar(title:String) {
        TopAppBar(
//            modifier = Modifier.background(MaterialTheme.colorScheme.primary),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center) {
                        Text(title)
                    }
                },
            actions = {
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Search,  contentDescription = "Search")
                }
                IconButton(onClick = {  }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
            }
        )
    }
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {

            val settingsViewModel: SettingViewModel = viewModel()
            val isDarkTheme by settingsViewModel.isDarkTheme
            val languageCode:String  by  settingsViewModel.language

            WasmApplicationTheme(isDarkTheme) {

                 Surface(color = MaterialTheme.colorScheme.background) {
                     val navController = rememberNavController()
                     Scaffold(
                         topBar = { MyAppBar(getString(R.string.app_name))},
                         bottomBar = { BottomNavigationBar(this,navController) } // إضافة قائمة التنقل السفلية هنا
                     ) { innerPadding ->
                     NavHost(
                         navController = navController,
                         startDestination = Screens.HomeScreen.name,
                         Modifier.padding(innerPadding)
                     ) {

                         composable(route = Screens.HomeScreen.name) {
                             RequestPermissionsOnStart()
                             HomeScreen(navController, this@MainActivity)
                         }
                         composable(route = Screens.RobotSpeechScreen.name) {
                             RobotSpeechScreen(navController, this@MainActivity)
                         }
                         composable(route = Screens.SettingsScreen.name) {
                             SettingsScreen(navController, this@MainActivity,settingsViewModel)
                         }
                     }
                 }
                }
            }
            ChangeLanguage(this,languageCode = languageCode)
        }
    }
    override fun onStart() {
        super.onStart()

    }

    fun getCurrentLocales(context: Context): android.os.LocaleList? {

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
           return context.getSystemService(LocaleManager::class.java).applicationLocales
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val configuration = context.resources.configuration
            return configuration.locales
        }else
            return  null
    }
    @Composable
    fun ChangeLanguage(context: Context,languageCode: String) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            Toast.makeText(this, "Language set to TIRAMISU :  $languageCode", Toast.LENGTH_SHORT).show()
            context.getSystemService(LocaleManager::class.java)
                .applicationLocales = android.os.LocaleList(Locale.forLanguageTag(languageCode))
        }else{
            Toast.makeText(this, "Language set to :  $languageCode", Toast.LENGTH_SHORT).show()
            val configuration = resources.configuration
            configuration.setLocale(Locale.forLanguageTag(languageCode))
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)

//        val context = LocalContext.current
//        val configuration = context.resources.configuration



//        LaunchedEffect(languageCode) {
//            val newLocale = Locale(languageCode)
//            Locale.setDefault(newLocale)
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                configuration.setLocales(android.os.LocaleList(newLocale))
//            } else {
//                @Suppress("DEPRECATION")
//                configuration.locale = newLocale
//            }
//
//            // لضمان التحديث على مستوى التطبيق
//            context.createConfigurationContext(configuration)
//        }
    }

    @Composable
    fun changeAppLanguage(languageCode: String?){

        val context = LocalContext.current
        val configuration = context.resources.configuration

        Toast.makeText(this@MainActivity,languageCode,Toast.LENGTH_SHORT).show()
//        val configuration = LocalConfiguration.current

//        LaunchedEffect(languageCode) {
            val newLocale = Locale(languageCode)
            Locale.setDefault(newLocale)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocales(android.os.LocaleList(newLocale))
            } else {
                @Suppress("DEPRECATION")
                configuration.locale = newLocale
            }
        configuration.setLocale(newLocale)
        context.createConfigurationContext(configuration) // لتطبيق التحديث على السياق
//
//            @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
//        }
//        configuration.setLocale(locale)
//        val resources = LocalContext.current.resources
//        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
    @Composable
    fun RequestPermissions() {
        val context = LocalContext.current
        var permissionsGranted by remember { mutableStateOf(false) }

        // Check if permissions are already granted
        val allPermissionsGranted = remember {
            ContextCompat.checkSelfPermission(context,  android.Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context,  android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        }

        // Launcher to request permissions
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissionsGranted = permissions[ android.Manifest.permission.RECORD_AUDIO] == true &&
                    permissions[ android.Manifest.permission.CAMERA] == true
        }

        // Request permissions if not already granted
        if (!allPermissionsGranted) {
                launcher.launch(arrayOf( android.Manifest.permission.RECORD_AUDIO,  android.Manifest.permission.CAMERA))
//            Button(onClick = {
//
//            }) {
//                Text(text = "Request Camera and Microphone Permissions")
//            }
        } else {
            Text(text = "Permissions already granted!")
        }
    }
    @Composable
    fun RequestPermissionsOnStart() {
        val context = LocalContext.current
        var showPermissionDialog by remember { mutableStateOf(false) }
        var permissionsGranted by remember { mutableStateOf(false) }

        // Check if permissions are already granted
        val allPermissionsGranted = remember {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        }

        // Launcher to request permissions
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissionsGranted = permissions[android.Manifest.permission.RECORD_AUDIO] == true &&
                    permissions[android.Manifest.permission.CAMERA] == true
        }

        // Trigger dialog immediately if permissions are not granted
        LaunchedEffect(Unit) {
            if (!allPermissionsGranted) {
                showPermissionDialog = true
            }
        }

        // Permission request dialog
        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = { /* Prevent dismissing */ },
                title = { Text(text = "طلب الأذونات") },
                text = { Text("نحتاج إلى إذن للوصول إلى الكاميرا والميكروفون. هل ترغب في منح الأذونات؟") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPermissionDialog = false
                            // Launch permissions request
                            permissionLauncher.launch(
                                arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.CAMERA)
                            )
                        }
                    ) {
                        Text("نعم")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showPermissionDialog = false
                        // Optionally close the app if permissions are critical
                    }) {
                        Text("لا")
                    }
                }
            )
        }

        // Here you can show the main content after permissions are handled
        if (permissionsGranted) {

            Text(text = "الأذونات ممنوحة! يمكنك الآن استخدام التطبيق.")
        }
    }
    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        WasmApplicationTheme {
            Greeting("Android")
        }
    }
}