package com.example.wasmapplication.core.features.wasmSpeech.domain.use_case

import com.example.wasmapplication.core.Resource
import com.example.wasmapplication.core.constant.Constants
import com.example.wasmapplication.core.error.FailureMsg
import com.example.wasmapplication.core.error.ServerException
import com.example.wasmapplication.core.features.wasmSpeech.domain.repository.GeminiAiRepository
import com.example.wasmapplication.core.features.wasmSpeech.domain.repository.WasmTextToSpeechRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion

class GeminiTextWasmQueryStreamUseCase  @Inject constructor(
    private val repositoryGemini: GeminiAiRepository,
    private val repositoryWasmTextSpeech: WasmTextToSpeechRepository
) {
    fun invoke2(inputText: String): Flow<Resource<Any>> = flow {


        emit(Resource.Loading())
        val flow = repositoryGemini.sendMessageStream(inputText)
            ?.flowOn(Dispatchers.IO)
            ?.cancellable()//Allow Cancel Flow when cancel  Coroutine
            ?.catch { e ->
                emit(Resource.FinalError(e.localizedMessage ?: e.message ?: ""))
                return@catch
            }
            ?.onCompletion { cause ->
                if (cause == null) {
                    emit(Resource.Complete())
                } else {
                    emit(Resource.Error(cause.message!!))
                }
                return@onCompletion
            }

        flow?.collect { response ->
            if (response != null && response.isNotBlank() && response != Constants.END_SYMBOL) {
                try {
                    emit(Resource.Loading(response))
                    val bytes: ByteArray? = repositoryWasmTextSpeech.queryText(response)
                    if (bytes != null && bytes?.isNotEmpty() == true) {
                        emit(Resource.Success(bytes))
                    } else {
                        emit(Resource.Error(FailureMsg.WASM_QUERY_NULL_EN))
                    }
                } catch (e: Exception) {
                    if (e is ServerException) {
                        emit(Resource.FinalError("ServerException:${e.message}-${e.code}"))
                    } else {
                        emit(Resource.FinalError("Exception:${e.message}"))
                    }
                    return@collect
                }
            }
        }
    }
    suspend operator fun invoke(inputText: String): Flow<Resource<Any>>? {

        return repositoryGemini?.sendMessageStream(inputText)
            ?.flowOn(Dispatchers.IO)
            ?.cancellable()
            ?.flatMapConcat { response ->
                flow {
                    if (response != null && response.isNotBlank() && response != Constants.END_SYMBOL) {
                        try {
                            emit(Resource.Loading(response))
                            val bytes: ByteArray? = repositoryWasmTextSpeech.queryText(response)
                            if (bytes != null && bytes?.isNotEmpty() == true) {
                                emit(Resource.Success(bytes))
                            } else {
                                emit(Resource.Error(FailureMsg.WASM_QUERY_NULL_EN))
                            }
                        } catch (e: Exception) {
                            if (e is ServerException) {
                                emit(Resource.FinalError("ServerException:${e.message}-${e.code}"))
                            } else {
                                emit(Resource.FinalError("Exception:${e.message}"))
                            }
                            return@flow
                        }
                    }
                }
            }
    }
}
