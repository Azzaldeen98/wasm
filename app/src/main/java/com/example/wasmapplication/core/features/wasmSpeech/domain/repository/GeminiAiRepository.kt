package com.example.wasmapplication.core.features.wasmSpeech.domain.repository

import com.example.wasmapplication.core.interfaces.IListenerStream
import com.google.ai.client.generativeai.type.GenerateContentResponse
import kotlinx.coroutines.flow.Flow


interface GeminiAiRepository {
     suspend fun sendMessage(text:String): String
     suspend fun sendMessageWithSafe(text:String): String
     suspend fun sendMessageStream(text: String): Flow<String>?
     suspend fun sendListenerMessageStream(text: String, callBack: IListenerStream<String>): Flow<String>?
     suspend fun sendMessageFlowStream(text: String): Flow<GenerateContentResponse>?
     suspend fun sendMessageStreamWithSafe(text: String): Flow<String>?
}

