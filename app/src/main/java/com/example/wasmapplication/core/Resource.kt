package com.example.wasmapplication.core

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class FinalError<T>(message: String) : Resource<T>(null,message)
    class Loading<T>(message: String="") : Resource<T>(null,message)
    class Complete<T>(data: T? = null) : Resource<T>(data)
}