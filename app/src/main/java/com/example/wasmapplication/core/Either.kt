package com.example.wasmapplication.core

import android.annotation.SuppressLint
import android.util.Log
import com.example.wasmapplication.core.error.AiSafetyException
import com.example.wasmapplication.core.error.AiSafetyFailure
import com.example.wasmapplication.core.error.AuthorizeException
import com.example.wasmapplication.core.error.AuthorizeFailure
import com.example.wasmapplication.core.error.ConnectErrorException
import com.example.wasmapplication.core.error.ConnectErrorFailure
import com.example.wasmapplication.core.error.CustomException
import com.example.wasmapplication.core.error.ErrorFailure
import com.example.wasmapplication.core.error.Failure
import com.example.wasmapplication.core.error.NullException
import com.example.wasmapplication.core.error.NullFailure
import com.example.wasmapplication.core.error.OfflineFailure
import com.example.wasmapplication.core.error.ServerException
import com.example.wasmapplication.core.error.ServerFailure
import com.example.wasmapplication.core.interfaces.ICallbackTask
import retrofit2.HttpException
import java.net.ConnectException
import java.net.HttpURLConnection

sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()

    fun isLeft(): Boolean = this is Left<L>
    fun isRight(): Boolean = this is Right<R>

    fun <L> leftOrNull(): L? = (this as? Left<L>)?.value
    fun <R> rightOrNull(): R? = (this as? Right<R>)?.value
}


suspend fun <T> safeExecuteCallbackTask(callBack: ICallbackTask): T? {
    try{

       return callBack?.executed() as? T;
        HttpURLConnection.HTTP_GONE
    }catch (e: ServerException) {
        var code=e.code;
            when(code){
                HttpURLConnection.HTTP_PROXY_AUTH -> throw AuthorizeException();
            }
    }catch (e: HttpException) {
        throw  ServerException(e.code(),e.message())
    } catch (e: KotlinNullPointerException) {
        throw  NullException(e.message)
    }catch (e: ConnectException) {
        throw  ConnectErrorException(e.message)
    } catch (e: Exception) {
        if (e.message?.contains("SAFETY") == true) {
            throw AiSafetyException(e.message)
        }
        throw CustomException(e.message)
    }
    return  null
}

fun processValue(value: String): Either<Failure, String> {
    return if (value?.isNotBlank()==true) {
        Either.Right(value)
    } else {
        Either.Left(ServerFailure())
    }
}
@SuppressLint("SuspiciousIndentation")
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