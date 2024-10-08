package com.example.wasmapplication.core.features.wasmSpeech.data.remote

import android.util.Log
import com.example.wasmapplication.R
import com.example.wasmapplication.core.constant.Constants
import com.example.wasmapplication.core.error.AiSafetyException
import com.example.wasmapplication.core.error.ServerException
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

interface WasmApiRemote {
    suspend fun queryTextHubaV2(text:String): ByteArray?
    suspend fun queryTextToSpeech(text:String): ByteArray?
}

class WasmApiRemoteImpl @Inject constructor(
    private  val  api: IWasmApiServices,
    private val httpConn: HttpURLConnection
) : WasmApiRemote {

    override suspend fun queryTextHubaV2(text: String): ByteArray? {
//            var client = module.provideWasmSpeechApi()
            val call: Call<ResponseBody?>? = api?.queryHubaV2(text) ?: null
            val response: Response<ResponseBody?>? = call?.execute()
            if (response?.isSuccessful == true) {
                var responseBody = response.body() ?: null
                val byteArray: ByteArray? = responseBody?.bytes()
                Log.d("response", "Byte array length: ${byteArray?.size}")
                return byteArray
            } else{
                throw HttpException(response)
            }
        }
    suspend  fun queryTextToSpeech2(input: String): ByteArray?  {

            val apiUrl="${Constants.WASM_BASE_URL}vits-ar-sa-huba-v2" //vits-ar-sa-A"
            val authorization = "Bearer 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";
            val maxRetries = 3
            var currentAttempt = 0
            while (currentAttempt < maxRetries) {
                try {
                    currentAttempt++
                    val url = URL(apiUrl)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Authorization", authorization)
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.doOutput = true

                    val jsonInputString = "{\"inputs\": \"$input\"}"

                    conn.outputStream.use { os ->
                        val inputBytes = jsonInputString.toByteArray(Charsets.UTF_8)
                        os.write(inputBytes, 0, inputBytes.size)
                    }

                    val responseCode = conn.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        return conn.inputStream.use { it.readBytes() }
                    } else {

                        val responseBody = ResponseBody.create("text/plain".toMediaTypeOrNull(), conn.responseMessage)
                        val response: Response<String> =Response.error(responseCode,responseBody)
                        throw HttpException(response)
                        Log.e("WasmApiAudio", "Failed with HTTP response code: $responseCode")
                        delay(1000)
                    }
                }
                catch (e: HttpException) {
                    throw HttpException(e.response())
                }
                catch (e: Exception) {
//                    Log.e("WasmApiAudio", "An error occurred: ${e.message}")
                    if (e.message?.contains("SAFETY") == true) {
                        throw AiSafetyException(e.message)
//                        Log.e("WasmApiAudio", "Content generation stopped due to safety reasons.")
                    }else if (currentAttempt >= maxRetries) {
                        e.printStackTrace()
                       throw  Exception(e)
                    } else {
//                        Log.w("WasmApiAudio", "Retrying... Attempt $currentAttempt")
                        delay(500)
                    }
                }
            }
            return null

    }
    override suspend  fun queryTextToSpeech(input: String): ByteArray?  {

            val apiUrl = "${Constants.WASM_BASE_URL}vits-ar-sa-huba-v2" //vits-ar-sa-A"
            val authorization = "Bearer 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";

            val url = URL(apiUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", authorization)
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val jsonInputString = "{\"inputs\": \"$input\"}"
            conn.outputStream.use { os ->
                val inputBytes = jsonInputString.toByteArray(Charsets.UTF_8)
                os.write(inputBytes, 0, inputBytes.size)
            }

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return conn.inputStream.use { it.readBytes() }
            } else {
                throw ServerException(responseCode, "ServerException:${conn.responseMessage}")
            }

    }
}
