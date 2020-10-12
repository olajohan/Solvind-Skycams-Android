package com.lyngennorth.solvindskycams.auth.domain.usecases

import com.lyngennorth.solvindskycams.auth.domain.model.User
import com.lyngennorth.solvindskycams.auth.domain.repo.IAuthenticatorRepo
import com.lyngennorth.solvindskycams.common.Either
import com.lyngennorth.solvindskycams.common.Failure
import com.lyngennorth.solvindskycams.common.usecases.UseCase
import javax.inject.Inject

class GetCurrentUser @Inject constructor(private val repo: IAuthenticatorRepo) : UseCase<User, UseCase.None>() {
    override suspend fun run(params: None): Either<Failure, User> = repo.getCurrentUser()
}