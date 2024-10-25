package com.example.wasmapplication.core.interfaces

interface IListenerStream<T> {
    fun onStreamReader(response:T?=null,lastFlow:Boolean=false){}
    fun onStreamReader(response:T?=null,flowIndex:Int=0,lastFlowIndex:Int=0){}
    suspend  fun onStreamReader(response:T?=null){}
    fun onStreamComplete(lastFlowIndex:Int=0){}
    suspend fun onStreamComplete(){}
    fun onStreamError(message:Throwable){}
    suspend fun onStreamError2(message:Throwable){}
}