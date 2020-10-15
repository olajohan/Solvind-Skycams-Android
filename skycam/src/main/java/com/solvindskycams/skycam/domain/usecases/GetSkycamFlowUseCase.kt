package com.solvindskycams.skycam.domain.usecases

import com.solvindskycams.common.usecases.UseCaseFlow
import com.solvindskycams.skycam.domain.model.Skycam
import com.solvindskycams.skycam.domain.repo.ISkycamRepo
import javax.inject.Inject

class GetSkycamFlowUseCase @Inject constructor(private val repo: ISkycamRepo): UseCaseFlow<Skycam, GetSkycamFlowUseCase.Params>() {
    data class Params(val skycamKey: String)

    override fun run(params: Params) = repo.getSkycamFlow(params.skycamKey)


}