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
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import com.example.wasmapplication.core.interfaces.ICustomPlayerListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


class ExoPlayerMedia (private val context: Context ,private val isStream: Boolean =true){

    private var isRelease: Boolean? = true;
    var player: ExoPlayer? =null;
    private var listener : Listener?=null;
    private lateinit var mediaItem:MediaItem;

    private var noiseSuppressor: NoiseSuppressor? = null
    private var bassBoost: BassBoost? = null
    private var equalizer: Equalizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var acousticEchoCanceler:AcousticEchoCanceler? = null

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
                val defaultRenderersFactory = DefaultRenderersFactory(context).setEnableAudioTrackPlaybackParams(true)

                if(isStream){
                    player =  ExoPlayer.Builder(context,defaultRenderersFactory)
                        .setMediaSourceFactory(DefaultMediaSourceFactory(context).setLiveTargetOffsetMs(5000)).build()
                    mediaItem = getStreamMediaItem()
                }else{
                    player = ExoPlayer.Builder(context,defaultRenderersFactory).build()
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
    fun getRemainingDuration():Long{
        val totalDuration = player?.duration ?: 0
        val currentPosition = player?.currentPosition ?: 0
        return (totalDuration - currentPosition)
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

//            if (listener == null) {
//                listener = getNewPlayerListener()
//                if (listener != null) {
//                    player?.addListener(listener!!)
//                }
//            }

            player?.prepare()
            player?.play()

        }catch (e:Exception){
            e.printStackTrace()
            Log.e("playMediaStream",e.message.toString())
        }
    }
    private suspend fun waitUntilAudioEnds() {
        if (listener != null) {
            player?.let { it.removeListener(listener!!) }
            listener=null;
        }
        suspendCancellableCoroutine<Unit> { continuation ->
            if(listener==null){
                listener=object : Player.Listener {
                    @SuppressLint("UnsafeOptInUsageError", "SuspiciousIndentation")
                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

                        if (playbackState == Player.STATE_READY && player?.audioSessionId != null) {
                            val audioSessionId = player?.audioSessionId!!
                                initializeAudioEffects(audioSessionId)
                            } else {
                                Log.w("AudioEffects", "LoudnessEnhancer is not supported on this device")
                            }

                        }
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        // تحقق مما إذا كانت حالة التشغيل تعني الانتهاء
                        if (playbackState == Player.STATE_ENDED) {
                            continuation.resume(Unit)
                        }
                    }
                    override fun onPlayerError(error: PlaybackException) {
                        // إذا حدث خطأ أثناء التشغيل
                        continuation.resume(Unit) // يمكنك أيضًا إدارة الأخطاء بشكل أفضل هنا
                    }
                }
            }
            player?.addListener(listener!!)
        }
    }
    private fun initializeAudioEffects(audioSessionId: Int) {
        if (noiseSuppressor == null && bassBoost == null
            && equalizer == null && loudnessEnhancer == null && acousticEchoCanceler==null) {
            try {
                // إنشاء التأثيرات الصوتية مرة واحدة
                if (NoiseSuppressor.isAvailable()) {
                    noiseSuppressor = NoiseSuppressor.create(audioSessionId)
                    noiseSuppressor?.enabled = true
                    Log.d("AudioEffects", "NoiseSuppressor created and enabled")
                }
                if (AcousticEchoCanceler.isAvailable()) {
                    acousticEchoCanceler = AcousticEchoCanceler.create(audioSessionId)
                    if (acousticEchoCanceler != null) {
                        acousticEchoCanceler?.enabled = true
                    }
                }

                bassBoost = BassBoost(0, audioSessionId)
                if(bassBoost!=null){
                    bassBoost?.enabled = true
                    bassBoost?.setStrength(1000.toShort())
                    Log.d("AudioEffects", "BassBoost created and enabled")
                }

                equalizer = Equalizer(0, audioSessionId)
                if(equalizer!=null){
                    equalizer?.enabled = true
                    equalizer?.setBandLevel(0, 1000)  // تحسين الجهير
                    Log.d("AudioEffects", "Equalizer created and enabled")

                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    loudnessEnhancer = LoudnessEnhancer(audioSessionId)
                    loudnessEnhancer?.setTargetGain(2000)
                    loudnessEnhancer?.enabled = true
                    Log.d("AudioEffects", "LoudnessEnhancer created and enabled")
                }
            } catch (e: Exception) {
                Log.e("AudioEffects", "Error setting up audio effects: ${e.message}")
            }
        } else {
            Log.d("AudioEffects", "Audio effects are already set up")
        }
    }
    suspend fun playerMediaStreamAndWait(data: ByteArray) {
        playMediaByteStream(data);
        waitUntilAudioEnds();
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
        player?.play()
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
                        if (noiseSuppressor==null && NoiseSuppressor.isAvailable()) {
                             noiseSuppressor = NoiseSuppressor.create(audioSessionId)
                            if (noiseSuppressor != null) {
                                noiseSuppressor?.enabled = true
                                Log.d("AudioEffects", "NoiseSuppressor enabled")
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
                            acousticEchoCanceler = AcousticEchoCanceler.create(audioSessionId)
                            if (acousticEchoCanceler != null) {
                                acousticEchoCanceler?.enabled = true
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
        try {
            releaseAudioEffects();
        }finally {
            if(player!=null) {
                stop()
                if(listener!=null)
                    player?.let { it.removeListener(listener!!) }
                player!!.release()
                player=null
            }
        }
    }
    fun isPlayer():Boolean {
        if(player==null) return false
        return player?.isPlaying?:false
    }
    fun stop() {
        try{
            isRelease=true
            if( player?.isPlaying==true){
                player?.stop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    private fun releaseAudioEffects() {
        // تأكد من وجود تأثيرات الصوت مفعلّة
        if (noiseSuppressor != null) {
            noiseSuppressor?.enabled = false
            noiseSuppressor?.release() // تحرير الذاكرة
            noiseSuppressor = null
        }

        if (acousticEchoCanceler != null) {
            acousticEchoCanceler?.enabled = false
            acousticEchoCanceler?.release() // تحرير الذاكرة
            acousticEchoCanceler = null
        }

        if (bassBoost != null) {
            bassBoost?.enabled = false
            bassBoost?.release() // تحرير الذاكرة
            bassBoost = null
        }

        if (equalizer != null) {
            equalizer?.enabled = false
            equalizer?.release() // تحرير الذاكرة
            equalizer = null
        }

        if (loudnessEnhancer != null) {
            loudnessEnhancer?.enabled = false
            loudnessEnhancer?.release() // تحرير الذاكرة
            loudnessEnhancer = null
        }
    }


}

