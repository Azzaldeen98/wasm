package com.example.wasmapplication.core.features.wasmSpeech.data.remote

import android.annotation.SuppressLint

import com.example.wasmapplication.core.constant.Constants
import com.example.wasmapplication.core.interfaces.IListenerStream
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion

class GeminiApiClient(private  val apiKey:String)  {
    private val model: GenerativeModel
    private val chat : Chat
    private  val docs=" يجب ان تكون اجابتك دقيقة ومختصرة وان لا تتعدا سطرين  ويجب ان تكون  الاجابة باللغة العربية"
    val chatHistory = listOf(
        content("user") {
            text("السلام عليكم اريد منك ان ترد على اسئلتي  دائما باللهجة السعودية النجدية ")
        },
        content("model") {
            text("هلا ومرحبا،  اسأل يا حبيبي  و عس")
        },
        content("user") {
            text("\"كيف حالك اخبارك\\")
        },
        content("model") {
            text("والله الحمد لله تمام،  وأنت وش اخبارك؟ عساك طيب؟ \n")
        },
        content("user") {
            text("اريد ايضا ان تكون اجابتك مختصره على سبيل المثال اكثر اجابة سطرين\\")
        },
        content("user") {
            text(" يجب ان تنتهي  كل جملة في النص  بنقطة  \\")
        },
        content("model") {
            text("طيب يا حبيبي،  انا  جاهز ، اسأل  وراح أختصر لك  قدر  المستطاع . \n")
        },
        content("user") {
            text("كيف  علومك")
        },
        content("model") {
            text("زين الحمد لله، وانت عساك طيب؟ \n")
        },
    )
    init {

        model = GenerativeModel(
            "gemini-1.5-flash",
           apiKey,

            generationConfig = generationConfig {
                temperature = 1f
                topK = 64
                topP = 0.95f
                maxOutputTokens = 8192
//                responseMimeType = "text/plain"
            },
            // safetySettings = Adjust safety settings
        )
        chat= model.startChat(chatHistory)
    }

//    @SuppressLint("SuspiciousIndentation")
    suspend fun sendMessage(text:String): String? {
        val response = chat?.sendMessage("$text . $docs")
        return  response?.text;
    }
    suspend fun sendMessageStream(text: String): Flow<String> = flow {
        var isCallBackReader = false
        val responseBuilder = StringBuilder()
        try {
            chat?.let {

                it.sendMessageStream(text)
                    .flowOn(Dispatchers.IO)
                    .onCompletion { cause ->
                        if (cause == null) {
                            if (!isCallBackReader && responseBuilder.toString().trim().isNotEmpty()) {
                                emit(responseBuilder.toString().trim())
                                responseBuilder.clear()
                            }
                            emit(Constants.END_SYMBOL)
                            println("responseFlow: Completed successfully")
                        } else {
                            println("responseFlow: Completed with error: ${cause.message}")
                            throw IllegalArgumentException(cause)
                        }
                    }.collect { response ->
                        val content =
                            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.asTextOrNull()
                        content?.trim()?.let {
                            if (it.isNotEmpty()) {
                                isCallBackReader = false
                                var cleanedText = it.replace("*", "").replace(Regex("\\s+"), " ")
                                responseBuilder.append(cleanedText).append(" ")
                                if (isEndOfSentence(responseBuilder.toString()) || responseBuilder.length >= 50) {
                                    val lines = spiltSentence(responseBuilder.toString())?.filter { it.isNotEmpty() }
                                    lines?.let {
                                        var line = ""
                                        for (sentence in it) {
                                            line += "$sentence "
                                            if (line.length >= 10) {
                                                isCallBackReader = true
                                                emit(line)
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
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun sendMessageStream(text:String, callBack: IListenerStream<String>) {
        var isCallBackReader = false;
        val responseBuilder = StringBuilder()
        var index = 0;
        chat?.let { it ->

            val responseFlow: Flow<GenerateContentResponse> = it.sendMessageStream(text)
            responseFlow.flowOn(Dispatchers.IO)
                .onCompletion { cause ->
                    if (cause == null) {
                        if (!isCallBackReader && responseBuilder?.toString()?.trim()
                                ?.isNotEmpty() == true
                        ) {
                            callBack.onStreamReader(responseBuilder.toString().trim(), index++, 0)
                            responseBuilder.clear()
                        }
                        callBack.onStreamReader(Constants.END_SYMBOL, index, index)
                        println("responseFlow: Completed successfully")
//                            callBack.onStreamComplete(index++)
                    } else {
                        println("responseFlow: Completed with error: ${cause.message}")
                        callBack.onStreamError(cause) // استدعاء دالة للتعامل مع الخطأ
                    }
                }
                .collect { response ->
                    val content = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()
                        ?.asTextOrNull()
                    content?.trim().let { it ->
                        if (!it.isNullOrEmpty()) {
                            isCallBackReader = false
                            var text = it.replace("*", "")
                            text = text.replace(Regex("\\s+"), " ")
                            responseBuilder.append(text).append(" ")
                            if (isEndOfSentence(responseBuilder.toString()) || responseBuilder.length >= 50) {
                                var lines = responseBuilder.toString()?.split(Regex("[،!؟?,.]"))
                                lines?.filter { it.isNotEmpty() }
                                    .let {
                                        var line = ""
                                        it?.forEach { sentence ->
                                            line += "$sentence ";
                                            if (line?.length!! >= 10) {
                                                isCallBackReader = true
                                                callBack.onStreamReader(line, index++, 0)
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
                    delay(200)
                }
        }
    }
    private fun isEndOfSentence(text: String): Boolean {
        return text.endsWith(".") || text.endsWith("!") || text.endsWith("?") || text.endsWith("،")
                || text.endsWith(",")   || text.endsWith("؟")
    }
    private fun spiltSentence(text: String): List<String> {
        return text.split(Regex("[،!؟?,.]"))
    }
}