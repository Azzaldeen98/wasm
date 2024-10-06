package com.example.wasmapplication.features.wasmSpeech.presentation


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import com.example.wasm.core.android_api.media.ExoPlayerMedia
import com.example.wasmapplication.core.Resource
import com.example.wasmapplication.core.constant.Constants
import com.example.wasmapplication.core.constant.FailureMsg
import com.example.wasmapplication.core.helpers.Helper
import com.example.wasmapplication.core.interfaces.ICustomPlayerListener
import com.example.wasmapplication.features.UiState
import com.example.wasmapplication.core.features.wasmSpeech.domain.use_case.GeminiTextWasmQueryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class WasmSpeechViewModel
@Inject constructor(
//    savedStateHandle: SavedStateHandle,
//    private val exoPlayer:ExoPlayerMedia,
    private val wasmSpeechUseCase: GeminiTextWasmQueryUseCase,
//    @ApplicationContext private val context: Context,
)
: ViewModel(){
//    private  val exoPlayer:ExoPlayerMedia = ExoPlayerMedia(context);
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    private val TAG:String="WasmSpeechViewModel";
//    private val _state = mutableStateOf(WasmSpeechState())
//    val state: State<WasmSpeechState> = _state
    init {
//    _uiState.value=UiState.Loading;
//    viewModelScope.launch {
//        delay(5000)
        _uiState.value=UiState.Success("Result Data");
//    }
//    Log.d(TAG,"Welcome  $firstName")
        sendInputText("السلام عليكم . كيف الحال ؟ هل انت بخير؟")
//        savedStateHandle.get<String>(Constants.PARAM_AUDIO_CLIP_DATA)?.let { text ->
//         عندما نرسل قيم الى هذا النموذج من صفحة اخرى هنا نلتقط القيم المرسلة
//        }
    }
    private fun sendInputText(inputText: String) {
        viewModelScope.launch(Dispatchers.IO) {
//            wasmSpeechUseCase(inputText).collect { result ->
//                when (result) {
//                    is Resource.Loading -> {
//                        _uiState.value = UiState.Loading
//                    }
//                    is Resource.Success -> {
//                        if(result?.data!=null && result?.data is ByteArray){
//                            playAudio(result?.data)
//                        }else if(result?.data!=null && result?.data is String){
//                            _uiState.value = UiState.Success(result?.data)
//                        } else
//                            _uiState.value = UiState.Error(FailureMsg.UNEXPECTED_FAILURE_MESSAGE_EN)
//                    } is Resource.Error -> {
//                        _uiState.value = UiState.Error(if(result.message?.isNullOrEmpty()==false) result.message
//                        else FailureMsg.UNEXPECTED_FAILURE_MESSAGE_EN)
//                    }
//
//                }
//            }
        }


        //        viewModelScope.launch {
//            wasmSpeechUseCase(inputText).collect { result ->
//                when (result) {
//                    is Resource.Success -> {
//                    }
//                    is Resource.Error -> {
//                        _state.value = WasmSpeechState(
//                            error = if(result.message?.isNullOrEmpty()==false) result.message
//                            else FailureMsg.UNEXPECTED_FAILURE_MESSAGE_EN
//                        )
//                    }
//                    is Resource.Loading -> {
//                        _state.value = WasmSpeechState(isLoading = true)
//                    }
//                }
//            }
//        }

    }

private suspend fun  playAudio(data:ByteArray){
//            var file= Helper.createTempFileAudio(Constants.TEMP_FILE_AUDIO_NAME,data, context)
//            if(file!=null && file?.canRead()==true && file?.absoluteFile!=null){
////                    withContext(Dispatchers.Main) {
////                        exoPlayer?.playMedia(file?.absolutePath ?: "", false, object :
////                            ICustomPlayerListener<ExoPlayer> {
////                            override fun onErrorListener(mp: ExoPlayer?, error: Exception) {
////                                super.onErrorListener(mp, error)
////                                _uiState.value = UiState.Success("ExoPlayer-Error")
////                            }
////
////                            override fun onCompletionListener(mp: ExoPlayer?) {
////                                super.onCompletionListener(mp)
////                                _uiState.value = UiState.Success("ExoPlayer-Completion")
////                            }
////                        })
////                    }
//                }else{
//                  _uiState.value = UiState.Error(FailureMsg.EMPTY_CACHE_FAILURE_MESSAGE_AR)
//            }
    }
}