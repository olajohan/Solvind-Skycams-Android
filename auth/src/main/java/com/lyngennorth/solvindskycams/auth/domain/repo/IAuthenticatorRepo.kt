package com.lyngennorth.solvindskycams.auth.domain.repo

import com.lyngennorth.solvindskycams.auth.domain.model.User
import com.lyngennorth.solvindskycams.common.Either
import com.lyngennorth.solvindskycams.common.Failure

interface IAuthenticatorRepo {

    suspend fun signInAnonymously(): Either<Failure, Unit>
    suspend fun signOut(): Either<Failure, Unit>
    suspend fun getCurrentUser(): Either<Failure, User>
}