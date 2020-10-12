package com.lyngennorth.solvindskycams.skycam.domain.usecases

import com.lyngennorth.solvindskycams.common.usecases.UseCase
import com.lyngennorth.solvindskycams.skycam.domain.model.Skycam
import com.lyngennorth.solvindskycams.skycam.domain.repo.ISkycamRepo
import kotlinx.coroutines.flow.Flow

class GetSkycamFlow(private val repo: ISkycamRepo): UseCase<Flow<Skycam>, GetSkycamFlow.Params>() {
    data class Params(val skycamKey: String)

    override suspend fun run(params: Params) = repo.getSkycamFlow(params.skycamKey)


}