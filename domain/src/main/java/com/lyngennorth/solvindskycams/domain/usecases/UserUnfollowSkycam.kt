package com.lyngennorth.solvindskycams.domain.usecases

import com.lyngennorth.solvindskycams.domain.repositories.IUserRepository

class UserUnfollowSkycam(private val repo: IUserRepository): UseCase<Unit, UserUnfollowSkycam.Params>() {
    data class Params(val skycamKey: String)
    override suspend fun run(params: Params) = repo.unFollowSkycam(params.skycamKey)
}