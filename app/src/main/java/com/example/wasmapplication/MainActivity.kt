package com.example.wasmapplication

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wasmapplication.broadcasts.NetworkChangeReceiver
import com.example.wasmapplication.features.wasmSpeech.presentation.RobotSpeechScreen
import com.example.wasmapplication.services.RecordVoiceService
import com.example.wasmapplication.ui.theme.WasmApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        enableEdgeToEdge()
        setContent {
            WasmApplicationTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Routes.RobotSpeechScreen.route
                    ) {
                        composable(
                            route = Routes.RobotSpeechScreen.route
                        ) {
                            RequestPermissionsOnStart()
                            RobotSpeechScreen(navController,this@MainActivity)
                        }
                    }
//                Column(
//                    modifier = Modifier.fillMaxSize(),
//                    verticalArrangement = Arrangement.Center,
//                    ) {
//                    RobotSpeechScreen()
//                }
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

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