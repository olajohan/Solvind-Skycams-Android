package com.solvind.skycams.app.domain.usecases.skycam

import com.solvind.skycams.app.core.UseCaseFlow
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.domain.repo.ISkycamRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class GetSkycamFlowUseCase @Inject constructor(
    private val repo: ISkycamRepo
): UseCaseFlow<Flow<Skycam>, GetSkycamFlowUseCase.Params>() {
    data class Params(val skycamKey: String)

    override fun run(params: Params): Flow<Skycam> = repo.getSkycamFlow(params.skycamKey)



}