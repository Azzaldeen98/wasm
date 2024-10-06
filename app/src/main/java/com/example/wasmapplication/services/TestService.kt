//package com.example.wasmapplication.services
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.app.Service
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.os.IBinder
//import android.util.Log
//import android.widget.Toast
//import androidx.core.app.NotificationCompat
//import androidx.core.app.NotificationManagerCompat
//import com.example.wasmapplication.MainActivity
//import com.example.wasmapplication.R
//import com.example.wasmapplication.core.Notifications.LocalNotification
//import com.example.wasmapplication.core.android_api.speech_recognizer.SpeechRecognizerService
//import dagger.hilt.android.AndroidEntryPoint
//import jakarta.inject.Inject
//
//interface  TestServiceInterface{
//    fun  startService(context: Context):String
//    fun  stopService(context: Context)
//}
//
//
//class  TestServiceInterfaceImpl:TestServiceInterface{
//    @Inject lateinit var localNotification: LocalNotification
//    override fun  startService(context: Context):String{
//
//        return "startService"
//    }
//    override fun  stopService(context: Context){
//        localNotification.showNotification(context,"Title","TestServiceInterfaceImpl")
//    }
//}
//@AndroidEntryPoint
//class TestService : Service() {
//
//
//    @Inject lateinit var testServiceInterface: TestServiceInterfaceImpl
//    private lateinit var speechRecognizerService: SpeechRecognizerService
//    private lateinit var notificationManager : NotificationManagerCompat
//
//    override fun onCreate() {
//        super.onCreate()
//
//        notificationManager = NotificationManagerCompat.from(this)
//        // Create the notification channel
//        createNotificationChannel(this)
//
//
//    }
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        super.onStartCommand(intent, flags, startId)
//        try {
//            startForegroundServiceWithNotification(this,getString(R.string.notify1_Automated_msg));
//            Log.e("onStartCommand","onStartCommand")
////            testServiceInterface.stopService(this)
////            Toast.makeText(this, testServiceInterface.startService(this), Toast.LENGTH_SHORT).show()
//        }
//        catch (e:Exception){
//            e.printStackTrace()
//            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
//            stopSelf()
//        }
//        return START_STICKY
//    }
//    // Method to create the notification channel
//    private fun createNotificationChannel(context: Context) {
//
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                CHANNEL_ID,
//                "Record_Audio_Service",
//                NotificationManager.IMPORTANCE_HIGH)
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//    // Method to start the foreground service with notification
//    private fun startForegroundServiceWithNotification(context: Context,description: String) {
//        val notificationIntent = Intent(context, MainActivity::class.java)
//        val pendingIntent:PendingIntent
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
//            pendingIntent = PendingIntent.getActivity(
//                context,
//                0,
//                notificationIntent,
//                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//            )
//        } else {
//            pendingIntent = PendingIntent.getActivity(
//                context,
//                0,
//                notificationIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT
//            )
//        }
//
//        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
//            .setContentTitle(getString(R.string.app_name))
//            .setContentText(description)
//            .setSmallIcon(R.drawable.baseline_mic_24)
//            .addAction(R.drawable.baseline_mic_off_24, "إيقاف", pendingIntent)
//            .setContentIntent(pendingIntent)
//            .build()
//
//        // Start the foreground service with the notification
//        startForeground(NOTIFICATION_ID, notification)
//    }
//    //===========================================================================================
//    override fun onDestroy() {
//        super.onDestroy()
//        try {
//        } finally {
//        }
//
//    }
//    override fun onBind(intent: Intent?): IBinder? {
//        TODO("Not yet implemented")
//    }
//    //===========================================================================================
//    companion object {
//        const val LOG_TAG = "AudioRecordService"
//        const val TAG = "RecordVoiceService"
//        const val NOTIFICATION_ID = 69
//        const val CHANNEL_ID = "SERVICE_CHANNEL_ID"
//
//        fun  startService(context: Context){
//            val serviceIntent = Intent(context, TestService::class.java)
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
//                val serviceIntent = Intent(context, TestService::class.java)
//                 context.stopService(serviceIntent)
//
//            }catch (e:Exception){
//                e.printStackTrace()
//            }
//        }
//
//    }
//}
//
