package com.solvind.skycams.app.domain.usecases

import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.di.IoDispatcher
import com.solvind.skycams.app.domain.model.ImageInfo
import com.solvind.skycams.app.domain.repo.IImageInfoRepo
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

open class GetImageInfoUseCase @Inject constructor(
    private val repo: IImageInfoRepo,
    @IoDispatcher dispatcher: CoroutineDispatcher
): UseCase<ImageInfo, GetImageInfoUseCase.Params>(dispatcher) {
    data class Params(val imageId: String)
    override suspend fun run(params: Params) : Resource<ImageInfo> {
        if (params.imageId.isEmpty()) return Resource.Error(Failure.EmptyImageIdFailure)
        return repo.getSkycamImageInfo(params.imageId)
    }
}