package com.example.wasmapplication

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.wasmapplication.core.constant.STORAGE_NETWORK_CHECK_WORKER
import com.example.wasmapplication.core.local_storage.ExternalStorage
import com.example.wasmapplication.worker.NetworkCheckWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit


@HiltAndroidApp
class WasmApplication : Application(){

    override fun onCreate() {
        super.onCreate()

//        // التحقق مما إذا كانت الجدولة قد تمت مسبقًا
//        val isWorkScheduledInternet= ExternalStorage.getBooleanValue(this,STORAGE_NETWORK_CHECK_WORKER)
//        if (!isWorkScheduledInternet) {
//            // إذا لم يتم الجدولة من قبل، قم بجدولة العمل
//            scheduleNetworkCheckWork(this)
//            ExternalStorage.storage(this,STORAGE_NETWORK_CHECK_WORKER,true)
//        }
    }

    @SuppressLint("InvalidPeriodicWorkRequestInterval")
    private fun scheduleNetworkCheckWork(context: Context) {
        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // الشرط هو الاتصال بالإنترنت
            .build()

        val networkCheckWork: PeriodicWorkRequest =  PeriodicWorkRequest.Builder(
            NetworkCheckWorker::class.java, 10, TimeUnit.SECONDS
        ) // التحقق كل 15 دقيقة
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "NetworkCheckWork", ExistingPeriodicWorkPolicy.UPDATE, networkCheckWork
        )
    }
}