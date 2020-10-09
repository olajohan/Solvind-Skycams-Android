package com.lyngennorth.solvindskycams.domain.usecases

import com.lyngennorth.solvindskycams.domain.domainmodel.Skycam
import com.lyngennorth.solvindskycams.domain.repositories.ISkycamRepository
import javax.inject.Inject

/*
* When a user opens the application, retrieve all the skycams.
* 1. Request skycamlist from repository
* */
class GetAllSkycams @Inject constructor(private val repo: ISkycamRepository): UseCase<List<Skycam>, UseCase.None>() {
    override suspend fun run(params: None) = repo.getAllSkycams()
}