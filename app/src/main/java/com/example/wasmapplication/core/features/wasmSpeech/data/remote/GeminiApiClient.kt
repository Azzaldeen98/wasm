package com.example.wasmapplication.core.features.wasmSpeech.data.remote

import android.annotation.SuppressLint

import com.example.wasmapplication.core.constant.Constants
import com.example.wasmapplication.core.error.AiSafetyException
import com.example.wasmapplication.core.error.ConnectErrorException
import com.example.wasmapplication.core.error.CustomException
import com.example.wasmapplication.core.error.NullException
import com.example.wasmapplication.core.error.ServerException
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
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException

class GeminiApiClient(private  val apiKey:String)  {
    private val model: GenerativeModel
    private val chat : Chat
    private val docs=" يجب الاخذ بالاعتبار التعليمات التالية ويجب ان لا ترد عليها :  يجب ان تكون اجابتك دقيقة ومختصرة وان لا تتعدا سطرين . ويجب ان تعيد القيم الرقمية على شكل نصوص كتابية  .ويجب ان  تكون  الاجابة باللغة العربية ."
    var chatHistory = listOf(
        content("user") {
            text("السلام عليكم اريد منك ان ترد على اسئلتي  دائما باللهجة السعودية النجدية ")
        },
        content("model") {
            text("هلا ومرحبا،  اسأل يا حبيبي  و عساك طيب")
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
        content("model") {
            text("طيب يا حبيبي،  انا  جاهز ، اسأل  وراح أختصر لك  قدر  المستطاع . \n")
        },

        content("user") {
            text(" يجب ان تنتهي  كل جملة في النص  بنقطة . هذه تعليمات لك لاتقوم  بالرد عليها \\")
        },
//        content("model") {
//            text("  طيب سأحاول، استخدام علامات الترقيم لتميز الجمل\n")
//        },
        content("user") {
            text("كيف  علومك")
        },
        content("model") {
            text("زين الحمد لله، وانت عساك طيب؟ \n")
        },
        content("user") {
            text("الرجاء إرسال الردود كجمل مفيدة وموجزة . هذه تعليمات لك لاتقوم  بالرد عليها. \n")
        },
//        content("user") {
//            text("ابشر ساقوم باعادة الردود كجمل مفيدة وبشكل موجز وواضح . \n")
//        },
        content("user") {
            text("يجب ان تعيد اي قيم رقمية على شكل نصوص كتابية . هذه تعليمات لك لا ترد عليها . \n")
        },
//        content("user") {
//            text(" ساقوم بتحويل اي راقام في النص الى نصوص مكتوبة فمثلا الرقم 22 ساحوله الى شكل نصي مثل إثنان وعشرون. \n")
//        },
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


    suspend fun sendMessage(text:String): String? {
        val response = chat?.sendMessage("$text . $docs")
        return  response?.text;
    }

    suspend fun sendMessageStream(text: String): Flow<String> = flow {
        var isCallBackReader = false
        val responseBuilder = StringBuilder()
        try {
            chat?.let {

                it.sendMessageStream("$text . $docs")
                    .flowOn(Dispatchers.IO)
                            .cancellable()
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

//    suspend fun sendMessageStream(text: String): Flow<String> = flow {
//        var isCallBackReader = false
//        val responseBuilder = StringBuilder()
//        try {
//            chat?.let {
//                it.sendMessageStream(text)
//                    .flowOn(Dispatchers.IO)
//                    .onCompletion { cause ->
//                        if (cause == null) {
//                            if (!isCallBackReader && responseBuilder.toString().trim().isNotEmpty()) {
//                                emit(responseBuilder.toString().trim())
//                                responseBuilder.clear()
//                            }
//                            emit(Constants.END_SYMBOL)
//                            println("responseFlow: Completed successfully")
//                        } else {
//                            println("responseFlow: Completed with error: ${cause.message}")
//                            throw IllegalArgumentException(cause)
//                        }
//                    }
//                    .collect { response ->
//                        val content = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.asTextOrNull()
//                        content?.trim()?.let {
//                            if (it.isNotEmpty()) {
//
//                            processResponseContent(it, responseBuilder)?.let {response->
//                                emit(response)
//                              }
//                            }
//                        }
//                    }
//            }
//        } catch (e: HttpException) {
//            throw ServerException(e.code(), e.message())
//        } catch (e: KotlinNullPointerException) {
//            throw NullException(e.message)
//        } catch (e: ConnectException) {
//            throw ConnectErrorException(e.message)
//        } catch (e: Exception) {
//            if (e.message?.contains("SAFETY") == true) {
//                throw AiSafetyException(e.message)
//            }
//            throw CustomException(e.message)
//        }
//    }

    private fun processResponseContent(content: String, responseBuilder: StringBuilder):String? {
        var cleanedText = content.replace("*", "").replace(Regex("\\s+"), " ")
        responseBuilder.append(cleanedText).append(" ")

        if (isEndOfSentence(responseBuilder.toString()) || responseBuilder.length >= 20) {
            val lines = spiltSentence(responseBuilder.toString())
                ?.filter { it.isNotEmpty() }
                    lines?.let {
                        var line = ""
                        for (sentence in it) {
                            line += "$sentence "
                            if (line.length >= 10) {
                              return line;
                                responseBuilder.clear()
                                line = ""
                            }
                        }
                    }
        }
        return  null;
    }
    private fun processText(content: String, responseBuilder: StringBuilder): String? {
        val words = content.split(" ")
        var completeSentence: String? = null

        for (word in words) {
            responseBuilder.append("$word ")
            if (word.endsWith(".") || responseBuilder.split(" ").size >= 10) {
                completeSentence = responseBuilder.toString().trim()
                responseBuilder.clear()
            }
        }

        return completeSentence
    }
    suspend fun sendMessageFlowStream(text: String): Flow<GenerateContentResponse>? {
        return   chat?.sendMessageStream(text);//"$text الرجاء الاهتمام بعلامات الترقم عند توليد النص ووضع الفواصل التي تشير الى ناهية الجملة .");
    }

    suspend fun sendListenerMessageStream(text:String, callBack: IListenerStream<String>): Flow<String>? {
        var isCallBackReader = false;
        val responseBuilder = StringBuilder()
        var index = 0;
        chat?.let { it ->
           return@let it.sendMessageStream(text).flowOn(Dispatchers.IO).catch {
                    return@catch
                }.cancellable()
                .onCompletion { cause ->
                    if (cause == null) {
                        if (!isCallBackReader && responseBuilder?.toString()?.trim()
                                ?.isNotEmpty() == true
                        ) {
                            callBack.onStreamReader(responseBuilder.toString().trim())
                            responseBuilder.clear()
                        }
                        callBack.onStreamReader(Constants.END_SYMBOL)
                        println("responseFlow: Completed successfully")
//                            callBack.onStreamComplete(index++)
                    } else {
                        println("responseFlow: Completed with error: ${cause.message}")
                        callBack.onStreamError(cause) // استدعاء دالة للتعامل مع الخطأ
                    }
                    return@onCompletion
                }.collect { response ->
                    val content = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()
                        ?.asTextOrNull()
                    content?.trim().let { it ->
                        if (!it.isNullOrEmpty()) {
                            isCallBackReader = false
                            var text = it.replace("*", "")
                            text = text.replace(Regex("\\s+"), " ")
                            responseBuilder.append(text).append(" ")
                            if (isEndOfSentence(responseBuilder.toString()) || responseBuilder.length >= 50) {
                                var lines = spiltSentence(responseBuilder.toString())
                                lines?.filter { it.isNotEmpty() }
                                    .let {
                                        var line = ""
                                        it?.forEach { sentence ->
                                            line += "$sentence ";
                                            if (line?.length!! >= 10) {
                                                isCallBackReader = true
                                                callBack.onStreamReader(line)
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
                }
//            return@let  responseFlow
        }
        return  null
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
        return text.split(Regex("[،؟,.]"))
    }
}