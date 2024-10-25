package com.example.wasmapplication.core.components

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.getString
import androidx.navigation.NavHostController
import com.example.wasmapplication.R
import com.example.wasmapplication.Screens


@Composable
fun BottomNavigationBar(context: Context,navController: NavHostController) {
    var selectedIndex by remember { mutableIntStateOf(0) }

    NavigationBar {

        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text(context.getString(R.string.home)) },
            selected =  selectedIndex== 0,
            onClick = {
                selectedIndex=0
                navController.navigate(Screens.HomeScreen.name)
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Robot Speech") },
            label = { Text(context.getString(R.string.robot_speech)) },
            selected =  selectedIndex==1,
            onClick = {
                selectedIndex=1
                navController.navigate(Screens.RobotSpeechScreen.name) }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text(context.getString(R.string.settings)) },
            selected = selectedIndex==2,
            onClick = {
                selectedIndex=2
                navController.navigate(Screens.SettingsScreen.name)
            }
        )

    }
}