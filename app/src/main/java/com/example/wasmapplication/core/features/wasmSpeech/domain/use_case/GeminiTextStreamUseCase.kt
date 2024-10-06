package com.example.wasmapplication.core.features.wasmSpeech.domain.use_case


import android.annotation.SuppressLint
import com.example.wasmapplication.core.Resource
import com.example.wasmapplication.core.constant.FailureMsg
import com.example.wasmapplication.core.features.wasmSpeech.domain.repository.GeminiAiRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException

class GeminiTextStreamUseCase  @Inject constructor(
    private val repositoryGemini: GeminiAiRepository
) {

    @SuppressLint("SuspiciousIndentation")
    operator fun invoke(inputText:String): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading<String>())

            val flow=repositoryGemini.sendMessageStream(inputText)
            if(flow!=null){
                flow.flowOn(Dispatchers.IO)
                .collect { response ->
                    emit(Resource.Success<String>(response))
                }

            }else{
                emit(Resource.Error<String>(FailureMsg.GEMINI_RETURN_NULL_EN))
            }

        } catch (e: HttpException) {
            emit(Resource.Error<String>(e.localizedMessage ?: e.message?:"Http Error"))
        } catch (e: IOException) {
            emit(Resource.Error<String>(e.message?:"IO Error"))
        }
    }
//    operator fun invoke(inputText:String): Flow<Resource<String>> = flow {
//        try {
//            emit(Resource.Loading<String>())
//            geminiSendMessageUseCase(inputText).collect{ result ->
//                when (result) {
//                    is Resource.Success -> {
//                        queryTextToSpeechUseCase(result.data?:"")
//                            .collect{ result ->
//                            when (result) {
//                                is Resource.Success -> {
//                                    if(result?.data?.isNullOrEmpty()==false){
//                                        var file=Helper.createTempFile("wasm_temp_audio",result?.data!!,this)
//                                    }
//                                    emit(Resource.Success<String>(!!))
//                                }
//                                is Resource.Error -> {
//                                    emit(Resource.Error<String>(  result.message ?: "An unexpected error occured"))
//
//                                }
//                                is Resource.Loading -> {
//
//                                }
//                            }
//                        }
//                    }
//                    is Resource.Error -> {
//                        result.message ?: "An unexpected error occured"
//                    }
//                    is Resource.Loading -> {
//
//                    }
//                }
//            }
//
//        } catch (e: HttpException) {
//            emit(Resource.Error<String>(e.localizedMessage ?: "An unexpected error occured"))
//        } catch (e: IOException) {
//            emit(Resource.Error<String>("Couldn't reach server. Check your internet connection."))
//        }
//    }
}