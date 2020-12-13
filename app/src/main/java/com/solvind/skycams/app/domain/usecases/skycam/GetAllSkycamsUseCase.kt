package com.solvind.skycams.app.domain.usecases.skycam

import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.domain.repo.ISkycamRepo
import javax.inject.Inject

open class GetAllSkycamsUseCase @Inject constructor(
    private val repo: ISkycamRepo
): UseCase<List<Skycam>, UseCase.None>() {

    override suspend fun run(params: None) = repo.getAllSkycams()
}