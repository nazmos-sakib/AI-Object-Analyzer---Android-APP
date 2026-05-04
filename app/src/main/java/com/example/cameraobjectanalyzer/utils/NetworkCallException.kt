package com.example.cameraobjectanalyzer.utils

class NetworkCallException (
    message: String,
    method: String?,
    cause: Throwable? = null
) : Exception(message, cause)