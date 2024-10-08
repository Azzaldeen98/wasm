package com.example.wasmapplication.core.features.wasmSpeech.domain.use_case
import android.annotation.SuppressLint
import com.example.wasmapplication.core.Resource
import com.example.wasmapplication.core.error.FailureMsg
import com.example.wasmapplication.core.features.wasmSpeech.domain.repository.GeminiAiRepository
import com.example.wasmapplication.core.features.wasmSpeech.domain.repository.WasmTextToSpeechRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class GeminiTextWasmQueryUseCase  @Inject constructor(private val repositoryGemini: GeminiAiRepository,
    private val repositoryWasmTextSpeech: WasmTextToSpeechRepository) {

    @SuppressLint("SuspiciousIndentation")
    operator fun invoke(inputText:String): Flow<Resource<Any>> = flow {
        try {
            emit(Resource.Loading())
            val result=repositoryGemini.sendMessage(inputText)
            if(result!=null && result.isNotBlank()){
//                emit(Resource.Success(result))
                try {

                    val bytes: ByteArray? = repositoryWasmTextSpeech.queryText(result)
                    if (bytes != null) {
                        emit(Resource.Success(bytes))
                    }
                    else {
                        emit(Resource.Error(FailureMsg.WASM_QUERY_NULL_EN))
                    }
                }
                catch (e:HttpException){
                    emit(Resource.Error(e.localizedMessage ?: "${e.message?:"Http Error"}-${e.code()}"))
                }
                catch (e:Exception){
                    emit(Resource.Error(e.localizedMessage ?: e.message?:"Http Error"))
                }
            }else{
                emit(Resource.Error(FailureMsg.GEMINI_RETURN_NULL_EN))
            }

        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: e.message?:"Http Error"))
        } catch (e: IOException) {
            emit(Resource.Error(e.message?:"IO Error"))
        }
    }
}

