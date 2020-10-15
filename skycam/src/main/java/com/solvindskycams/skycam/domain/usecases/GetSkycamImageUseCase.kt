package com.solvindskycams.skycam.domain.usecases

import com.solvindskycams.common.usecases.UseCase
import com.solvindskycams.skycam.domain.model.SkycamImageDetails
import com.solvindskycams.skycam.domain.repo.ISkycamImageDetailsRepo

class GetSkycamImageUseCase(private val detailsRepo: ISkycamImageDetailsRepo): UseCase<SkycamImageDetails, GetSkycamImageUseCase.Params>() {
    data class Params(val imageId: String)
    override suspend fun run(params: Params) = detailsRepo.getSkycamImage(params.imageId)
}