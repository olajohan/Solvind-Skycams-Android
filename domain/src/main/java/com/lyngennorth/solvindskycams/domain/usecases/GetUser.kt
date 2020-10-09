package com.lyngennorth.solvindskycams.domain.usecases

import com.lyngennorth.solvindskycams.domain.domainmodel.User
import com.lyngennorth.solvindskycams.domain.repositories.IUserRepository

class GetUser(private val repo: IUserRepository): UseCase<User, UseCase.None>() {
    override suspend fun run(params: None) = repo.getCurrentUser()

}