package com.solvind.skycams.app.core

abstract class UseCaseFlow<Type, in Params>() where Type : Any {
    abstract fun run(params: Params): Type
    class None
}