package com.solvind.skycams.app.domain.usecases.image

import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.domain.model.ImageInfo
import com.solvind.skycams.app.domain.repo.IImageInfoRepo
import javax.inject.Inject

open class GetImageInfoUseCase @Inject constructor(
    private val repo: IImageInfoRepo
): UseCase<ImageInfo, GetImageInfoUseCase.Params>() {
    data class Params(val imageId: String)
    override suspend fun run(params: Params) : Resource<ImageInfo> {
        if (params.imageId.isEmpty()) return Resource.Error(Failure.EmptyImageIdFailure)
        return repo.getSkycamImageInfo(params.imageId)
    }
}