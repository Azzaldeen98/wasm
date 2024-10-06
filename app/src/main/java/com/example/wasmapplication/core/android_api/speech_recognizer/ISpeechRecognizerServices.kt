package com.example.wasmapplication.core.android_api.speech_recognizer

import java.util.*

interface ISpeechRecognizerCallBack {

    fun onSpeechRecognizerResults(results: ArrayList<String>?){}
    fun onSpeechRecognizerResult(result:String?){}
}