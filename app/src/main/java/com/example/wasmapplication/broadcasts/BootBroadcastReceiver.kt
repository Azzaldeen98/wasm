package com.example.wasmapplication.broadcasts

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.wasmapplication.core.constant.STORAGE_RECORD_SERVICE_STATE
import com.example.wasmapplication.core.local_storage.ExternalStorage
import com.example.wasmapplication.services.RecordVoiceLifeCycleService
import com.example.wasmapplication.services.RecordVoiceService

class BootBroadcastReceiver : BroadcastReceiver() {
    @SuppressLint("NewApi")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != null && intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, RecordVoiceLifeCycleService::class.java)
            try {
                val state = ExternalStorage.getBooleanValue(context, STORAGE_RECORD_SERVICE_STATE)
                if (state) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.startForegroundService(serviceIntent)
                    }
                    else
                        context.startService(serviceIntent)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
}