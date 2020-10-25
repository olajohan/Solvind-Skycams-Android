package com.solvind.skycams.skycam.domain.usecases

import com.solvind.skycams.skycam.Failure
import com.solvind.skycams.skycam.Result
import com.solvind.skycams.skycam.UseCase
import com.solvind.skycams.skycam.domain.model.ImageInfo
import com.solvind.skycams.skycam.domain.repo.IImageInfoRepo
import javax.inject.Inject

class GetImageInfoUseCase @Inject constructor(private val repo: IImageInfoRepo): UseCase<ImageInfo, GetImageInfoUseCase.Params>() {
    data class Params(val imageId: String)
    override suspend fun run(params: Params) : Result<ImageInfo> {
        if (params.imageId.isEmpty()) return Result.Error(Failure.EmptyImageIdFailure)
        return repo.getSkycamImageInfo(params.imageId)
    }
}