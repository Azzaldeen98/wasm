package com.example.wasmapplication

sealed class Routes(val route: String) {
    object RobotSpeechScreen: Routes("robot_speech_screen")
}
