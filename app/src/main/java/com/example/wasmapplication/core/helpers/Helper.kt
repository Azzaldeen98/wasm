package com.example.wasmapplication.core.helpers

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.wasmapplication.core.constant.STORAGE_RECORD_SERVICE_STATE
import com.example.wasmapplication.core.local_storage.ExternalStorage
import java.io.File
import java.io.FileOutputStream


class Helper {
    companion object {

        fun createTempFileAudio(filePth: String, audioBytes: ByteArray, context: Context): File? {
            return try {
                var tempFile = File.createTempFile(filePth, ".wav", context.cacheDir)
                tempFile.deleteOnExit();
                val fos = FileOutputStream(tempFile)
                fos.write(audioBytes)
                fos.close()
                tempFile
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

        }
    }

}