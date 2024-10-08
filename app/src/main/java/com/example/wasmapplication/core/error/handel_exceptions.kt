package com.example.wasmapplication.core.error

import com.example.wasmapplication.core.Either
import com.example.wasmapplication.core.interfaces.ICallbackTask
import retrofit2.HttpException
import java.net.ConnectException
import java.net.HttpURLConnection

suspend fun <T> safeExecuteWithHandelException(callBack: ICallbackTask): Either<Failure, T?> {
//    var code=e.code;
//    when(code){
//        HttpURLConnection.HTTP_PROXY_AUTH -> return Either.Left(AuthorizeFailure());
//    }
    try {

        return Either.Right(callBack?.executed() as? T);

    }catch (e: HttpException) {
        return Either.Left(ServerFailure())
    } catch (e: KotlinNullPointerException) {
        return Either.Left(NullFailure())
    }catch (e: ConnectException) {
        return  Either.Left(ConnectErrorFailure());
    } catch (e: Exception) {
        if (e.message?.contains("SAFETY") == true) {
            return  Either.Left(AiSafetyFailure());
        }
        return  Either.Left(ErrorFailure());
    }
}
