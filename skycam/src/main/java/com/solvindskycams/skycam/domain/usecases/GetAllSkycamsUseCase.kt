package com.solvindskycams.skycam.domain.usecases

import com.solvindskycams.common.usecases.UseCase
import com.solvindskycams.skycam.domain.model.Skycam
import com.solvindskycams.skycam.domain.repo.ISkycamRepo
import javax.inject.Inject

/*
* When a user opens the application, retrieve all the skycams.
* 1. Request skycamlist from repository
* */
class GetAllSkycamsUseCase @Inject constructor(private val repo: ISkycamRepo): UseCase<List<Skycam>, UseCase.None>() {
    override suspend fun run(params: None) = repo.getAllSkycams()
}