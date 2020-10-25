package com.solvind.skycams.skycam.domain.usecases

import com.solvind.skycams.skycam.UseCase
import com.solvind.skycams.skycam.domain.model.Skycam
import com.solvind.skycams.skycam.domain.repo.ISkycamRepo
import javax.inject.Inject

class GetAllSkycamsUseCase @Inject constructor(private val repo: ISkycamRepo): UseCase<List<Skycam>, UseCase.None>() {
    override suspend fun run(params: None) = repo.getAllSkycams()
}