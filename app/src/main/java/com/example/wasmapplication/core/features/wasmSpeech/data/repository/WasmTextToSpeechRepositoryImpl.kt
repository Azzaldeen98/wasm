package com.example.wasmapplication.core.features.wasmSpeech.data.repository

import com.example.wasmapplication.core.features.wasmSpeech.data.remote.WasmApiRemote
import com.example.wasmapplication.core.features.wasmSpeech.domain.repository.WasmTextToSpeechRepository

import javax.inject.Inject

class WasmTextToSpeechRepositoryImpl @Inject constructor(
    private val api: WasmApiRemote
)  : WasmTextToSpeechRepository {

    override suspend fun queryText(text: String): ByteArray? {
        return  api.queryTextToSpeech(text)
    }
}