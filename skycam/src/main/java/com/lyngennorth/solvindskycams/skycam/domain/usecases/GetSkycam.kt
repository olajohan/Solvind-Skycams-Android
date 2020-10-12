package com.lyngennorth.solvindskycams.skycam.domain.usecases

import com.lyngennorth.solvindskycams.common.Either
import com.lyngennorth.solvindskycams.common.Failure
import com.lyngennorth.solvindskycams.common.usecases.UseCase
import com.lyngennorth.solvindskycams.skycam.domain.model.Skycam
import com.lyngennorth.solvindskycams.skycam.domain.repo.ISkycamRepo
import javax.inject.Inject


class GetSkycam @Inject constructor(private val repo: ISkycamRepo): UseCase<Skycam, GetSkycam.Params>() {
    data class Params(val skycamKey: String)

    override suspend fun run(params: Params): Either<Failure, Skycam> = repo.getSkycam(params.skycamKey)
}