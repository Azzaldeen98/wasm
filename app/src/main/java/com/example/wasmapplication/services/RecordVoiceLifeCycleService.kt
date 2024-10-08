package com.example.wasmapplication.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.media3.exoplayer.ExoPlayer
import com.example.wasm.core.android_api.media.ExoPlayerMedia
import com.example.wasmapplication.MainActivity
import com.example.wasmapplication.R
import com.example.wasmapplication.core.Resource
import com.example.wasmapplication.core.TestConnection
import com.example.wasmapplication.core.android_api.speech_recognizer.ISpeechRecognizerCallBack
import com.example.wasmapplication.core.android_api.speech_recognizer.SpeechRecognizerService
import com.example.wasmapplication.core.constant.Constants
import com.example.wasmapplication.core.enums.NotificationsId
import com.example.wasmapplication.core.features.wasmSpeech.domain.use_case.GeminiTextStreamUseCase
import com.example.wasmapplication.core.features.wasmSpeech.domain.use_case.GeminiTextWasmQueryFlowStreamUseCase
import com.example.wasmapplication.core.features.wasmSpeech.domain.use_case.GeminiTextWasmQueryStreamUseCase
import com.example.wasmapplication.core.features.wasmSpeech.domain.use_case.GeminiTextWasmQueryStreamUseCaseV2
import com.example.wasmapplication.core.features.wasmSpeech.domain.use_case.GeminiTextWasmQueryUseCase
import com.example.wasmapplication.core.features.wasmSpeech.domain.use_case.WasmQueryUseCase
import com.example.wasmapplication.core.features.wasmSpeech.domain.use_case.WasmSecondQueryUseCase
import com.example.wasmapplication.core.interfaces.ICustomPlayerListener
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import retrofit2.HttpException

@AndroidEntryPoint
class RecordVoiceLifeCycleService: LifecycleService(), ISpeechRecognizerCallBack{

//    @Inject lateinit var geminiTextWasmQueryUseCase: GeminiTextWasmQueryUseCase
//    @Inject lateinit var wasmQueryUseCase: WasmQueryUseCase
//    @Inject lateinit var wasmSecondQueryUseCase: WasmSecondQueryUseCase
//    @Inject lateinit var geminiTextStreamUseCase: GeminiTextStreamUseCase
    @Inject lateinit var geminiTextWasmQueryStreamUseCase: GeminiTextWasmQueryStreamUseCase
//    @Inject lateinit var geminiTextWasmQueryStreamUseCaseV2: GeminiTextWasmQueryStreamUseCaseV2
//    @Inject lateinit var geminiTextWasmQueryFlowStreamUseCase: GeminiTextWasmQueryFlowStreamUseCase

    //    @Inject lateinit var localNotification: LocalNotification
    //    @Inject lateinit var geminiTextUseCase: GeminiTextUseCase
    //    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    private lateinit var  speechRecognizerService: SpeechRecognizerService;
    private lateinit var exoPlayerMedia : ExoPlayerMedia;
    val exceptionHandler = CoroutineExceptionHandler  { _, throwable ->  suspend {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                this@RecordVoiceLifeCycleService,
                "Error::${throwable.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
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
            exoPlayerMedia=ExoPlayerMedia(this)
            speechRecognizerService?.speechRecognizerListenAgain()
            Toast.makeText(this, "Background is working ", Toast.LENGTH_SHORT).show()

        } catch (e:Exception){
            e.printStackTrace()
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
            stopSelf()
        }
        return START_STICKY
    }
    override fun onSpeechRecognizerResult(result:String?){

        if(result?.isNullOrBlank()==false){
            sendRequestToGeminiTextWasmSpeechStream(result)
        }else{
            lifecycleScope.launch(Dispatchers.Main) {
                speechRecognizerService.reStartListening()
            }
        }
    }
    private fun sendRequestToGeminiTextWasmSpeechStream(result: String) {

        if (TestConnection.isOnline(this, false)) {
            try {
                lifecycleScope.launch(Dispatchers.IO){
                    val response = geminiTextWasmQueryStreamUseCase(result)
                    response?.collect { it ->
                            when (it) {
                                is Resource.Loading -> {
                                    if(it.message?.isNullOrBlank()==false){
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                this@RecordVoiceLifeCycleService,
                                                "${it.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                                is Resource.Success -> {
                                        if (it.data is ByteArray && it.data.isNotEmpty() ) {
                                            playMedia(it.data)
                                        }
                                }
                                is Resource.Error -> {
                                    Log.e("SpeechError", it.message?:"")
//                                    withContext(Dispatchers.Main) {
//                                        Toast.makeText(
//                                            this@RecordVoiceService,
//                                         "Error::${ it.message}",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
                                    restartRecognizerAgain()
                                }
                                is Resource.Complete -> {
//                                    withContext(Dispatchers.Main) {
//                                        Toast.makeText(
//                                            this@RecordVoiceService,
//                                            "is Complete",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
                                    restartRecognizerAgain()
                                }
                                is Resource.FinalError -> {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@RecordVoiceLifeCycleService,
                                            "Final Error:${it.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    restartRecognizerAgain()
                                }
                            }
                    }
                }
            } catch (e:Exception) {
                lifecycleScope?.launch(Dispatchers.Main+exceptionHandler) {
                    restartRecognizerAgain()
//                    speechRecognizerService.reStartListening()
                }
            }
        } else {
           // notifyNoInternetConnection()
        }
    }
    private suspend fun restartRecognizerAgain(){
            withContext(Dispatchers.Main) {
                    speechRecognizerService.reStartListening()
            }
    }
    private suspend fun playMedia(data:ByteArray){
        var dueration:Long=500;
        withContext(Dispatchers.Main) {
            try {

                 exoPlayerMedia?.playerMediaStreamAndWait(data)
//                    exoPlayerMedia?.playMediaByteStream(data)

//                    if (exoPlayerMedia?.isPlayer() == false) {
//                        delay(500)
//                    }
//                 dueration=exoPlayerMedia?.getRemainingDuration()?:0;//exoPlayerMedia?.player?.duration?:500;
//                 delay(dueration)
//                    while (exoPlayerMedia?.isPlayer() == true)
//                        delay(500)

            }catch (e:Exception){
                e.printStackTrace()
            }
        }

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


    //===========================================================================================

    override fun stopService(name: Intent?): Boolean {
        Log.d("Stopping","Stopping Service")
        speechRecognizerService.stopSpeechRecognizer()
        return super.stopService(name)
    }
    override fun onDestroy() {
        super.onDestroy()

        try {
            exoPlayerMedia.release()
        }finally {
            speechRecognizerService.destroy()
        }
    }


//===================================================================================================


    companion object {
        const val LOG_TAG = "AudioRecordService"
        const val TAG = "RecordVoiceService"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "SERVICE_CHANNEL_ID"
    }
}