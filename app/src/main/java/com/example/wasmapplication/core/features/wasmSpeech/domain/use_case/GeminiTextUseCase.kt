package com.example.wasmapplication.core.features.wasmSpeech.domain.use_case


import android.annotation.SuppressLint
import com.example.wasmapplication.core.Resource
import com.example.wasmapplication.core.constant.FailureMsg
import com.example.wasmapplication.core.features.wasmSpeech.domain.repository.GeminiAiRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class GeminiTextUseCase  @Inject constructor(
    private val repositoryGemini: GeminiAiRepository,
   ) {

@SuppressLint("SuspiciousIndentation")
operator  fun invoke(inputText:String): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading<String>())
            val result=repositoryGemini.sendMessage(inputText)
            if(result!=null && result.isNotBlank()){
                emit(Resource.Success<String>(result))
            }else{
                emit(Resource.Error<String>(FailureMsg.GEMINI_RETURN_NULL_EN))
            }
        } catch (e: HttpException) {
            emit(Resource.Error<String>(e.localizedMessage ?: e.message?:"Http Error"))
        } catch (e: IOException) {
            emit(Resource.Error<String>(e.message?:"IO Error"))
        }
    }

}