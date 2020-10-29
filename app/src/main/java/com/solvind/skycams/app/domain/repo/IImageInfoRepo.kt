package com.solvind.skycams.app.domain.repo

import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.domain.model.ImageInfo

interface IImageInfoRepo {
    suspend fun getSkycamImageInfo(imageName: String): Resource<ImageInfo>
    suspend fun getMostRecentImagesInfo(numberOfImages: Long, skycamKey: String): Resource<List<ImageInfo>>
}