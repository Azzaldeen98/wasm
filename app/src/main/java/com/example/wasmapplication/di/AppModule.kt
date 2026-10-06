package com.example.wasmapplication.di

import android.content.Context
import android.util.Log
import com.example.wasmapplication.R
import com.example.wasmapplication.core.Notifications.LocalNotification
import com.example.wasmapplication.core.constant.AppBuildConfig
import com.example.wasmapplication.core.constant.Constants
import com.example.wasmapplication.core.features.wasmSpeech.data.remote.GeminiApiClient
import com.example.wasmapplication.core.features.wasmSpeech.data.remote.IWasmApiServices
import com.example.wasmapplication.core.features.wasmSpeech.data.remote.WasmApiRemote
import com.example.wasmapplication.core.features.wasmSpeech.data.remote.WasmApiRemoteImpl
import com.example.wasmapplication.core.features.wasmSpeech.data.repository.GeminiAiRepositoryImpl
import com.example.wasmapplication.core.features.wasmSpeech.data.repository.WasmTextToSpeechRepositoryImpl
import com.example.wasmapplication.core.features.wasmSpeech.domain.repository.GeminiAiRepository
import com.example.wasmapplication.core.features.wasmSpeech.domain.repository.WasmTextToSpeechRepository
import com.example.wasmapplication.core.local.LanguageControls
import com.google.ai.client.generativeai.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Named
import javax.inject.Singleton
import com.example.wasm.BuildConfig
//@Module
//@InstallIn(ServiceComponent::class)
//object ServiceModule {
//        @Provides
//        fun provideTestServiceInterface(): TestServiceInterfaceImpl {
//            return TestServiceInterfaceImpl()
//        }
//}


@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    @Named("firstName")
    fun provideFirstName(): String {
        return "Azzaldeen Mansour";
    }

    @Provides
    @Singleton
    fun provideWasmSpeechHttpURLConnection(@ApplicationContext context: Context): HttpURLConnection {
        val apiUrl="${Constants.WASM_BASE_URL}vits-ar-sa-huba-v2" //vits-ar-sa-A"
        val authorization = AppBuildConfig.WASM_API_KEY // "Bearer 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";
        Log.d("apiUrl",apiUrl)

        val url = URL(apiUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", authorization)
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        return  conn
    }

    @Provides
    @Singleton
    fun provideWasmSpeechApi(): IWasmApiServices {
        return Retrofit.Builder()
            .baseUrl(Constants.WASM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IWasmApiServices::class.java)
    }

    @Provides
    @Singleton
    fun provideGeminiApiClient(@ApplicationContext context: Context): GeminiApiClient {
//        private val apiKey: String = BuildConfig.GEMINI_API_KEY
        return GeminiApiClient(BuildConfig.GEMINI_API_KEY)
    }

    @Provides
    @Singleton
    fun provideGeminiApiRepository(api: GeminiApiClient)
    : GeminiAiRepository {
        return GeminiAiRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideLanguageControls(@ApplicationContext context: Context)
    : LanguageControls {
        return LanguageControls.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideWasmTextToSpeechRepository(api: WasmApiRemote): WasmTextToSpeechRepository {
        return WasmTextToSpeechRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideWasmApiRemote(client: IWasmApiServices, httpConn: HttpURLConnection): WasmApiRemote {
        return WasmApiRemoteImpl(client,httpConn)
    }

    @Provides
    @Singleton
    fun provideLocalNotification():LocalNotification {
        return  LocalNotification();
    }


//    @Provides
//    @Singleton
//    fun provideExoPlayerMedia(@ApplicationContext  context: Context): ExoPlayerMedia {
//        return ExoPlayerMedia(context)
//    }


}