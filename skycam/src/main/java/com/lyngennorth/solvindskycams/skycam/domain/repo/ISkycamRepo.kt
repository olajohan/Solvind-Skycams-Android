package com.lyngennorth.solvindskycams.skycam.domain.repo

import com.lyngennorth.solvindskycams.common.Either
import com.lyngennorth.solvindskycams.common.Failure
import com.lyngennorth.solvindskycams.skycam.domain.model.Skycam
import kotlinx.coroutines.flow.Flow

interface ISkycamRepo {
    suspend fun getAllSkycams(): Either<Failure, List<Skycam>>
    suspend fun getSkycam(skycamKey: String): Either<Failure, Skycam>
    suspend fun getSkycamFlow(skycamKey: String): Either<Failure, Flow<Skycam>>
}