package com.solvind.skycams.skycam.domain.repo

import com.solvind.skycams.skycam.Result
import com.solvind.skycams.skycam.domain.model.ImageInfo

interface IImageInfoRepo {
    suspend fun getSkycamImageInfo(imageName: String): Result<ImageInfo>
    suspend fun getMostRecentImagesInfo(numberOfImages: Long, skycamKey: String): Result<List<ImageInfo>>
}