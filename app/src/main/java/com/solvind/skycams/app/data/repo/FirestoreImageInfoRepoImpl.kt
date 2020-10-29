package com.solvind.skycams.app.data.repo

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.data.repo.mappers.SnapshotToImageInfoMapper
import com.solvind.skycams.app.domain.model.ImageInfo
import com.solvind.skycams.app.domain.repo.IImageInfoRepo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val IMAGES_COLLECTION_GROUP = "images"

class FirestoreImageInfoRepoImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val mapper: SnapshotToImageInfoMapper
) : IImageInfoRepo {
    override suspend fun getSkycamImageInfo(imageName: String): Resource<ImageInfo> {
        val result = firestore.collectionGroup(IMAGES_COLLECTION_GROUP).whereEqualTo("imageId", imageName).get()
        val collection = result.await()
        if (collection.isEmpty) return Resource.Error(Failure.ImageNotFoundFailure)
        return Resource.Success(mapper.singleFromLeftToRight(collection.first()))
    }

    override suspend fun getMostRecentImagesInfo(
        numberOfImages: Long,
        skycamKey: String
    ): Resource<List<ImageInfo>> {
        val result = firestore
            .collectionGroup(IMAGES_COLLECTION_GROUP)
            .whereEqualTo("skycamKey", skycamKey)
            .limit(numberOfImages)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
        val collection = result.await()

        if (collection.isEmpty) return Resource.Error(Failure.ImageNotFoundFailure)
        return Resource.Success(mapper.listFromLeftToRight(collection.documents))
    }

}