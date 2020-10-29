package com.solvind.skycams.app.domain.usecases

import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.di.IoDispatcher
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.domain.repo.ISkycamRepo
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

open class GetAllSkycamsUseCase @Inject constructor(
    private val repo: ISkycamRepo,
    @IoDispatcher dispatcher: CoroutineDispatcher
): UseCase<List<Skycam>, UseCase.None>(dispatcher) {
    override suspend fun run(params: None) = repo.getAllSkycams()
}