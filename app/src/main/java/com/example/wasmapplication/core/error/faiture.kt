package com.example.wasmapplication.core.error

interface Failure {}

class OfflineFailure : Failure
class ServerFailure : Failure
class OTPNotValidFailure : Failure
class SigOutFailure : Failure
class AccountNotActiveFailure : Failure
class CustomerExistsFailure : Failure
class AuthorizeFailure : Failure
class ConnectErrorFailure : Failure
class InvalidEmailOrPasswordFailure : Failure
class AiSafetyFailure : Failure
class  NullFailure : Failure
class  WrongDataFailure : Failure
class  EmptyCacheFailure : Failure
class  ErrorFailure : Failure
class  FirebaseFailure : Failure
class  AuthenticationFailure : Failure