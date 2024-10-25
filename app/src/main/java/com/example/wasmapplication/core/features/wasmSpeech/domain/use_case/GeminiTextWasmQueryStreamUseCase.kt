package com.example.wasmapplication.core.features.wasmSpeech.domain.use_case

import com.example.wasm.core.android_api.media.ExoPlayerMedia
import com.example.wasmapplication.core.Resource
import com.example.wasmapplication.core.constant.Constants
import com.example.wasmapplication.core.error.AiSafetyException
import com.example.wasmapplication.core.error.ConnectErrorException
import com.example.wasmapplication.core.error.CustomException
import com.example.wasmapplication.core.error.FailureMsg
import com.example.wasmapplication.core.error.NullException
import com.example.wasmapplication.core.error.ServerException
import com.example.wasmapplication.core.features.wasmSpeech.domain.repository.GeminiAiRepository
import com.example.wasmapplication.core.features.wasmSpeech.domain.repository.WasmTextToSpeechRepository
import com.example.wasmapplication.core.interfaces.IListenerStream
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
import java.net.ConnectException

class GeminiTextWasmQueryStreamUseCase  @Inject constructor(
    private val repositoryGemini: GeminiAiRepository,
    private val repositoryWasmTextSpeech: WasmTextToSpeechRepository
) {
       fun invoke6(inputText:String,exoPlayerMedia:ExoPlayerMedia): Flow<Resource<Any>> = flow {

        emit(Resource.Loading())
       var flowRes:Flow<String>?= null
        flowRes=repositoryGemini.sendListenerMessageStream(inputText,object :IListenerStream<String>{
            override suspend fun onStreamReader(response: String?) {
                if(response?.isNotEmpty()==true && response==Constants.END_SYMBOL){
                    emit(Resource.Complete())
                }else{
                    try{
                        emit(Resource.Loading(response.toString()))
                        val bytes: ByteArray? = repositoryWasmTextSpeech.queryText(response.toString())
                        if (bytes != null) {
                            if (bytes is ByteArray && bytes.isNotEmpty()) {
                                withContext(Dispatchers.Main){
                                    exoPlayerMedia?.playerMediaStreamAndWait(bytes)
                                }
                            }
//                                emit(Resource.Success(bytes))
                        } else {
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
            override suspend fun onStreamComplete() {
                emit(Resource.Complete())
            }



            override  suspend  fun onStreamError2(message: Throwable) {
                emit(Resource.Complete())
            }

        })

    }
       fun invoke4(inputText:String,exoPlayerMedia:ExoPlayerMedia): Flow<Resource<Any>> = flow {

        emit(Resource.Loading())
        val flow= repositoryGemini.sendMessageFlowStream(inputText)
        var isCallBackReader = false
        val responseBuilder = StringBuilder()
        try {

            flow?.flowOn(Dispatchers.IO)
                    ?.cancellable()
                    ?.onCompletion { cause ->
                        if (cause == null) {
//                            if (!isCallBackReader && responseBuilder.toString().trim().isNotEmpty()) {
//                                emit(responseBuilder.toString().trim())
//                                responseBuilder.clear()
//                            }
                            emit(Resource.Complete())
                            println("responseFlow: Completed successfully")
                        } else {
                            println("responseFlow: Completed with error: ${cause.message}")
                            throw IllegalArgumentException(cause)
                        }
                        return@onCompletion
                    }?.collect { response ->
                        val content = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.asTextOrNull()
                        content?.trim()?.let {
                            if (it.isNotEmpty()) {
                                isCallBackReader = false
                                var cleanedText = it.replace("*", "").replace(Regex("\\s+"), " ")
                                responseBuilder.append(cleanedText).append(" ")
                                if (isEndOfSentence(responseBuilder.toString()) || responseBuilder.length >= 20) {
                                    val lines = spiltSentence(responseBuilder.toString())?.filter { it.isNotEmpty() }
                                    lines?.let {
                                        var line = ""
                                        for (sentence in it) {
                                            line += "$sentence "
                                            if (line.length >= 10) {
                                                isCallBackReader = true
                                                try{
                                                    var response=line;
                                                    emit(Resource.Loading(response))
                                                    val bytes: ByteArray? = repositoryWasmTextSpeech.queryText(response)
                                                    if (bytes != null) {
                                                        if (bytes is ByteArray && bytes.isNotEmpty()) {
                                                            withContext(Dispatchers.Main){
                                                                exoPlayerMedia?.playerMediaStreamAndWait(bytes)
                                                            }
                                                        }
//                                emit(Resource.Success(bytes))
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
//                            delay(500)
                                                    return@collect
                                                }
//                                                emit(line)
                                                responseBuilder.clear()
                                                line = ""
                                            } else {
                                                isCallBackReader = false
                                            }
                                        }
                                    }
                                }
                            }
                        }
//                        delay(200)
                    }

        } catch (e: HttpException) {
            throw  ServerException(e.code(),e.message())
        } catch (e: KotlinNullPointerException) {
            throw  NullException(e.message)
        }catch (e: ConnectException) {
            throw  ConnectErrorException(e.message)
        } catch (e: Exception) {
            if (e.message?.contains("SAFETY") == true) {
                throw AiSafetyException(e.message)
            }
            throw CustomException(e.message)
        }
    }
        private fun isEndOfSentence(text: String): Boolean {
            return text.endsWith(".") || text.endsWith("!") || text.endsWith("?") || text.endsWith("،")
                    || text.endsWith(",")   || text.endsWith("؟")
        }
        private fun spiltSentence(text: String): List<String> {
            return text.split(Regex("[،؟,.]"))
        }
      fun invoke3(inputText:String,exoPlayerMedia:ExoPlayerMedia): Flow<Resource<Any>> = flow {

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

        }
            ?.flowOn(Dispatchers.IO)
            ?.collect { response ->
                if(response!=null && response.isNotBlank()){
                    if(response==Constants.END_SYMBOL){
                        emit(Resource.Complete())
                    }else{
                        try{
                            emit(Resource.Loading(response))
                            val bytes: ByteArray? = repositoryWasmTextSpeech.queryText(response)
                            if (bytes != null) {
                                if (bytes is ByteArray && bytes.isNotEmpty()) {
                                    withContext(Dispatchers.Main){
                                        exoPlayerMedia?.playerMediaStreamAndWait(bytes)
                                        return@withContext
                                    }
                                }
//                                emit(Resource.Success(bytes))
                            }
                            else {
                                emit(Resource.Error(FailureMsg.WASM_QUERY_NULL_EN))
                                return@collect
                            }
                        } catch (e:Exception){
                            if(e is ServerException){
                                emit(Resource.FinalError("ServerException:${e.message}-${e.code}"))
                            }else{
                                emit(Resource.FinalError("Exception:${e.message}"))
                            }
//                            delay(500)
                            return@collect
                        }
                    }
                }
            }
    }
    operator  fun invoke(inputText:String): Flow<Resource<Any>> = flow {


        emit(Resource.Loading())
        val flow= repositoryGemini.sendMessageStream(inputText)
        flow?.catch {e->
            emit(Resource.FinalError(e.localizedMessage ?: e.message?:""))
            return@catch
        }?.cancellable()//Allow Cancel Flow when cancel  Coroutine
            ?.onCompletion { cause ->
                if (cause != null) {
                    emit(Resource.FinalError(cause.message!!))
                }
                return@onCompletion
            }?.flowOn(Dispatchers.IO)
            ?.collect { response ->
                if(response!=null && response.isNotBlank()){
                    if(response==Constants.END_SYMBOL){
                        emit(Resource.Complete())
                        return@collect
                    }else{
                        try{
                            emit(Resource.Loading(response))
                            val bytes: ByteArray? = repositoryWasmTextSpeech.queryText(response)
                            if (bytes != null && bytes?.isNotEmpty()==true) {
                                emit(Resource.Success(bytes))
                            } else {
//                                delay(500)
                                emit(Resource.Error(FailureMsg.WASM_QUERY_NULL_EN))
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