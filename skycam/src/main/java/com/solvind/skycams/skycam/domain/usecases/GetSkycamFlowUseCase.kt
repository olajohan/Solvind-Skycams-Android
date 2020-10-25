package com.solvind.skycams.skycam.domain.usecases

import com.solvind.skycams.skycam.UseCaseFlow
import com.solvind.skycams.skycam.domain.model.Skycam
import com.solvind.skycams.skycam.domain.repo.ISkycamRepo
import javax.inject.Inject

class GetSkycamFlowUseCase @Inject constructor(private val repo: ISkycamRepo): UseCaseFlow<Skycam, GetSkycamFlowUseCase.Params>() {
    data class Params(val skycamKey: String)

    override fun run(params: Params) = repo.getSkycamFlow(params.skycamKey)


}