package com.solvind.skycams.app.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Abstract class for a Use Case (Interactor in terms of Clean Architecture).
 * This abstraction represents an execution unit for different use cases (this means than any use
 * case in the application should implement this contract).
 *
 * By convention each [UseCase] implementation will execute its job in a background thread
 * (kotlin coroutine) and will post the result in the UI thread.
 */
abstract class UseCase<Type, in Params>(
    private val ioDispatcher: CoroutineDispatcher,
    private val mainDispatcher: CoroutineDispatcher
) where Type : Any {

    abstract suspend fun run(params: Params): Resource<Type>

    operator fun invoke(
        scope: CoroutineScope,
        params: Params,
        onResult: (Resource<Type>) -> Unit = {}
    ) {
        val job = scope.async(ioDispatcher) { run(params) }
        scope.launch(mainDispatcher) { onResult(job.await()) }
    }

    class None
}