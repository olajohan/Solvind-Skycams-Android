package com.solvindskycams.skycam.domain.repo

import com.solvindskycams.common.Either
import com.solvindskycams.common.Failure
import com.solvindskycams.skycam.domain.model.Skycam
import kotlinx.coroutines.flow.Flow

interface ISkycamRepo {
    suspend fun getAllSkycams(): Either<Failure, List<Skycam>>
    suspend fun getSkycam(skycamKey: String): Either<Failure, Skycam>
    fun getSkycamFlow(skycamKey: String): Flow<Skycam>
}