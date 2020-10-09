package com.lyngennorth.solvindskycams.domain.usecases

import com.lyngennorth.solvindskycams.domain.domainmodel.SkycamImage
import com.lyngennorth.solvindskycams.domain.repositories.ISkycamImageRepository

class GetSkycamImage(private val repo: ISkycamImageRepository): UseCase<SkycamImage, GetSkycamImage.Params>() {
    data class Params(val imageId: String)
    override suspend fun run(params: Params) = repo.getSkycamImage(params.imageId)
}