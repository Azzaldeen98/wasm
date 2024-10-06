package com.example.wasmapplication.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.exoplayer.ExoPlayer
import com.example.wasm.core.android_api.media.ExoPlayerMedia
import com.example.wasmapplication.MainActivity
import com.example.wasmapplication.R
import com.example.wasmapplication.broadcasts.NetworkChangeReceiver
import com.example.wasmapplication.core.Notifications.LocalNotification
import com.example.wasmapplication.core.Resource
import com.example.wasmapplication.core.TestConnection
import com.example.wasmapplication.core.android_api.speech_recognizer.ISpeechRecognizerCallBack
import com.example.wasmapplication.core.android_api.speech_recognizer.SpeechRecognizerService
import com.example.wasmapplication.core.constant.Constants
import com.example.wasmapplication.core.enums.NotificationsId
import com.example.wasmapplication.core.features.wasmSpeech.domain.use_case.GeminiTextStreamUseCase
import com.example.wasmapplication.core.features.wasmSpeech.domain.use_case.GeminiTextUseCase
import com.example.wasmapplication.core.features.wasmSpeech.domain.use_case.GeminiTextWasmQueryUseCase
import com.example.wasmapplication.core.features.wasmSpeech.domain.use_case.WasmQueryUseCase
import com.example.wasmapplication.core.features.wasmSpeech.domain.use_case.WasmSecondQueryUseCase
import com.example.wasmapplication.core.interfaces.IBaseCallbackListener
import com.example.wasmapplication.core.interfaces.ICustomPlayerListener
import com.example.wasmapplication.core.interfaces.IWasmServiceEventListener
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

@AndroidEntryPoint
class RecordVoiceService: Service(), ISpeechRecognizerCallBack{ // IWasmServiceEventListener {

    @Inject lateinit var geminiTextWasmQueryUseCase: GeminiTextWasmQueryUseCase
    @Inject lateinit var wasmQueryUseCase: WasmQueryUseCase
    @Inject lateinit var wasmSecondQueryUseCase: WasmSecondQueryUseCase
    @Inject lateinit var geminiTextStreamUseCase: GeminiTextStreamUseCase

    //    @Inject lateinit var localNotification: LocalNotification
    //    @Inject lateinit var geminiTextUseCase: GeminiTextUseCase

    //    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    private lateinit var  speechRecognizerService: SpeechRecognizerService;
    private lateinit var exoPlayerMedia : ExoPlayerMedia;
    private lateinit var scope:CoroutineScope;

    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println ( "Coroutine Exception: $throwable" )
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    super.onStartCommand(intent, flags, startId)
        try {

            startForegroundServiceWithNotification(this,getString(R.string.notify1_Automated_msg));

//            networkChangeReceiver = NetworkChangeReceiver()
//            val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
//            registerReceiver(networkChangeReceiver, intentFilter)

            speechRecognizerService=SpeechRecognizerService(this,this)
            speechRecognizerService?.initialization(
                recognizer = true,
                workingInTheContinuously = true,
                lang = "ar"
            );
            scope= CoroutineScope(Dispatchers.IO)
            exoPlayerMedia=ExoPlayerMedia(this)
            speechRecognizerService?.speechRecognizerListenAgain()
            Toast.makeText(this, "Background is working ", Toast.LENGTH_SHORT).show()
        }
        catch (e:Exception){
            e.printStackTrace()
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
            stopSelf()
        }
        return START_STICKY
    }
    override fun onSpeechRecognizerResult(result:String?){

        if(result?.isNullOrBlank()==false){
//            sendRequestToGeneratorBasic(result)
            sendRequestToGeminiTextGeneratorStream(result)
        }else{
            scope.launch(Dispatchers.Main) {
                speechRecognizerService.speechRecognizerListenAgain()
            }
        }
    }
//    @SuppressLint("SuspiciousIndentation")
    private  fun sendRequestToGeminiTextGeneratorStream(result: String) {

        if (TestConnection.isOnline(this, false)) {
            try {
                scope.launch(Dispatchers.IO+exceptionHandler) {
                    val response = geminiTextStreamUseCase(result)
//                    response?.flowOn(Dispatchers.IO)
                    response?.collect { it ->
                        when (it) {
                            is Resource.Loading -> {}
                            is Resource.Success -> {
                                if (it.data is String) {
                                    if (it.data != Constants.END_SYMBOL) {
                                        sendRequestToSecondWasmQuery(it.data, false)
                                    } else {
                                        startSpeechRecognition()
                                    }
//                                            withContext(Dispatchers.Main) {
//                                                Toast.makeText(
//                                                    this@RecordVoiceService,
//                                                    it.data as String,
//                                                    Toast.LENGTH_SHORT
//                                                ).show()
//                                            }
                                } else {
                                    startSpeechRecognition()
                                }

                            }

                            is Resource.Error -> {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@RecordVoiceService,
                                        it.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                startSpeechRecognition()
                            }

                        }
                    }
                }
            } catch (e:Exception) {
                scope?.launch(Dispatchers.Main) {
                    speechRecognizerService.reStartListening()
                }
            }
        } else {
           // notifyNoInternetConnection()
        }
    }
    private suspend  fun sendRequestToSecondWasmQuery(result: String, lastSequence:Boolean=true) {
        if (TestConnection.isOnline(this, false)) {

            try {
                val data = wasmSecondQueryUseCase(result)
                if (data != null) {
                    withContext(Dispatchers.Main) {
                        try {
                            exoPlayerMedia?.playMediaByteStream(data)
                            if (exoPlayerMedia?.isPlayer() == false){
//                                Toast.makeText(this@RecordVoiceService, "exoPlayer is not Player", Toast.LENGTH_SHORT).show()
                                delay(500)
                            }
                            while (exoPlayerMedia?.isPlayer() == true)
                                delay(1000)
//                           exoPlayerMedia?.setPlayerListener(object : ICustomPlayerListener<ExoPlayer> {
    //                                override fun onCompletionListener(
    //                                    mp: ExoPlayer?,
    //                                    isComplete: Boolean
    //                                ) {
    ////                                    super.onCompletionListener(mp, isComplete)
    ////                                        if (isComplete) {
    ////                                            speechRecognizerService.speechRecognizerListenAgain()
    ////                                        }
    //                                }
    //
    //                                override fun onErrorListener(
    //                                    mp: ExoPlayer?,
    //                                    error: java.lang.Exception
    //                                ) {
    ////                                    super.onErrorListener(mp, error)
    ////                                        speechRecognizerService.speechRecognizerListenAgain()
    //                                }
    //                            }, lastSequence)
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }
                }

            }catch (e:HttpException){
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RecordVoiceService,
                        "HTTP-${e.code()}:${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        } else {
            notifyNoInternetConnection()
        }
    }
//    @SuppressLint("SuspiciousIndentation")
    private  fun sendRequestToGeneratorBasic(result: String) {

        if (TestConnection.isOnline(this, false)) {
            try {
                scope?.launch(Dispatchers.IO) {
                    val flowRes=geminiTextWasmQueryUseCase(result)
                    flowRes?.let {
                        it.flowOn(Dispatchers.IO)
                        it.collect{ it->
                            when (it) {
                                is Resource.Loading -> {}
                                is Resource.Success -> {
                                    if (it.data is ByteArray) {
                                        withContext(Dispatchers.Main) {
                                            exoPlayerMedia?.setPlayerListener(object : ICustomPlayerListener<ExoPlayer>{
                                                override fun onCompletionListener(mp: ExoPlayer?, isComplete: Boolean) {
                                                    super.onCompletionListener(mp, isComplete)
                                                    if(isComplete){
                                                        speechRecognizerService.speechRecognizerListenAgain()
                                                    }
                                                }
                                                override fun onErrorListener(mp: ExoPlayer?, error: java.lang.Exception) {
                                                    super.onErrorListener(mp, error)
                                                    speechRecognizerService.speechRecognizerListenAgain()
                                                }
                                            },true)
                                            exoPlayerMedia?.playMediaByteStream(it.data)
                                        }
                                    }else{
                                        startSpeechRecognition()
                                    }
                                }
                                is Resource.Error -> {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@RecordVoiceService,
                                            it.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    startSpeechRecognition()
                                }
                            }
                        }
                    }
                }

            } catch (e:Exception) {
                scope?.launch(Dispatchers.Main) {
                    speechRecognizerService.speechRecognizerListenAgain()
                }
            }
        } else {
            notifyNoInternetConnection()
//            reconnectOnInternet()
        }
    }
    private suspend fun startSpeechRecognition() {
        withContext(Dispatchers.Main) {
            speechRecognizerService.reStartListening()  // إعادة تشغيل خدمة التعرف
        }
//         scope= CoroutineScope(dispatcher)
//         scope?.launch {
//             try {
////                 withTimeout(20000) {  // تحديد مدة 5000 ميلي ثانية (5 ثواني)
//                     speechFlow.collect { speechText ->
//                         speechText?.let {
//                             processSpeechText(it)
//                         }
////                     }
//                 }
//             } catch (e: TimeoutCancellationException) {
//                 println("Coroutine timed out and was cancelled.")
//
//             }
//         }
    }
//    @SuppressLint("SuspiciousIndentation")
    private suspend fun convertTextToSpeech(text:String, lastSequence:Boolean=true){
//        scope.launch(Dispatchers.IO) {
        try {

            val flowRes = wasmQueryUseCase(text)
            flowRes?.let {
                it.flowOn(Dispatchers.IO)
                    .collect { result ->
                        when (result) {
                            is Resource.Loading -> {}
                            is Resource.Success -> {
                                if (result?.data != null && result?.data is ByteArray) {
                                    withContext(Dispatchers.Main) {
//                                   Toast.makeText(
//                                       this@RecordVoiceService,
//                                       "Resource.Success",
//                                       Toast.LENGTH_SHORT
//                                   ).show()

                                        exoPlayerMedia?.setPlayerListener(object : ICustomPlayerListener<ExoPlayer>{
                                            override fun onCompletionListener(mp: ExoPlayer?, isComplete: Boolean) {
                                                super.onCompletionListener(mp, isComplete)
                                                Toast.makeText(
                                                    this@RecordVoiceService,
                                                    "onCompletionListener:$lastSequence",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                if(lastSequence){
                                                    speechRecognizerService.speechRecognizerListenAgain()
                                                }
                                            }

                                            override fun onErrorListener(mp: ExoPlayer?, error: java.lang.Exception) {
                                                super.onErrorListener(mp, error)
                                                speechRecognizerService.speechRecognizerListenAgain()
                                            }
                                        },lastSequence)
                                        exoPlayerMedia?.playMediaByteStream(result.data)

//                                    while (simaphor.withPermit { exoPlayerMedia?.isPlayer() } == true)
//                                        delay(1000)
//                                    speechRecognizerService.reStartListening()


                                    }
//                       val tempFile=Helper.createTempFileAudio(Constants.TEMP_FILE_AUDIO_NAME,result?.data,this@RecordVoiceService)
                                } else {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@RecordVoiceService,
                                            "Data is Null",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }

                            is Resource.Error -> {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@RecordVoiceService,
                                        result?.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                startSpeechRecognition()

                            }
                        }
                    }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            startSpeechRecognition()
        }
//        }
    }

    //===========================================================================================

    // Method to create the notification channel
    private fun createNotificationChannel(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Record_Audio_Service",
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
    }
    // Method to start the foreground service with notification
    private fun startForegroundServiceWithNotification(context: Context,description: String) {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent:PendingIntent
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(description)
            .setSmallIcon(R.drawable.baseline_mic_24)
            .addAction(R.drawable.baseline_mic_off_24, "إيقاف", pendingIntent)
            .setContentIntent(pendingIntent)
            .build()
        // Create the notification channel
        createNotificationChannel(context)
        // Start the foreground service with the notification
        startForeground(NotificationsId.FOREGROUND_RECORD_SERVICE.ordinal, notification)
    }
    private fun notifyNoInternetConnection() {
//        localNotification.showNotification(
//            this,
//            NotificationsId.LOCAL_NOTIFICATION.ordinal,
//            getString(R.string.msg_no_internet_ar),
//            getString(R.string.msg_no_internet),
//            R.drawable.baseline_signal_wifi_connected_no_internet_4_24
//        )
    }
    private fun reconnectOnInternet() {
        scope?.launch {
            while (isActive && !TestConnection.isOnline(this@RecordVoiceService, false)) {
                delay(2000)
            }
        }
    }
    //===========================================================================================

    override fun stopService(name: Intent?): Boolean {
        Log.d("Stopping","Stopping Service")
        speechRecognizerService.stopSpeechRecognizer()
        return super.stopService(name)
    }
    override fun onDestroy() {
        super.onDestroy()
        try {
            scope?.cancel()
        } finally {
            try {
                exoPlayerMedia.stop()
            }finally {
                speechRecognizerService.destroy()
            }
        }

    }
    override fun onBind(intent: Intent?): IBinder? {
       return null
    }

//===================================================================================================
//    private  fun sendRequestToGenerator2(speechText:String) {
//
//    try {
//
////            if(TestConnection.isOnline(this@RecordVoiceService,false)) {
//////                lifecycleScope?.launch{
////                    try {
////                        if (scope?.isActive == true) {
//////                        if (job?.isActive == true) {
//////                            job?.cancel()
//////                        }
////                            scope?.cancel()
////                        }
////
////                    } finally {
////                        scope= CoroutineScope(dispatcher)
////                        job = speechChatControl?.generateStreamTextAudio7Last4(
////                            speechText,
////                            dispatcher,
////                            scope!!
////                        )
////                        job?.invokeOnCompletion {
////                            println("End-CoroutineScope...");
////                            scope?.launch(Dispatchers.Main) {
//////                            simaphor?.withPermit {
//////                                 scope?.cancel()
////                                speechRecognizerService?.speechRecognizerListenAgain()
//////                            }
////                            }
////                        }
////                    }
//////                }
////
////            } else {
////
////                localNotification?.showNotification(this,
////                    getString(R.string.msg_no_internet_ar),
////                    getString(R.string.msg_no_internet),
////                    R.drawable.baseline_signal_wifi_statusbar_connected_no_internet_4_24)
////
////                scope?.launch {
////                    while(scope?.isActive==true && !TestConnection.isOnline(this@RecordVoiceService,false)){
////                        delay(2000)
////                    }
////                }
////                Log.e("Internet", "Not Connection Internet !!!!")
////            }
//
//    } catch (e: Exception) {
//        Log.e("Error !", e.message.toString())
//        try {
//            if(scope?.isActive==true){
//                scope?.cancel()
//            }
//        }finally {
//            speechRecognizerService.speechRecognizerListenAgain()
//        }
//
//    }
//}
//    @SuppressLint("SuspiciousIndentation")
//    private fun sendRequestToGenerator3(speechText: String) {
//
////        if (TestConnection.isOnline(this, false)) {
////
////                try {
////                    if(scope?.isActive==true)
////                    scope?.cancel()  // إلغاء الـ Scope الحالي إن وجد
////                } finally {
////                    scope = CoroutineScope(dispatcher)
////
////
//////                    runBlocking {
//////                    var stateFlow: MutableSharedFlow<SpeechChatBotState>? =null
////                        scope?.launch{
////
////                            var  stateFlow = speechChatControl?.generateStreamTextAudio7Last6(
////                                speechText,
////                                scope!!)
////                            stateFlow?.collect { state ->
////                                when (state) {
////                                    is Listening -> Log.d("SpeechChatBotState", "Listening")
////                                    is Completed -> {
////                                        Log.d("SpeechChatBotState", "Completed")
////                                        isResponse=true
////                                       this@launch.cancel()
////                                    }
////                                    is Error -> {
////                                        Log.d("SpeechChatBotState", "Error: ${state?.message}")
////                                        state?.message
////                                        this@launch.cancel()
////                                    }
////                                    is Exception ->  this@launch.cancel()
////                                    is Initial -> {
////                                        Log.d("SpeechChatBotState", "Initial")
////                                    }
////                                    else -> {
////                                        Log.d("SpeechChatBotState", "Else")
////                                    }
////                                }
////
////                            }
////                    }?.invokeOnCompletion {
////                            scope?.launch(Dispatchers.Main) {
////                                speechRecognizerService?.speechRecognizerListenAgain()
////                            }
////                        }
//////                    job?.join()
//////                    delay(3000)
////
//////                        job?.invokeOnCompletion {
//////                            scope?.launch(Dispatchers.Main) {
//////                                speechRecognizerService?.speechRecognizerListenAgain()
//////                            }
//////                        }
////
////                }
////
////        } else {
////            notifyNoInternetConnection()
////            reconnectOnInternet()
////        }
//    }
//    fun startTimerWithScope(scope: CoroutineScope?) {
//        val timer = Timer()
//
//        // جدولة عملية يتم تنفيذها بعد 5 ثواني
//        timer.schedule(object : TimerTask() {
//            override fun run() {
//                // إطلاق Coroutine في النطاق المحدد
//                scope?.launch {
//                    // هنا يمكنك وضع الكود الذي تريد تنفيذه
//                    println("Task executed after delay!")
//                }
//            }
//        }, 5000)  // التأخير بـ 5000 ميلي ثانية (5 ثواني)
//    }
//    private fun processSpeechText(speechText: String) {
//
////        scope?.launch {
////            simaphor.withPermit { isResponse=false }
////            // إرسال النص لتحويله إلى صوت
////            speechChatControl?.generateStreamTextAudio7Last7(speechText, scope!!)
//////            withContext(Dispatchers.Main) {
//////                speechRecognizerService?.speechRecognizerListenAgain()  // إعادة تشغيل خدمة التعرف
//////            }
////            simaphor.withPermit { isResponse=true }
////        }
////        scope?.launch {
////            while (!simaphor.withPermit { isResponse }){
////                delay(2000)
////            }
////            withContext(Dispatchers.Main) {
////                speechRecognizerService?.speechRecognizerListenAgain()  // إعادة تشغيل خدمة التعرف
////            }
////        }
//    }
//
//    @SuppressLint("SuspiciousIndentation")
//    private  fun sendRequestToGeminiTextGeneratorBasic(result: String) {
//
//
//        if (TestConnection.isOnline(this, false)) {
//            try {
//
//
////                Toast.makeText(this@RecordVoiceService, "${result}", Toast.LENGTH_SHORT).show()
//
//                scope.launch {
//
//                    val flowRes=geminiTextUseCase(result)
//                    flowRes?.collect{ it->
//
//                        when (it) {
//                            is Resource.Loading -> {}
//                            is Resource.Success -> {
////                                    withContext(Dispatchers.Main) {
////                                        Toast.makeText(
////                                            this@RecordVoiceService,
////                                            it.data as String,
////                                            Toast.LENGTH_SHORT
////                                        ).show()
////                                    }
////                                    startSpeechRecognition()
//                                if (it.data is String) {
//                                    convertTextToSpeech(it.data)
//                                }else{
//                                    startSpeechRecognition()
//                                }
//                            }
//
//                            is Resource.Error -> {
//                                withContext(Dispatchers.Main) {
//                                    Toast.makeText(
//                                        this@RecordVoiceService,
//                                        it.message,
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                                startSpeechRecognition()
//                            }
//
//                        }
//                    }
//
//
////                        val flowRes=geminiTextUseCase(result)
////                            flowRes?.collect { response ->
////                                when (response) {
////                                    is Resource.Loading -> {
////                                        Log.d(TAG, "Gemini Text Start Loading...")
////                                    }
////                                    is Resource.Success -> {
////                                        startSpeechRecognition()
//////                                        if (response.data is String) {
//////                                            convertTextToSpeech(response.data)
//////                                        }else{
//////                                            startSpeechRecognition()
//////                                        }
////                                    } is Resource.Error -> {
////                                    Toast.makeText(this@RecordVoiceService, response.message, Toast.LENGTH_SHORT).show()
////                                    startSpeechRecognition()
////                                }
//                }
////                            }
////
////                }
//
////                }
////                scope?.launch {
//
////                }
//            } catch (e:Exception) {
//                scope?.launch(Dispatchers.Main) {
//                    speechRecognizerService.speechRecognizerListenAgain()
//                }
//            }
//        } else {
//            notifyNoInternetConnection()
////            reconnectOnInternet()
//        }
//    }
//    @SuppressLint("SuspiciousIndentation")
//    private  fun sendRequestToGenerator(speechText: String) {
//
//        if (TestConnection.isOnline(this, false)) {
//
//            try {
//                if(scope?.isActive==true)
//                    scope?.cancel()  // إلغاء الـ Scope الحالي إن وجد
//            } finally {
//                scope = CoroutineScope(Dispatchers.IO)
//
//
////                    runBlocking {
////                    var stateFlow: MutableSharedFlow<SpeechChatBotState>? =null
//                scope?.launch{
//
////                   speechChatControl?.generateStreamTextAudio7Last7(
////                        speechText,
////                        scope!!)
//
//                }?.invokeOnCompletion {
//
//                    scope?.launch(Dispatchers.Main) {
//                        speechRecognizerService?.speechRecognizerListenAgain()
//                    }
//
//                }
//
//
////                    job?.join()
////                    delay(3000)
//
//
//
//            }
//
//        } else {
//            notifyNoInternetConnection()
//            reconnectOnInternet()
//        }
//    }
    //===========================================================================================

//    override fun startListener() {
////
//        try {
//            stopAudioPlayer()
//            speechRecognizerService?.speechRecognizerListenAgain()
//        }catch (e:java.lang.Exception){}
//    }
//    override fun onRequestIsFailure(error: String) {
//        scope?.launch(Dispatchers.Main) {
//            Log.e("onRequestIsFailure", error)
////            simaphor.withPermit {
//            isResponse = true
//            speechRecognizerService.speechRecognizerListenAgain()
////            }
//        }
//
//
//
//
////                runBlocking {
//
////                    isResponse=true
////                    if (audioPlayer==null)
////                        audioPlayer=AudioPlayer(this@RecordVoiceService)
////                    try {
////                        Log.d("onRequestIsFailure", error)
////                        if (speakerJob != null && speakerJob?.isActive == true) {
////                            speakerJob?.join()
////                            delay(1000)
////                        }
//
////                    }finally{
////                        playDefaultVoiceResponse(DefaultSoundResource.getAgainQuestions(this@RecordVoiceService), DefaultAudioStatus.After, true)
////                    }
////                }
//
//
//
//
////            if (isSpeaking){ //audioPlayer != null && audioPlayer?.isPlayer() == true) {
////                _jop = CoroutineScope(Dispatchers.IO).async {
////                    val duration = audioPlayer?.getRemainingDuration()?.toLong() ?: 0
////                    delay(duration)
////                }
////
////            }
//
////        } catch (e:Exception){
////            Log.d("Error", e.message.toString());
////        }finally {
////            Log.d("onRequestIsFailure", "")
//////            if(_jop!=null && _jop.isActive){
//////                _jop.invokeOnCompletion {
//////                    runBlocking {
//////                        withContext(Dispatchers.Main) {
//////                              playDefaultVoiceResponse(DefaultSoundResource.getAgainQuestions(this@RecordVoiceService), DefaultAudioStatus.After, true)
//////                        }
//////                    }
//////                }
//////            }else{
//////                 playDefaultVoiceResponse(DefaultSoundResource.getAgainQuestions(this@RecordVoiceService),DefaultAudioStatus.After,true)
//////            }
////
////        }
//    }
//    @SuppressLint("SuspiciousIndentation")
//    override fun onRequestIsSuccess2(callBack: IBaseCallbackListener<Any?>?) {
//
//        Log.d("onRequestIsSuccess2", "true")
//        try {
//            callBack?.onCallBackExecuted(null)
//        }finally {
//
//                isResponse = true
//                speechRecognizerService.speechRecognizerListenAgain()
//
////                  try {
////                      if(job?.isActive==true)
////                          job?.cancelAndJoin()
////                  }finally {
////                      isResponse = true
////                      speechRecognizerListenAgain();
////                  }
//
//            }
//    }
//    @SuppressLint("SuspiciousIndentation")
//    override fun onRequestIsSuccess(responseURL:String) {
//
////        if(job?.isActive==true)
////            job?.cancel()
////        CoroutineScope(Dispatchers.Main).launch {
////            try {
////
////                isResponse = true
//////                    if(responseURL==null || responseURL.isEmpty())
//////                        throw Exception("Responce is null");
////
//////                 if(exoPlayer!=null)
//////                     exoPlayer= ExoPlayerMedia(this);
////
//////                    (this as LifecycleOwner).lifecycle.addObserver(object : LifecycleObserver {
//////                        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
//////                        fun onPause() {
//////                            exoPlayer?.player?.pause()
//////                        }
//////
//////                        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//////                        fun onDestroy() {
//////                            exoPlayer?.player?.release()
//////                        }
//////                    })
////
////                exoPlayer?.start(responseURL, object : ICustomPlayerListener<ExoPlayer> {
////                    override fun onErrorListener(mp: ExoPlayer?) {
////                        try {
////                            exoPlayer?.stop()
////                        } finally {
////                            speechRecognizerService?.speechRecognizerListenAgain()
////                        }
////                    }
////
////                    override fun onCompletionListener(mp: ExoPlayer?) {
////                        try {
////                            exoPlayer?.stop()
////                        } finally {
////                            speechRecognizerService?.speechRecognizerListenAgain()
////                        }
////                    }
////                });
////                speechRecognizerService?.speechRecognizerListenAgain()
////
////
////
//////                    speechResponseResult(responseURL)
////
////            } catch (e: Exception) {
////                Log.e("responseError", e.message.toString());
////                speechRecognizerService?.speechRecognizerListenAgain()
////
//////                    playDefaultVoiceResponse(DefaultSoundResource.getAgainQuestions(this@RecordVoiceService),DefaultAudioStatus.After,true)
////            }
////        }
//
//
//    }
//    @SuppressLint("SuspiciousIndentation")
//    fun speechResponseResult(result: String) {
//
//        try {
//            isResponse=true
////            if( result?.isNullOrEmpty()==false  && Helper.isLocalAudioFile(result)){
////                Log.e("speechResponseResult: ", result);
////                var player = audioPlayer?.start(result)
////                player?.setOnErrorListener { mp, what, extra ->
////                    try {
////                        isSpeaking = false
////                        Log.e("errorPlyer", "OnErrorListener");
////                        audioPlayer?.takeIf { it.isPlayer() }?.stop()
////                    } catch (e: Exception) {
////                        Log.e("Audio Player has Error", e.message.toString()); }
////                    finally {     speechRecognizerService?.speechRecognizerListenAgain(); }
////                    true // Return true if the error is considered handled, false otherwise
////                }
////                player?.setOnCompletionListener { mp ->
////                    try {
////                        isSpeaking = false
////                        audioPlayer?.takeIf { it.isPlayer() }?.stop()
////                        Log.d("Complate Plyer", "Complate Plyer Museic");
////                    } catch (e: Exception) { Log.e("Complate Plyer", e.message.toString()); }
////                    finally {     speechRecognizerService?.speechRecognizerListenAgain(); }
////                }
////
////            }else{
////                throw  Exception("audioPlayer is null !!");
////            }
//
//        }catch (e:Exception){
//            speechRecognizerService.speechRecognizerListenAgain()
////            playDefaultVoiceResponse(DefaultSoundResource.getAgainQuestions(this@RecordVoiceService),DefaultAudioStatus.After,true)
//        }
//    }

    companion object {
        const val LOG_TAG = "AudioRecordService"
        const val TAG = "RecordVoiceService"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "SERVICE_CHANNEL_ID"

//        fun  startService(context: Context){
//            val serviceIntent = Intent(context, RecordVoiceService::class.java)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                context.startForegroundService(serviceIntent)
//            } else {
//                context.startService(serviceIntent)
//            }
//        }
//
//        fun  stopService(context: Context){
//
//            try {
//
//                val serviceIntent = Intent(context, RecordVoiceService::class.java)
//                 context.stopService(serviceIntent)
//
//            }catch (e:Exception){
//                e.printStackTrace()
//            }
//        }
    }
}