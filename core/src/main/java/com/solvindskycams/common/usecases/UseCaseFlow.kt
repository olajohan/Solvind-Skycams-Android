package com.solvindskycams.common.usecases

import kotlinx.coroutines.flow.Flow

/**
 * Returns a cold stream flow
 */
abstract class UseCaseFlow<out Type, in Params> where Type : Any {

    abstract fun run(params: Params): Flow<Type>

    operator fun invoke(params: Params) : Flow<Type> {
        return run(params)
    }

    class None
}