package com.example.wasmapplication.core.interfaces

interface IBaseServiceEventListener<T> {
    fun onRequestIsSuccess(response: T){}
    fun onRequestIsFailure(error: String)
    fun startListener() {}
    fun stopListener() {}
}