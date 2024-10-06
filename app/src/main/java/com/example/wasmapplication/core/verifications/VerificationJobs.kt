

package com.example.wasmapplication.core.verifications

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build


class VerificationJobs {

    companion object {
        fun isConnectedToInternet(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cm.run {
                    cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                        return when {
                            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                            else -> false
                        }
                    }
                }
            }
            else {
                cm.run {
                    @Suppress("DEPRECATION")
                    cm.activeNetworkInfo?.run {
                        if (type == ConnectivityManager.TYPE_WIFI) {
                            return true
                        } else if (type == ConnectivityManager.TYPE_MOBILE) {
                            return true
                        }
                    }
                }
            }

            return false

        }

    }
}