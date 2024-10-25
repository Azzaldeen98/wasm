package com.example.wasmapplication

sealed class Screens(val name: String) {
    object HomeScreen: Screens("Home")
    object RobotSpeechScreen: Screens("RobotSpeech")
    object SettingsScreen: Screens("Settings")
}
