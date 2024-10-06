package com.example.wasmapplication.core.error

public class OfflineException(message: String?) : Exception()

public class AiSafetyException(message: String?) : Exception()

public class ServerException(message: String?) : Exception()

public class EmptyCacheException(message: String?) : Exception()

public class InvalidEmailOrPasswordException : Exception()

public class OTPNotValidException : Exception()

public class SigOutException : Exception()

public class OldPasswordException : Exception()

public class CustomerExistsException : Exception()

public class AccountNotActiveException : Exception()

public class CustomerNotFoundException : Exception()
