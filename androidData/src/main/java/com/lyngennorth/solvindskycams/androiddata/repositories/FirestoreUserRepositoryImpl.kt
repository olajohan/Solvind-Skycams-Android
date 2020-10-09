package com.lyngennorth.solvindskycams.androiddata.repositories

import com.lyngennorth.solvindskycams.domain.Either
import com.lyngennorth.solvindskycams.domain.Failure
import com.lyngennorth.solvindskycams.domain.domainmodel.User
import com.lyngennorth.solvindskycams.domain.repositories.IUserRepository
import java.lang.Exception
import javax.inject.Inject

class FirestoreUserRepositoryImpl @Inject constructor() : IUserRepository {
    override suspend fun getCurrentUser(): Either<Failure, User> {
        TODO("Not yet implemented")
    }

    override suspend fun getAlarmAvailableUntilEpochSeconds(): Either<Failure, Long> {
        TODO("Not yet implemented")
    }

    override suspend fun updateAlarmAvailableUntilEpochSeconds(plusSeconds: Long): Either<Failure, Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun followSkycam(skycamKey: String): Either<Failure, Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun unFollowSkycam(skycamKey: String): Either<Failure, Unit> {
        TODO("Not yet implemented")
    }
}