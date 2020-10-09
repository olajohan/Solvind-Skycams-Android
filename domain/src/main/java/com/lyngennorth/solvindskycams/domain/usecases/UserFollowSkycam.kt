package com.lyngennorth.solvindskycams.domain.usecases

import com.lyngennorth.solvindskycams.domain.repositories.IUserRepository
import javax.inject.Inject

class UserFollowSkycam @Inject constructor(private val repo: IUserRepository): UseCase<Unit, UserFollowSkycam.Params>() {

    data class Params(val skycamKey: String)
    override suspend fun run(params: Params) = repo.followSkycam(params.skycamKey)
}