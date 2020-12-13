package com.solvind.skycams.app.domain.usecases.skycam

import com.solvind.skycams.app.core.UseCaseFlow
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.domain.repo.ISkycamRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAllSkycamsFlowUseCase @Inject constructor(
    private val mSkycamRepo: ISkycamRepo
) : UseCaseFlow<Flow<Skycam>, UseCaseFlow.None>() {
    override fun run(params: None): Flow<Skycam> = mSkycamRepo.getAllSkycamsFlow()
}