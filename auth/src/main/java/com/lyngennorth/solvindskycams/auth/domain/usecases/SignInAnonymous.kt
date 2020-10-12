package com.lyngennorth.solvindskycams.auth.domain.usecases

import com.lyngennorth.solvindskycams.auth.domain.repo.IAuthenticatorRepo
import com.lyngennorth.solvindskycams.common.Either
import com.lyngennorth.solvindskycams.common.Failure
import com.lyngennorth.solvindskycams.common.usecases.UseCase
import javax.inject.Inject

class SignInAnonymous @Inject constructor(private val repo: IAuthenticatorRepo): UseCase<Unit, UseCase.None>() {
    override suspend fun run(params: None): Either<Failure, Unit> = repo.signInAnonymously()

}