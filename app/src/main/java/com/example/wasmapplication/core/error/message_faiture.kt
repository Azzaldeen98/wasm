package com.example.wasmapplication.core.error

import android.content.Context

import com.example.wasmapplication.core.local_storage.LanguageInfo
import dagger.hilt.android.qualifiers.ApplicationContext

fun choseFailureMessage(lang:String,arabicLabel:String,englishLabel:String):String {
  return  when(lang){
      in  "ar" -> return arabicLabel
      in  "en" -> return englishLabel
      else -> return arabicLabel
    }
}

fun mapFailureToMessage(failure:Failure,@ApplicationContext context: Context):String {
    var lang=LanguageInfo.getCurrentAppLanguage(context);
   return  when (failure) {
         is OfflineFailure -> choseFailureMessage(lang,
             FailureMsg.OFFLINE_FAILURE_MESSAGE_AR,
             FailureMsg.OFFLINE_FAILURE_MESSAGE_EN)
         is ServerFailure -> choseFailureMessage(lang,
             FailureMsg.SERVER_FAILURE_MESSAGE_AR,
             FailureMsg.SERVER_FAILURE_MESSAGE_EN)
         else -> "No Thing"
   }

}