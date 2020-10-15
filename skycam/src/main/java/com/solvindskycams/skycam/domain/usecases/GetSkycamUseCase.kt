package com.solvindskycams.skycam.domain.usecases

import com.solvindskycams.common.Either
import com.solvindskycams.common.Failure
import com.solvindskycams.common.usecases.UseCase
import com.solvindskycams.skycam.domain.model.Skycam
import com.solvindskycams.skycam.domain.repo.ISkycamRepo
import javax.inject.Inject


class GetSkycamUseCase @Inject constructor(private val repo: ISkycamRepo): UseCase<Skycam, GetSkycamUseCase.Params>() {
    data class Params(val skycamKey: String)

    override suspend fun run(params: Params): Either<Failure, Skycam> = repo.getSkycam(params.skycamKey)
}