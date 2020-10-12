package com.lyngennorth.solvindskycams.skycam.domain.usecases

import com.lyngennorth.solvindskycams.common.usecases.UseCase
import com.lyngennorth.solvindskycams.skycam.domain.model.SkycamImageDetails
import com.lyngennorth.solvindskycams.skycam.domain.repo.ISkycamImageDetailsRepo

class GetSkycamImage(private val detailsRepo: ISkycamImageDetailsRepo): UseCase<SkycamImageDetails, GetSkycamImage.Params>() {
    data class Params(val imageId: String)
    override suspend fun run(params: Params) = detailsRepo.getSkycamImage(params.imageId)
}