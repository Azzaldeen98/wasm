package com.example.wasmapplication.core.android_api.speech_recognizer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import com.example.wasmapplication.core.local_storage.LanguageInfo
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

interface OnErrorListener {

    fun onError(errorId: Int): Boolean
}
interface ISpeechRecognizerService{
    fun initialization(recognizer: Boolean=true, workingInTheContinuously: Boolean=false, lang:String?="ar",
                       errorListener: OnErrorListener?=null):SpeechRecognizer?
    fun speechRecognizerListenAgain()
    fun startSpeechRecognizerListening()
    fun stopSpeechRecognizer()
    fun destroy()

}

 class SpeechRecognizerService( private val context: Context,
                                private val speechListenerCallback: ISpeechRecognizerCallBack,
                                private var recognizer: Boolean=true,
                                private var workingInTheContinuously: Boolean=true,
                                private var lang:String?="ar",
                                private var errorListener: OnErrorListener?=null)
// @Inject constructor(
//    private val context: Context,
//    private val speechListenerCallback: ISpeechRecognizerCallBack,
//    private var recognizer: Boolean=true,
//    private var workingInTheContinuously: Boolean=false,
//    private var lang:String?="ar",
//    private var errorListener: OnErrorListener?=null
//)
{

//     @Inject lateinit var context: Context;
//     @Inject lateinit var speechListenerCallback: ISpeechRecognizerCallBack;
//    private var recognizer: Boolean=true;
//    private var workingInTheContinuously: Boolean=false;
//    private  var lang:String?="ar";
//    private  var errorListener: OnErrorListener?=null;


    private var speechRecognizerIsListening: Boolean? = false
    private var shareWithApiGenerator:Boolean=true
    private lateinit var textSpeachResult: String
    private var speechRecognizerIntent: Intent?=null
    private var speechRecognizer: SpeechRecognizer?=null

    init {

//        initialization(recognizer,workingInTheContinuously,lang,errorListener)
    }

    @SuppressLint("LongLogTag")
     fun  initialization(recognizer: Boolean=true,
                        workingInTheContinuously:  Boolean=true,
                        lang:String?="ar",
                        errorListener: OnErrorListener?=null
    ) {
        setOptions(lang,recognizer,false,workingInTheContinuously)
        try {
            speechRecognizerIsListening=false;
            if(errorListener!=null)
                this.errorListener=errorListener

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer?.setRecognitionListener(onRecognitionListener)
            speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//            speechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true);
            // Define the language model used for voice recognition
            speechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            speechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_PROMPT, "")
            // Specify the preferred language for voice recognition
            speechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);



        } catch (e: Exception) {
            Log.e("SpeechRecognizerServiceError",e.message.toString())
            Toast.makeText(context, "SpeechRecognizer:" + e.message.toString(), Toast.LENGTH_SHORT)
                .show()
        }


        // Specifies how complete silence is required for audio input to be considered complete
//        speechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000);
        // Specifies the minimum amount of silence required to be considered audio input
//        speechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000);
        // The amount of time that it should take after we stop hearing speech to consider the input possibly complete.
//        speechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
        speechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    fun  resetInitialize(recognizer: Boolean=true,
                         workingInTheContinuously: Boolean=false,
                         lang:String?="ar",
                         errorListener: OnErrorListener?=null){
        stopSpeechRecognizer()
        this.recognizer=recognizer;
        this.workingInTheContinuously=workingInTheContinuously;
        this.lang=lang;
        this.errorListener=errorListener;
        initialization(recognizer,workingInTheContinuously,lang,errorListener)
    }

    private  val onRecognitionListener=object : RecognitionListener {
        override fun onReadyForSpeech(bundle: Bundle) {

//            Toast.makeText(this@SpeechRecognizerService.context, "onReadyForSpeech:", Toast.LENGTH_SHORT).show()
            Log.d("onReadyForSpeech", "Ready Speech")

        }
        override fun onBeginningOfSpeech() {
//            Toast.makeText(this@SpeechRecognizerService.context, "onBeginningOfSpeech:", Toast.LENGTH_SHORT).show()
            Log.d("BeginningOfSpeech", "Start Speech")

        }
        override fun onRmsChanged(v: Float) {
//            Log.d("onRmsChanged", v.toString())
        }
        override fun onBufferReceived(bytes: ByteArray) {
        }
        override fun onEndOfSpeech() {
            Log.d("EndOfSpeech", "End Speech")
        }
        override fun onError(i: Int) {

            Log.e( "onError:", "$i")
            if( errorListener!=null) errorListener?.onError(i)
            if(workingInTheContinuously)
                speechRecognizerListenAgain()

//                    mOnErrorListener?.onError(i)

//                    when(i) {
//                        //1
//                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
////                            mOnErrorListener?.onError(i)
//                        }
//                        //2
//                        SpeechRecognizer.ERROR_NETWORK -> {
//                            Log.d("ERROR_NETWORK", i.toString() + "")
//                        }
//                        //3
//                        SpeechRecognizer.ERROR_AUDIO -> {
//                            if(context!=null)
//                                Toast.makeText(context , "Audio recording error", Toast.LENGTH_SHORT).show()
//                        }
//                        //4
//                        SpeechRecognizer.ERROR_SERVER -> {}
//                        //5
//                        SpeechRecognizer.ERROR_CLIENT -> {}
//                        //6
//                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {}
//                        //7
//                        SpeechRecognizer.ERROR_NO_MATCH -> {}
//                        //8
//                        SpeechRecognizer. ERROR_RECOGNIZER_BUSY -> {
//                            Log.d("ERROR_RECOGNIZER_BUSY", i.toString() + "")
//                        }
//                        //9
//                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {}
//                    }




        }
        override fun onResults(bundle: Bundle) {
            try {
                speechRecognizerIsListening=false;
                val data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (data == null || data.size < 1 || data[0] =="")
                    reStartListening();
                else {
//                    Toast.makeText(this@SpeechRecognizerService.context,"onResults1", Toast.LENGTH_SHORT).show()
                    textSpeachResult = data[0].toString()

                    Toast.makeText(this@SpeechRecognizerService.context,"${textSpeachResult?:"null"}", Toast.LENGTH_SHORT).show()
//                    Log.d("onResults", textSpeachResult);
//                    Toast.makeText(this@SpeechRecognizerService.context, textSpeachResult, Toast.LENGTH_SHORT).show()

                    speechListenerCallback?.onSpeechRecognizerResult(textSpeachResult);
                }

            }catch (ex:Exception){
                Toast.makeText(this@SpeechRecognizerService.context, "$textSpeachResult", Toast.LENGTH_SHORT).show()
                reStartListening();
            }

        }
        override fun onPartialResults(bundle: Bundle) {}
        override fun onEvent(i: Int, bundle: Bundle) {
            Log.d("onEvent", i.toString() + "")
        }
    }
    private fun setOptions(lang:String?, _recognizer: Boolean, _shareWithApiGenerator: Boolean, _workingInTheContinuously: Boolean,){

       this.lang=lang;
        recognizer=_recognizer
        shareWithApiGenerator=_shareWithApiGenerator
        workingInTheContinuously=_workingInTheContinuously
    }
      fun speechRecognizerListenAgain() {

        try{
            if(speechRecognizer!=null){
                speechRecognizerIsListening=false
                speechRecognizer?.cancel();
            }

        } finally {
            startSpeechRecognizerListening();
        }
    }
    private fun startSpeechRecognizerListening() {

        if (speechRecognizer != null && speechRecognizerIntent != null){
            val lang= LanguageInfo("ar",0) // LanguageInfo.getStorageSelcetedLanguage(context)
//            if(lang!=null && speechRecognizerIntent?.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE)?.lowercase()!=lang.code?.lowercase())
            if(lang!=null && speechRecognizerIntent?.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE)?.equals(lang?.code) == false)
                speechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang.code);
            speechRecognizer ?.startListening(speechRecognizerIntent !!)
            speechRecognizerIsListening=true
        }
    }

    fun reStartListening() {

        if (speechRecognizer != null && speechRecognizerIntent != null && speechRecognizerIsListening==false ){
            speechRecognizer ?.startListening(speechRecognizerIntent !!)
            speechRecognizerIsListening=true
        }
    }
    fun setOnErrorListener(listener: OnErrorListener){
        errorListener=listener
    }
      fun stopSpeechRecognizer(){
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.d("Error ! ", e.message.toString())
        }
    }
     fun destroy(){
        try {

            if (speechRecognizer != null) {
                speechRecognizer?.stopListening();
                speechRecognizer?.cancel();
                speechRecognizer?.destroy();
                speechRecognizer=null;
            }

        } catch (e: Exception) {
            Log.d("Error ! ", e.message.toString())
        }finally {
            speechRecognizerIsListening=false
            speechRecognizerIntent=null;
        }
    }



}

