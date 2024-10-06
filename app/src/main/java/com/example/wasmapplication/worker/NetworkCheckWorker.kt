package com.example.wasmapplication.worker

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters


class NetworkCheckWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {

        @Suppress("DEPRECATION")
        override fun doWork(): Result {

            val connectivityManager =
                applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo

            // التحقق من حالة الاتصال بالإنترنت
            if (networkInfo != null && networkInfo.isConnected) {
                // متصل بالإنترنت
                 Toast.makeText(this.context, "متصل بالإنترنت", Toast.LENGTH_SHORT).show()
    //            Log.d("NetworkCheckWorker", "Connected to the internet")
            } else {
                Toast.makeText(this.context, "غير متصل بالإنترنت", Toast.LENGTH_SHORT).show()
                // غير متصل بالإنترنت
    //            Log.d("NetworkCheckWorker", "Disconnected from the internet")
            }

            // إعادة Result.success() إذا كان العمل قد اكتمل بنجاح
            return Result.success()
        }
}
