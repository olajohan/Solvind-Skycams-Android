package com.lyngennorth.solvindskycams.domain.usecases

import com.lyngennorth.solvindskycams.domain.domainmodel.Skycam
import com.lyngennorth.solvindskycams.domain.repositories.ISkycamRepository
import kotlinx.coroutines.flow.Flow

class GetSkycamFlow(private val repo: ISkycamRepository): UseCase<Flow<Skycam>, GetSkycamFlow.Params>() {
    data class Params(val skycamKey: String)

    override suspend fun run(params: Params) = repo.getSkycamFlow(params.skycamKey)


}