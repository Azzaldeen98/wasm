package com.example.wasmapplication.core

import android.util.Log
import com.example.wasmapplication.core.error.Failure
import com.example.wasmapplication.core.error.OfflineFailure
import com.example.wasmapplication.core.error.ServerFailure

sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()

    fun isLeft(): Boolean = this is Left<L>
    fun isRight(): Boolean = this is Right<R>

    fun <L> leftOrNull(): L? = (this as? Left<L>)?.value
    fun <R> rightOrNull(): R? = (this as? Right<R>)?.value
}


fun processValue(value: String): Either<Failure, String> {
    return if (value?.isNotBlank()==true) {
        Either.Right(value)
    } else {
        Either.Left(ServerFailure())
    }
}
fun exampleRun(){
  var  result=processValue("Azzaldeen")
    when (result!!) {
        is Either.Left -> {
            when(result.leftOrNull() as Failure?){
                is OfflineFailure -> Log.e("Failure","OfflineFailure")
                is ServerFailure-> Log.e("Failure","ServerFailure")
                else -> Log.e("Failure","Null Reference")
            }
        }
        is Either.Right -> {
            var value=result.rightOrNull() as String?
            Log.d("Right",value?:"-")
        }
    }
}