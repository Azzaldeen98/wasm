package com.example.wasmapplication.core.helpers

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.example.wasmapplication.core.constant.STORAGE_RECORD_SERVICE_STATE
import com.example.wasmapplication.core.local_storage.ExternalStorage
import com.example.wasmapplication.services.RecordVoiceService

object ManageService {

    fun  startService(context: Context,  serviceClass: Class<*>){
        try {

            if(checkForegroundServiceIsRunning(context,serviceClass))
                 return

            val serviceIntent= Intent(context, serviceClass)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            ExternalStorage.storage(context, STORAGE_RECORD_SERVICE_STATE,true);

        }catch (e:Exception){
            e.printStackTrace()
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun checkForegroundServiceIsRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = manager.getRunningServices(Int.MAX_VALUE)
        for (service in runningServices) {
            if (serviceClass.name == service.service.className) {
                if (service.foreground) {
                    return service.foreground
                }
            }
        }
        return false
    }



    fun  stopService(context: Context, serviceClass: Class<*>){
        try {

            if(checkForegroundServiceIsRunning(context,serviceClass)) {
                val serviceIntent = Intent(context, serviceClass)
                  context.stopService(serviceIntent)
                ExternalStorage.storage(context, STORAGE_RECORD_SERVICE_STATE,false);
            }

        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}