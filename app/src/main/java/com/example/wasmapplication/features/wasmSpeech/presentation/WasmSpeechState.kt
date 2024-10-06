package  com.example.wasmapplication.features.wasmSpeech.presentation

data class WasmSpeechState(
    val isLoading: Boolean = false,
    val data: Any? = null,
    val error: String = ""
)
