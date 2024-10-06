package com.example.wasmapplication.core.error

interface Failure {}

class OfflineFailure : Failure {}
class ServerFailure : Failure {}