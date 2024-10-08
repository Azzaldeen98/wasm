package com.example.wasmapplication.core.error

public class OfflineException(message: String) : Exception(message)

public class AiSafetyException(message: String?) : Exception(message)
public class NullException(message: String?) : Exception(message)
public class ConnectErrorException(message: String?) : Exception(message)
public class ServerException(public final val code:Int,message: String?) : Exception(message)
public class CustomException(message: String?) : Exception(message)
public class AuthorizeException(message: String?="") : Exception(message)
public class EmptyCacheException(message: String?) : Exception(message)

public class InvalidEmailOrPasswordException : Exception()

public class OTPNotValidException : Exception()

public class SigOutException : Exception()

public class OldPasswordException : Exception()

public class CustomerExistsException : Exception()

public class AccountNotActiveException : Exception()

public class CustomerNotFoundException : Exception()
