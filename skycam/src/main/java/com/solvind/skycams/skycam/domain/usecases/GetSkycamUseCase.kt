package com.solvind.skycams.skycam.domain.usecases

import com.solvind.skycams.skycam.Result
import com.solvind.skycams.skycam.UseCase
import com.solvind.skycams.skycam.domain.model.Skycam
import com.solvind.skycams.skycam.domain.repo.ISkycamRepo
import javax.inject.Inject


class GetSkycamUseCase @Inject constructor(private val repo: ISkycamRepo): UseCase<Skycam, GetSkycamUseCase.Params>() {
    data class Params(val skycamKey: String)

    override suspend fun run(params: Params): Result<Skycam> = repo.getSkycam(params.skycamKey)
}