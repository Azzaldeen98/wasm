package com.example.wasmapplication.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.wasmapplication.core.constant.STORAGE_RECORD_SERVICE_STATE
import com.example.wasmapplication.core.helpers.ManageService
import com.example.wasmapplication.core.local_storage.ExternalStorage
import com.example.wasmapplication.core.verifications.VerificationJobs
import com.example.wasmapplication.services.RecordVoiceService


class NetworkChangeReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {

        if(context!=null) {

            if (VerificationJobs.isConnectedToInternet(context)) {

                Toast.makeText(context, "متصل بالإنترنت", Toast.LENGTH_SHORT).show()
                val state = ExternalStorage.getBooleanValue(context, STORAGE_RECORD_SERVICE_STATE)
                if (state) {
                    ManageService.startService(context, RecordVoiceService::class.java)
                }

            } else {

                Toast.makeText(context, "غير متصل بالإنترنت", Toast.LENGTH_SHORT).show()
//            if( ManageService.checkForegroundServiceIsRunning(context,RecordVoiceService::class.java))
//                ManageService.stopService(context,RecordVoiceService::class.java)
            }
        }

    }
}