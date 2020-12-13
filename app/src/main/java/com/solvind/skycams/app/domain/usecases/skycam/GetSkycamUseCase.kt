package com.solvind.skycams.app.domain.usecases.skycam

import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.domain.repo.ISkycamRepo
import javax.inject.Inject


open class GetSkycamUseCase @Inject constructor(
    private val repo: ISkycamRepo
): UseCase<Skycam, GetSkycamUseCase.Params>() {
    data class Params(val skycamKey: String)

    override suspend fun run(params: Params): Resource<Skycam> = repo.getSkycam(params.skycamKey)
}