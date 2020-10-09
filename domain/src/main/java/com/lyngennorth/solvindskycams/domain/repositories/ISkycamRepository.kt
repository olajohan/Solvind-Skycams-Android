package com.lyngennorth.solvindskycams.domain.repositories

import com.lyngennorth.solvindskycams.domain.Either
import com.lyngennorth.solvindskycams.domain.Failure
import com.lyngennorth.solvindskycams.domain.domainmodel.Skycam
import kotlinx.coroutines.flow.Flow
import java.lang.Exception

interface ISkycamRepository {
    suspend fun getAllSkycams(): Either<Failure, List<Skycam>>
    suspend fun getSkycam(skycamKey: String): Either<Failure, Skycam>
    suspend fun getSkycamFlow(skycamKey: String): Either<Failure, Flow<Skycam>>
}