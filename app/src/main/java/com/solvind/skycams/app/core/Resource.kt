package com.solvind.skycams.app.core

sealed class Resource<T> {

    data class Success<T>(val value: T) : Resource<T>()
    data class Error<T>(val failure: Failure) : Resource<T>()

}