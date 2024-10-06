package com.example.wasmapplication.core.interfaces



interface IWasmServiceEventListener: IBaseServiceEventListener<String> {
    fun onRequestIsSuccess2(callBack:IBaseCallbackListener<Any?>?){}

}