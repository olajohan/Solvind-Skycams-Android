package com.solvind.skycams.skycam

sealed class Result<T> {

    data class Success<T>(val value: T) : Result<T>()
    data class Error<T>(val failure: Failure) : Result<T>()

}