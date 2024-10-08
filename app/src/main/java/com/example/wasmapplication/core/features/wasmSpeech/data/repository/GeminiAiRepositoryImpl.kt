package com.example.wasmapplication.core.features.wasmSpeech.data.repository

import com.example.wasmapplication.core.features.wasmSpeech.data.remote.GeminiApiClient
import com.example.wasmapplication.core.features.wasmSpeech.domain.repository.GeminiAiRepository
import com.example.wasmapplication.core.interfaces.ICallbackTask
import com.example.wasmapplication.core.safeExecuteCallbackTask
import com.google.ai.client.generativeai.type.GenerateContentResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GeminiAiRepositoryImpl @Inject constructor(
    private val api: GeminiApiClient
) : GeminiAiRepository {
     suspend fun sendMessage2(text: String): String {
      return  api.sendMessage(text)?:""
    }
    override suspend fun sendMessage(text: String): String {
        return   api.sendMessage(text)?:""
    }
    override suspend fun sendMessageWithSafe(text: String): String {
        return  safeExecuteCallbackTask<String>(object :ICallbackTask{
            override suspend fun executed() {
                api.sendMessage(text)
            }
        })?:""

    }
    override suspend fun sendMessageStream(text: String): Flow<String>?  {
               return api.sendMessageStream(text);
    }
    override suspend fun sendMessageFlowStream(text: String): Flow<GenerateContentResponse>?   {
               return api.sendMessageFlowStream(text);
    }
    override suspend fun sendMessageStreamWithSafe(text: String): Flow<String>?  {
        return  safeExecuteCallbackTask<Flow<String>>(object :ICallbackTask{
            override suspend fun executed() {
                api.sendMessageStream(text)
            }
        })
    }
}