package com.example.wasm.core.android_api.media

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.audiofx.*
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.*
import androidx.media3.common.Player.Listener
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.ByteArrayDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import com.example.wasmapplication.core.interfaces.ICustomPlayerListener


class ExoPlayerMedia (private val context: Context ,private val isStream: Boolean =true){

    private var isRelease: Boolean? = true;
    var player: ExoPlayer? =null;
    private var listener : Listener?=null;
    private lateinit var mediaItem:MediaItem;
    init {
        initial(isStream)
    }
    @SuppressLint("UnsafeOptInUsageError")
    private fun createMediaSourceFromByteArray(data: ByteArray, mediaItem: MediaItem): MediaSource {
        val byteArrayDataSource = ByteArrayDataSource(data)
        val dataSourceFactory = DataSource.Factory { byteArrayDataSource }
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)
        return mediaSource
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun initial(isStream:Boolean=false) {

        try {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .setUsage(C.USAGE_MEDIA)
                .build()

            if(context!=null){

                if(isStream){
                    player =  ExoPlayer.Builder(context)
                        .setMediaSourceFactory(DefaultMediaSourceFactory(context).setLiveTargetOffsetMs(5000)).build()
                    mediaItem = getStreamMediaItem()
                }else{
                    player = ExoPlayer.Builder(context).build()
                }
                player?.volume = 1.0f  // 1.0 هو الحد الأقصى لمستوى الصوت
                player?.setAudioAttributes(audioAttributes,true)
                addPlayerListener()
                isRelease=false
            } else{
                Log.e("ExoPlayerMedia","the ExoPlayer  initial is Wrong because the context is null")
            }
        }catch (e:Exception) {
            e.printStackTrace()
        }
    }

    private fun getStreamMediaItem(uri: Uri?=null):MediaItem{
        return  MediaItem.Builder()
            .setUri(uri?: Uri.EMPTY)
            .setLiveConfiguration(
                MediaItem.LiveConfiguration.Builder().setMaxPlaybackSpeed(1.02f).build()
            ).build()
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun playMediaByteStream(data: ByteArray) {
        if(isRelease==true)
            initial(isStream)
        try {

            if(mediaItem==null)
                mediaItem = getStreamMediaItem()

            var mediaSource = createMediaSourceFromByteArray(data, mediaItem)
            player?.let {
                it.setMediaSource(mediaSource)
            }

            if (listener == null) {
                listener = getNewPlayerListener()
                if (listener != null) {
                    player?.addListener(listener!!)
                }
            }

            startPlay()

        }catch (e:Exception){
            e.printStackTrace()
            Log.e("playMediaStream",e.message.toString())
        }
    }
    @UnstableApi
    fun setAttributes(audioAttributes:AudioAttributes?=null){
        var _audioAttributes:AudioAttributes?=null
        if(audioAttributes==null){
            _audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .setUsage(C.USAGE_MEDIA)
                .build()
        }else{
            _audioAttributes=audioAttributes
        }

        player?.setAudioAttributes(_audioAttributes, true)
    }
    @SuppressLint("UnsafeOptInUsageError", "Range")
    fun playMedia(url: String) {
        if(isRelease==true)
            initial(isStream)

        val mediaItem =if(!isStream)
            MediaItem.Builder()
                .setUri(url).setMimeType(MimeTypes.AUDIO_WAV).build()
        else if(url?.isNullOrEmpty()==false && mediaItem!=null)
            getStreamMediaItem(Uri.parse(url))
        else
            mediaItem

        if(mediaItem!=null){
            player?.setMediaItem(mediaItem)
            startPlay()
        }
    }
    private  fun startPlay(){
        player?.prepare()
        player?.playWhenReady = true
    }
    private fun addPlayerListener(){
        if(player==null)
            return

        if (listener != null) {
            player?.let { it -> it.removeListener(listener!!) }
        }
        listener = getNewPlayerListener()
        if (listener != null) {
            player?.addListener(listener!!)
        }
    }
    private fun getNewPlayerListener():Listener?{
//        val audioManager =context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        val audioSessionId = audioManager.generateAudioSessionId()

        return object : Listener {
            @SuppressLint("UnsafeOptInUsageError")
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

                Log.d("onPlayerStateChanged","$playbackState")
                if (playbackState == Player.STATE_READY && player?.audioSessionId != null) {
                    val audioSessionId = player?.audioSessionId!!
                    try {
                        if (NoiseSuppressor.isAvailable()) {
                            val noiseSuppressor = NoiseSuppressor.create(audioSessionId)
                            if (noiseSuppressor != null) {
                                noiseSuppressor.enabled = true
                                Log.d("AudioEffects", "NoiseSuppressor enabled")
                            } else {
                                Log.e("AudioEffects", "Failed to create NoiseSuppressor")
                            }
                        } else {
                            Log.e("AudioEffects", "NoiseSuppressor is not available on this device")
                        }
                    } catch (e: Exception) {
                        Log.e("AudioEffects", "Error creating NoiseSuppressor: ${e.message}")
                    }

                    try {
                        // إلغاء الصدى
                        if (AcousticEchoCanceler.isAvailable()) {
                            val echoCanceler = AcousticEchoCanceler.create(audioSessionId)
                            if (echoCanceler != null) {
                                echoCanceler.enabled = true
                                Log.d("AudioEffects", "AcousticEchoCanceler enabled")
                            } else {
                                Log.e("AudioEffects", "Failed to create AcousticEchoCanceler")
                            }
                        } else {
                            Log.e("AudioEffects", "AcousticEchoCanceler is not available on this device")
                        }
                    } catch (e: Exception) {
                        Log.e("AudioEffects", "Error creating NoiseSuppressor: ${e.message}")
                    }

                    try {
                        // تحسين الصوت باستخدام Bass Boost
                        val bassBoost = BassBoost(0, audioSessionId)
                        if (bassBoost != null) {
                            bassBoost.enabled = true
                            bassBoost.setStrength(1000.toShort())  // تعيين مستوى تعزيز الجهير
                            Log.d("AudioEffects", "BassBoost enabled")
                        } else {
                            Log.e("AudioEffects", "Failed to create BassBoost")
                        }
                    } catch (e: Exception) {
                        Log.e("AudioEffects", "Error creating bassBoost: ${e.message}")
                    }

                    try {
                        // تحسين الترددات باستخدام Equalizer
                        val equalizer = Equalizer(0, audioSessionId)
                        if (equalizer != null) {
                            equalizer.enabled = true
                            equalizer.setBandLevel(0, 1000)  // تحسين الجهير
                            Log.d("AudioEffects", "Equalizer enabled")
                        } else {
                            Log.e("AudioEffects", "Failed to create Equalizer")
                        }
                    } catch (e: Exception) {
                        Log.e("AudioEffects", "Error creating Equalizer: ${e.message}")
                    }
                    //LoudnessEnhancer
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        try {
                            val loudnessEnhancer = LoudnessEnhancer(audioSessionId)
                            loudnessEnhancer.setTargetGain(2000)
                            loudnessEnhancer.enabled = true
                            Log.d("AudioEffects", "LoudnessEnhancer enabled with target gain: ")
                        } catch (e: Exception) {
                            Log.e("AudioEffects", "Error creating LoudnessEnhancer: ${e.message}")
                        }
                    } else {
                        Log.w("AudioEffects", "LoudnessEnhancer is not supported on this device")
                    }

                }
            }

        }

    }
    fun setPlayerListener(callback: ICustomPlayerListener<ExoPlayer>?=null, isComplete:Boolean=false){

        try {
            if (listener != null) {
                player?.let { it ->
                    it.removeListener(listener!!)
                }
            }
        }finally {
            listener=object : Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        ExoPlayer.STATE_READY -> {
                            println("ExoPlayer.STATE_READY     -")
                        }Player.STATE_ENDED-> {
                        Log.d("onCompletionListener", "Playback completed")
                        player?.let {
                            callback?.onCompletionListener(it,isComplete)
                        }

                    }
                    }
                }
                override fun onPlayerError(error: PlaybackException) {
                    Log.e("ExoPlayer", "Error occurred: ${error.message}")
                    player?.let {
                        callback?.onErrorListener(it,error)
                    }
                }
                @SuppressLint("UnsafeOptInUsageError")
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

                    Log.d("onPlayerStateChanged","$playbackState")
                    if (playbackState == Player.STATE_READY && player?.audioSessionId != null) {
                        val audioSessionId = player?.audioSessionId!!
                        try {
                            if (NoiseSuppressor.isAvailable()) {
                                val noiseSuppressor = NoiseSuppressor.create(audioSessionId)
                                if (noiseSuppressor != null) {
                                    noiseSuppressor.enabled = true
                                    Log.d("AudioEffects", "NoiseSuppressor enabled")
                                } else {
                                    Log.e("AudioEffects", "Failed to create NoiseSuppressor")
                                }
                            } else {
                                Log.e("AudioEffects", "NoiseSuppressor is not available on this device")
                            }
                        } catch (e: Exception) {
                            Log.e("AudioEffects", "Error creating NoiseSuppressor: ${e.message}")
                        }

                        try {
                            // إلغاء الصدى
                            if (AcousticEchoCanceler.isAvailable()) {
                                val echoCanceler = AcousticEchoCanceler.create(audioSessionId)
                                if (echoCanceler != null) {
                                    echoCanceler.enabled = true
                                    Log.d("AudioEffects", "AcousticEchoCanceler enabled")
                                } else {
                                    Log.e("AudioEffects", "Failed to create AcousticEchoCanceler")
                                }
                            } else {
                                Log.e("AudioEffects", "AcousticEchoCanceler is not available on this device")
                            }
                        } catch (e: Exception) {
                            Log.e("AudioEffects", "Error creating NoiseSuppressor: ${e.message}")
                        }

                        try {
                            // تحسين الصوت باستخدام Bass Boost
                            val bassBoost = BassBoost(0, audioSessionId)
                            if (bassBoost != null) {
                                bassBoost.enabled = true
                                bassBoost.setStrength(1000.toShort())  // تعيين مستوى تعزيز الجهير
                                Log.d("AudioEffects", "BassBoost enabled")
                            } else {
                                Log.e("AudioEffects", "Failed to create BassBoost")
                            }
                        } catch (e: Exception) {
                            Log.e("AudioEffects", "Error creating bassBoost: ${e.message}")
                        }

                        try {
                            // تحسين الترددات باستخدام Equalizer
                            val equalizer = Equalizer(0, audioSessionId)
                            if (equalizer != null) {
                                equalizer.enabled = true
                                equalizer.setBandLevel(0, 1000)  // تحسين الجهير
                                Log.d("AudioEffects", "Equalizer enabled")
                            } else {
                                Log.e("AudioEffects", "Failed to create Equalizer")
                            }
                        } catch (e: Exception) {
                            Log.e("AudioEffects", "Error creating Equalizer: ${e.message}")
                        }
                        //LoudnessEnhancer
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            try {
                                val loudnessEnhancer = LoudnessEnhancer(audioSessionId)
                                loudnessEnhancer.setTargetGain(2000)
                                loudnessEnhancer.enabled = true
                                Log.d("AudioEffects", "LoudnessEnhancer enabled with target gain: ")
                            } catch (e: Exception) {
                                Log.e("AudioEffects", "Error creating LoudnessEnhancer: ${e.message}")
                            }
                        } else {
                            Log.w("AudioEffects", "LoudnessEnhancer is not supported on this device")
                        }

                    }
                }

            }

            player?.let { it -> it.addListener(listener!!) }


        }


    }
    fun applyEqualizer() {
        val audioManager =context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val audioSessionId = audioManager.generateAudioSessionId()
        val equalizer = Equalizer(0, audioSessionId)
        if (equalizer != null) {
            equalizer.enabled = true
            equalizer.setBandLevel(0, 1000)  // تحسين الجهير
            Log.d("AudioEffects", "Equalizer enabled")
        } else {
            Log.e("AudioEffects", "Failed to create Equalizer")
        }
    }
    fun release() {
        isRelease=true
        if(player!=null) {
            if(listener!=null)
                player?.let { it.removeListener(listener!!) }
            player!!.release()
            player=null
        }
    }
    fun isPlayer():Boolean {
        return player?.isPlaying?:false
    }
    fun stop() {
        try{
            isRelease=true
            if( player?.isPlaying==true){
                player?.stop()
            }
            if(listener!=null)
                player?.let { it.removeListener(listener!!) }

            player?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}

