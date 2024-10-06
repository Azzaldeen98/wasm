package com.example.wasmapplication.core.features.wasmSpeech.data.remote

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface IWasmApiServices {

    @POST("vits-ar-sa-huba-v2")
    fun queryHubaV2(@Body input: String): Call<ResponseBody?>?
}