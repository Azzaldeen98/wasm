package com.example.wasmapplication.core.features.wasmSpeech.data.repository

import com.example.wasmapplication.core.features.wasmSpeech.data.remote.WasmApiRemote
import com.example.wasmapplication.core.features.wasmSpeech.domain.repository.WasmTextToSpeechRepository
import com.example.wasmapplication.core.interfaces.ICallbackTask
import com.example.wasmapplication.core.safeExecuteCallbackTask
import kotlinx.coroutines.flow.Flow

import javax.inject.Inject

class WasmTextToSpeechRepositoryImpl @Inject constructor(
    private val api: WasmApiRemote
)  : WasmTextToSpeechRepository {

    override suspend fun queryText(text: String): ByteArray? {
        return api.queryTextToSpeech(text)

    }
    override suspend fun queryTextWithSafe(text: String): ByteArray? {
        return  safeExecuteCallbackTask<ByteArray?>(object : ICallbackTask {
            override suspend fun executed() {
                api.queryTextToSpeech(text)
            }
        })
    }
}