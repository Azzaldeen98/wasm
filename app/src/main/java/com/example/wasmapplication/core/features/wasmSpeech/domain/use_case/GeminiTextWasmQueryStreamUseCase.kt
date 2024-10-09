package com.example.wasmapplication.core.features.wasmSpeech.domain.use_case

import com.example.wasmapplication.core.Resource
import com.example.wasmapplication.core.constant.Constants
import com.example.wasmapplication.core.error.FailureMsg
import com.example.wasmapplication.core.error.ServerException
import com.example.wasmapplication.core.features.wasmSpeech.domain.repository.GeminiAiRepository
import com.example.wasmapplication.core.features.wasmSpeech.domain.repository.WasmTextToSpeechRepository
import com.google.ai.client.generativeai.type.asTextOrNull
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class GeminiTextWasmQueryStreamUseCase  @Inject constructor(
    private val repositoryGemini: GeminiAiRepository,
    private val repositoryWasmTextSpeech: WasmTextToSpeechRepository
) {
    operator fun invoke(inputText:String): Flow<Resource<Any>> = flow {

        emit(Resource.Loading())
        val flow= repositoryGemini.sendMessageStream(inputText)
        flow?.catch {e->
            emit(Resource.FinalError(e.localizedMessage ?: e.message?:""))
            return@catch
           }?.cancellable()//Allow Cancel Flow when cancel  Coroutine
            ?.onCompletion{ cause ->
            if (cause == null) {
                emit(Resource.Complete())
            } else {
                emit(Resource.Error(cause.message!!))
            }

        }?.flowOn(Dispatchers.IO)
            ?.collect { response ->
                if(response!=null && response.isNotBlank()){
                    if(response==Constants.END_SYMBOL){
                        emit(Resource.Complete())
                    }else{
                        try{
                            emit(Resource.Loading(response))
                            val bytes: ByteArray? = repositoryWasmTextSpeech.queryText(response)
                            if (bytes != null) {
                                emit(Resource.Success(bytes))
                            } else {
                                emit(Resource.Error(FailureMsg.WASM_QUERY_NULL_EN))
                                return@collect
                            }
                        } catch (e:Exception){
                            if(e is ServerException){
                                emit(Resource.FinalError("ServerException:${e.message}-${e.code}"))
                            }else{
                                emit(Resource.FinalError("Exception:${e.message}"))
                            }
                            return@collect
                        }
                    }
                }
            }
    }
     fun invoke1(inputText:String): Flow<Resource<Any>> = flow {

        emit(Resource.Loading())
        val flow= repositoryGemini.sendMessageStream(inputText)
        flow?.catch {e->
            emit(Resource.FinalError(e.localizedMessage ?: e.message?:""))
        }?.cancellable()//Allow Cancel Flow when cancel  Coroutine
            ?.onCompletion{ cause ->
                if (cause == null) {
                    emit(Resource.Complete())
                } else {
                    emit(Resource.Error(cause.message!!))
                }

            }?.flowOn(Dispatchers.IO)
            ?.collect { response ->
                if(response!=null && response.isNotBlank()){
                    if(response==Constants.END_SYMBOL){
                        emit(Resource.Complete())
                    }else{
                        try{
                            emit(Resource.Loading(response))
                            val bytes: ByteArray? = repositoryWasmTextSpeech.queryText(response)
                            if (bytes != null) {
                                emit(Resource.Success(bytes))
                            } else {
                                delay(500)
                                emit(Resource.Error(FailureMsg.WASM_QUERY_NULL_EN))
                            }
                        } catch (e:Exception){
                            if(e is ServerException){
                                emit(Resource.FinalError("ServerException:${e.message}-${e.code}"))
                            }else{
                                emit(Resource.FinalError("Exception:${e.message}"))
                            }
                        }
                    }
                }
            }
    }
}
class GeminiTextWasmQueryStreamUseCaseV1  @Inject constructor(
    private val repositoryGemini: GeminiAiRepository,
    private val repositoryWasmTextSpeech: WasmTextToSpeechRepository
) {
    operator fun invoke(inputText:String): Flow<Resource<Any>> = flow {
        try {
            emit(Resource.Loading())
            val flow=repositoryGemini.sendMessageStream(inputText)
            if(flow!=null){
                //  .flowOn(Dispatchers.IO)
                flow.onCompletion{ cause ->
                    if (cause == null) {
                        emit(Resource.Complete())
                    } else {
                        emit(Resource.Error(cause.message!!))
//                            throw IllegalArgumentException(cause)
                    }
                }
                    .collect { response ->
                        if(response!=null && response.isNotBlank()){

                                try{

                                    val bytes: ByteArray? = repositoryWasmTextSpeech.queryText(response)
                                    if (bytes != null) {
                                        emit(Resource.Success(bytes))
                                    } else {
                                        emit(Resource.Error(FailureMsg.WASM_QUERY_NULL_EN))
                                    }
                                }
                                catch (e:HttpException){
                                    emit(Resource.FinalError("HttpException2:${e.localizedMessage}" ?: "${e.message?:"Http Error"}-${e.code()}"))
                                } catch (e:IOException){
                                    emit(Resource.FinalError("IOException2:${e.localizedMessage}" ?: e.message?:"Http Error"))
                                }catch (e:Exception){
                                    emit(Resource.FinalError("Exception2:${e.localizedMessage}" ?: e.message?:"Http Error"))
                                }
                            }

                    }
            }else{
                emit(Resource.FinalError(FailureMsg.GEMINI_RETURN_NULL_EN))
            }
        } catch (e: HttpException) {
            emit(Resource.FinalError(e.localizedMessage ?: e.message?:"Http Error"))
        } catch (e: IOException) {
            emit(Resource.FinalError(e.message?:"IO Error"))
        }
    }
}

class GeminiTextWasmQueryStreamUseCaseV2  @Inject constructor(
    private val repositoryGemini: GeminiAiRepository,
    private val repositoryWasmTextSpeech: WasmTextToSpeechRepository
) {
    operator fun invoke(inputText:String): Flow<Resource<Any>> = flow {
        try {
            emit(Resource.Loading())
            val flow=repositoryGemini.sendMessageStream(inputText)
            if(flow!=null){
                //  .flowOn(Dispatchers.IO)
                flow.onCompletion{ cause ->
                    if (cause == null) {
                        emit(Resource.Complete())
                    } else {
                        emit(Resource.Error(cause.message!!))
//                            throw IllegalArgumentException(cause)
                    }
                }
                    .collect { response ->
                        if(response!=null && response.isNotBlank()){

                            CoroutineScope(Dispatchers.IO).launch{
                                try{

                                    val bytes: ByteArray? = repositoryWasmTextSpeech.queryText(response)
                                    if (bytes != null) {
                                        emit(Resource.Success(bytes))
                                    } else {
                                        emit(Resource.Error(FailureMsg.WASM_QUERY_NULL_EN))
                                    }
                                }
                                catch (e:HttpException){
                                    emit(Resource.FinalError("HttpException2:${e.localizedMessage}" ?: "${e.message?:"Http Error"}-${e.code()}"))
                                } catch (e:IOException){
                                    emit(Resource.FinalError("IOException2:${e.localizedMessage}" ?: e.message?:"Http Error"))
                                }catch (e:Exception){
                                    emit(Resource.FinalError("Exception2:${e.localizedMessage}" ?: e.message?:"Http Error"))
                                }
                            }
                        }
                    }
            }else{
                emit(Resource.FinalError(FailureMsg.GEMINI_RETURN_NULL_EN))
            }
        } catch (e: HttpException) {
            emit(Resource.FinalError(e.localizedMessage ?: e.message?:"Http Error"))
        } catch (e: IOException) {
            emit(Resource.FinalError(e.message?:"IO Error"))
        }
    }
}

fun isValidSentence(text: String): Boolean {
    return text.length > 20 && text.contains(".")
}

class GeminiTextWasmQueryFlowStreamUseCase  @Inject constructor(
    private val repositoryGemini: GeminiAiRepository,
    private val repositoryWasmTextSpeech: WasmTextToSpeechRepository
) {
    operator fun invoke(inputText:String): Flow<Resource<Any>> = flow {
        try {
            emit(Resource.Loading())
            val flow=repositoryGemini.sendMessageStream(inputText)
            if(flow!=null){
                var contentText="";
                //  .flowOn(Dispatchers.IO)
                flow.onCompletion{ cause ->
                    if (cause == null) {
                        emit(Resource.Complete())
                    } else {
                        emit(Resource.Error(cause.message!!))
                    }
                }.collect { response ->
                        try{
                            val bytes: ByteArray? = repositoryWasmTextSpeech.queryText(response)
                            if (bytes != null) {
                                emit(Resource.Success(bytes))
                            } else {
                                emit(Resource.Error(FailureMsg.WASM_QUERY_NULL_EN))
                            }
                        }
                        catch (e:HttpException){
                            emit(Resource.FinalError("HttpException2:${e.localizedMessage}" ?: "${e.message?:"Http Error"}-${e.code()}"))
                        } catch (e:IOException){
                            emit(Resource.FinalError("IOException2:${e.localizedMessage}" ?: e.message?:"Http Error"))
                        }catch (e:Exception){
                            emit(Resource.FinalError("Exception2:${e.localizedMessage}" ?: e.message?:"Http Error"))
                        }
                    }
            }else{
                emit(Resource.FinalError(FailureMsg.GEMINI_RETURN_NULL_EN))
            }
        } catch (e: HttpException) {
            emit(Resource.FinalError(e.localizedMessage ?: e.message?:"Http Error"))
        } catch (e: IOException) {
            emit(Resource.FinalError(e.message?:"IO Error"))
        }
    }

    private suspend  fun  sendToQuerySpeech(text:String):Flow<Resource<Any>> = flow{
        try{
            val bytes: ByteArray? = repositoryWasmTextSpeech.queryText(text)
            if (bytes != null) {
                emit(Resource.Success(bytes))
            } else {
                emit(Resource.Error(FailureMsg.WASM_QUERY_NULL_EN))
            }
        }
        catch (e:HttpException){
            emit(Resource.FinalError("HttpException2:${e.localizedMessage}" ?: "${e.message?:"Http Error"}-${e.code()}"))
        } catch (e:IOException){
            emit(Resource.FinalError("IOException2:${e.localizedMessage}" ?: e.message?:"Http Error"))
        }catch (e:Exception){
            emit(Resource.FinalError("Exception2:${e.localizedMessage}" ?: e.message?:"Http Error"))
        }

    }
}