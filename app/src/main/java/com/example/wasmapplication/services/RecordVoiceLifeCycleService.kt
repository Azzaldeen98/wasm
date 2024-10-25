package com.example.wasmapplication.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.example.wasm.core.android_api.media.ExoPlayerMedia
import com.example.wasmapplication.MainActivity
import com.example.wasmapplication.R
import com.example.wasmapplication.core.Resource
import com.example.wasmapplication.core.TestConnection
import com.example.wasmapplication.core.android_api.speech_recognizer.ISpeechRecognizerCallBack
import com.example.wasmapplication.core.android_api.speech_recognizer.SpeechRecognizerService
import com.example.wasmapplication.core.enums.NotificationsId
import com.example.wasmapplication.core.features.wasmSpeech.domain.use_case.GeminiTextWasmQueryStreamUseCase
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class RecordVoiceLifeCycleService: LifecycleService(), ISpeechRecognizerCallBack{

    @Inject lateinit var geminiTextWasmQueryStreamUseCase: GeminiTextWasmQueryStreamUseCase

    //    @Inject lateinit var localNotification: LocalNotification
    //    @Inject lateinit var geminiTextUseCase: GeminiTextUseCase
    //    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    private lateinit var  speechRecognizerService: SpeechRecognizerService;
    private lateinit var exoPlayerMedia : ExoPlayerMedia;
    private  val semaphore :Semaphore= Semaphore(1);
    private  var startRecognizerSpeechListener :Boolean= false;
    private lateinit var job : Job;
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
        sendRequestToGeminiTextWasmSpeechStream(result)
    }
    private fun sendRequestToGeminiTextWasmSpeechStream(result: String?) {
        val exceptionHandler = CoroutineExceptionHandler  { _, throwable ->  suspend {
            ///TODO  restartRecognizerAgain()

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@RecordVoiceLifeCycleService,
                    "ExceptionHandler::${throwable.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
            println ( "Coroutine Exception: $throwable" )
        }
        if (TestConnection.isOnline(this, false)) {
          lifecycleScope.launch(Dispatchers.IO+exceptionHandler) {
              semaphore.withPermit {startRecognizerSpeechListener=false}
                try {
                        if(result?.isNullOrBlank()==false ){
                            val responseFlow = geminiTextWasmQueryStreamUseCase(result)
//                                ?.flowOn(Dispatchers.Main)
                                ?.cancellable()
                                ?.catch {
                                    restartRecognizerAgain()
                                    return@catch
                                }
                                ?.onCompletion {
                                    restartRecognizerAgain()
                                    return@onCompletion
                                }
                            responseFlow?.collect { it ->
                                    when (it) {
                                        is Resource.Loading -> {
//                                            if (it.message?.isNullOrBlank() == false) {
//                                                withContext(Dispatchers.Main) {
//                                                    Toast.makeText(
//                                                        this@RecordVoiceLifeCycleService,
//                                                        "${it.message}",
//                                                        Toast.LENGTH_SHORT
//                                                    ).show()
//                                                }
//                                            }
                                        }
                                        is Resource.Success -> {
                                            if (it.data is ByteArray && it.data.isNotEmpty()) {
                                                playMedia(it.data)
                                            }
                                        }
                                        is Resource.Error -> {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    this@RecordVoiceLifeCycleService,
                                                    "Error::${it.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
//                                            restartRecognizerAgain()
//                                            return@collect
                                        }
                                        is Resource.Complete -> {
//                                                        withContext(Dispatchers.Main) {
//                                                            Toast.makeText(
//                                                                this@RecordVoiceLifeCycleService,
//                                                                "is Complete",
//                                                                Toast.LENGTH_SHORT
//                                                            ).show()
//                                                        }
                                            restartRecognizerAgain()
                                            return@collect
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
                                            return@collect
                                        }
                                    }
                                }
                        }
                        else{
                            restartRecognizerAgain()
                        }
                    } catch (e:Exception) {
                        restartRecognizerAgain()
                    }
                }
        } else {
           // notifyNoInternetConnection()
        }
    }
    private suspend fun restartRecognizerAgain(){
        withContext(Dispatchers.Main) {
//            semaphore.withPermit {
                if(!startRecognizerSpeechListener){
                    startRecognizerSpeechListener=true
//                    Toast.makeText(
//                        this@RecordVoiceLifeCycleService,
//                        "reStartListening",
//                        Toast.LENGTH_SHORT
//                    ).show()
                    speechRecognizerService?.reStartListening()
//                }
            }
        }
    }
    @OptIn(UnstableApi::class)
    private suspend fun playMedia(data:ByteArray){
        withContext(Dispatchers.Main) {
            try {

                 exoPlayerMedia?.playerMediaStreamAndWait(data)

//                Toast.makeText(
//                    this@RecordVoiceLifeCycleService,
//                    "exoPlayerMedia is ",
//                    Toast.LENGTH_SHORT
//                ).show()

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
//            job?.let { if(it.isActive) it.cancel() }
            exoPlayerMedia?.let { it.release() }
//            exceptionHandler?.let { it.cancel() }
        }finally {
            speechRecognizerService?.let { it.destroy() }
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