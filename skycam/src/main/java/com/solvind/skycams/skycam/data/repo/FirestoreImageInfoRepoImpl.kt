package com.solvind.skycams.skycam.data.repo

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.solvind.skycams.skycam.Failure
import com.solvind.skycams.skycam.Result
import com.solvind.skycams.skycam.data.repo.mappers.SnapshotToImageInfoMapper
import com.solvind.skycams.skycam.domain.model.ImageInfo
import com.solvind.skycams.skycam.domain.repo.IImageInfoRepo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val IMAGES_COLLECTION_GROUP = "images"

class FirestoreImageInfoRepoImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val mapper: SnapshotToImageInfoMapper
) : IImageInfoRepo {
    override suspend fun getSkycamImageInfo(imageName: String): Result<ImageInfo> {
        val result = firestore.collectionGroup(IMAGES_COLLECTION_GROUP).whereEqualTo("imageId", imageName).get()
        val collection = result.await()
        if (collection.isEmpty) return Result.Error(Failure.ImageNotFoundFailure)
        return Result.Success(mapper.singleFromLeftToRight(collection.first()))
    }

    override suspend fun getMostRecentImagesInfo(
        numberOfImages: Long,
        skycamKey: String
    ): Result<List<ImageInfo>> {
        val result = firestore
            .collectionGroup(IMAGES_COLLECTION_GROUP)
            .whereEqualTo("skycamKey", skycamKey)
            .limit(numberOfImages)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
        val collection = result.await()

        if (collection.isEmpty) return Result.Error(Failure.ImageNotFoundFailure)
        return Result.Success(mapper.listFromLeftToRight(collection.documents))
    }

}