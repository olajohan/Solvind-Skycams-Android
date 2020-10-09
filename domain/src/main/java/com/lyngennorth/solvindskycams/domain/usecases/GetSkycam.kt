package com.lyngennorth.solvindskycams.domain.usecases

import com.lyngennorth.solvindskycams.domain.Either
import com.lyngennorth.solvindskycams.domain.Failure
import com.lyngennorth.solvindskycams.domain.domainmodel.Skycam
import com.lyngennorth.solvindskycams.domain.repositories.ISkycamRepository
import javax.inject.Inject


class GetSkycam @Inject constructor(private val repo: ISkycamRepository): UseCase<Skycam, GetSkycam.Params>() {
    data class Params(val skycamKey: String)

    override suspend fun run(params: Params): Either<Failure, Skycam> = repo.getSkycam(params.skycamKey)
}