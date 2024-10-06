package com.example.wasmapplication.core.interfaces

interface ICustomPlayerListener<T> {
    fun onErrorListener(mp: T?){}
    fun onErrorListener(mp: T?, error:java.lang.Exception){}
    fun onCompletionListener(mp: T?){}
    fun onCompletionListener(mp: T?, isComplete:Boolean=false){}
}