package com.example.wasmapplication.core.features.wasmSpeech.data.repository

import com.example.wasmapplication.core.features.wasmSpeech.data.remote.GeminiApiClient
import com.example.wasmapplication.core.features.wasmSpeech.domain.repository.GeminiAiRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GeminiAiRepositoryImpl @Inject constructor(
    private val api: GeminiApiClient
) : GeminiAiRepository {
    override suspend fun sendMessage(text: String): String {
      return  api.sendMessage(text)?:""
    }
    override suspend fun sendMessageStream(text: String): Flow<String>  {
      return  api.sendMessageStream(text)
    }
}