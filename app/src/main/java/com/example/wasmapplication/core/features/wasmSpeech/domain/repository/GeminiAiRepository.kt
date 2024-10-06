package com.example.wasmapplication.core.features.wasmSpeech.domain.repository

import kotlinx.coroutines.flow.Flow


interface GeminiAiRepository {
     suspend fun sendMessage(text:String): String
     suspend fun sendMessageStream(text: String): Flow<String>
}

