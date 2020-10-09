package com.lyngennorth.solvindskycams.domain.repositories

import com.lyngennorth.solvindskycams.domain.Either
import com.lyngennorth.solvindskycams.domain.Failure
import com.lyngennorth.solvindskycams.domain.domainmodel.User
import java.lang.Exception

interface IUserRepository {
    suspend fun getCurrentUser(): Either<Failure, User>
    suspend fun getAlarmAvailableUntilEpochSeconds(): Either<Failure, Long>
    suspend fun updateAlarmAvailableUntilEpochSeconds(plusSeconds: Long): Either<Failure, Unit>
    suspend fun followSkycam(skycamKey: String): Either<Failure, Unit>
    suspend fun unFollowSkycam(skycamKey: String): Either<Failure, Unit>
}