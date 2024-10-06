package com.example.wasmapplication.core.constant


    object Constants {
        const val WASM_BASE_URL = "https://api-inference.huggingface.co/models/wasmdashai/"

        //=========================================================
        const val PARAM_AUDIO_CLIP_DATA = "Audio_clip_data"
        const val INPUT_TEXT = "Input_text"
        //=========================================================
        const val REQUEST_MICROPHONE_PERMISSION_CODE = 112
        const val CURRENT_CHAT_TOKEN = "CurrentSessionChatToken"
        const val APP_NAME = "CurrentSessionChatToken"
        const val CURRENT_SESSION_TOKEN = "CurrentSessionToken"
        const val LANGUAGE = "Lang"
        const val SPEECHMODELS = "SpeechModels"
        const val SPEECHMODELSELECTED = "SpeechModelSelected"
        const val SPEECHMODELSELECTEDINDEX = "SpeechModelSelectedIndex"
        const val LANGUAGE_INDEX = "LangIndex"
        const val LANGUAGE_VOICE_GENDER = "langVoiceGender"
        const val CHATS_LIST_STORAGE = "ListChats"
        const val API_TEMP_STORAGE = "TempStorage"
        const val PLAYER_ROBOT_AUDIO = "PlayerAudio"
        const val ROBOT_CHAT_SETTINGS = "RobotChatSettings"
        const val MODEL_GENDER_TYPE = "ModelGenderType"
        const val CURRENT_MODEL_INFO = "CurrentModelInfo"
        const val DEFAULT_GENDER = "Male"
        const val TIME = "Time"
        const val LAST_ITEM = "Last"
        const val END_SYMBOL = "###"
        const val TEMP_FILE_AUDIO_NAME = "TempFileAudioNAme"
        const val RESPONSE_AUDIO_PATH = "\${externalCacheDir?.absolutePath}/response_audio.3gp"
    }
