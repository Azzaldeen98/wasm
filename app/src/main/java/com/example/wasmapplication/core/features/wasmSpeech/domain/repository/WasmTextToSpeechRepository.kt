package com.example.wasmapplication.core.features.wasmSpeech.domain.repository

interface WasmTextToSpeechRepository {
    suspend fun queryText(text:String): ByteArray?
    suspend fun queryTextWithSafe(text:String): ByteArray?
}