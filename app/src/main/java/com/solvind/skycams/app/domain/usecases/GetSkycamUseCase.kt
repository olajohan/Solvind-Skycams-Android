package com.solvind.skycams.app.domain.usecases

import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.di.IoDispatcher
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.domain.repo.ISkycamRepo
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject


open class GetSkycamUseCase @Inject constructor(
    private val repo: ISkycamRepo,
    @IoDispatcher dispatcher: CoroutineDispatcher
): UseCase<Skycam, GetSkycamUseCase.Params>(dispatcher) {
    data class Params(val skycamKey: String)

    override suspend fun run(params: Params): Resource<Skycam> = repo.getSkycam(params.skycamKey)
}