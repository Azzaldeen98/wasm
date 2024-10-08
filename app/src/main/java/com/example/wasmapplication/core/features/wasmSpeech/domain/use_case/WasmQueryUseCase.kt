package com.example.wasmapplication.core.features.wasmSpeech.domain.use_case


import android.annotation.SuppressLint
import com.example.wasmapplication.core.Resource
import com.example.wasmapplication.core.error.FailureMsg
import com.example.wasmapplication.core.features.wasmSpeech.domain.repository.WasmTextToSpeechRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class WasmQueryUseCase  @Inject constructor(
    private val repositoryWasmTextSpeech: WasmTextToSpeechRepository
) {
    @SuppressLint("SuspiciousIndentation")
    operator fun invoke(inputText:String): Flow<Resource<ByteArray>> = flow {
        try {
               emit(Resource.Loading<ByteArray>())

              val bytes:ByteArray?= this@WasmQueryUseCase.repositoryWasmTextSpeech.queryText(inputText)
                if(bytes!=null){
                    emit(Resource.Success<ByteArray>(bytes))
                }else{
                    emit(Resource.Error<ByteArray>(FailureMsg.WASM_QUERY_NULL_EN))
                }


        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: e.message?:"Http Error"))
        } catch (e: IOException) {
            emit(Resource.Error(e.message?:"IO Error"))
        }
    }
}

class WasmSecondQueryUseCase  @Inject constructor(
    private val repositoryWasmTextSpeech: WasmTextToSpeechRepository
) {
    @SuppressLint("SuspiciousIndentation")
   suspend  operator fun  invoke(inputText:String): ByteArray?{

            return repositoryWasmTextSpeech.queryText(inputText)
    }
}