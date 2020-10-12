package com.lyngennorth.solvindskycams.skycam.domain.usecases

import com.lyngennorth.solvindskycams.common.usecases.UseCase
import com.lyngennorth.solvindskycams.skycam.domain.model.Skycam
import com.lyngennorth.solvindskycams.skycam.domain.repo.ISkycamRepo
import javax.inject.Inject

/*
* When a user opens the application, retrieve all the skycams.
* 1. Request skycamlist from repository
* */
class GetAllSkycams @Inject constructor(private val repo: ISkycamRepo): UseCase<List<Skycam>, UseCase.None>() {
    override suspend fun run(params: None) = repo.getAllSkycams()
}