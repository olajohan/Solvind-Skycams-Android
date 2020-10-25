package com.solvind.skycams.skycam.data.repo.mappers

import com.google.firebase.firestore.DocumentSnapshot
import com.solvind.skycams.core.IMapper
import com.solvind.skycams.skycam.domain.enums.AuroraPredictionLabel
import com.solvind.skycams.skycam.domain.model.ImageInfo
import javax.inject.Inject

class SnapshotToImageInfoMapper @Inject constructor() : IMapper<DocumentSnapshot, ImageInfo> {

    override fun singleFromLeftToRight(left: DocumentSnapshot): ImageInfo {
        return ImageInfo(
            skycamKey = left.getString("skycamKey").orEmpty(),
            imageId = left.getString("imageId").orEmpty(),
            storageLocation = left.getString("storageLocation").orEmpty(),
            timestamp = left.getLong("timestamp") ?: 0L,
            sunElevation = left.getDouble("sunElevation") ?: 0.0,
            moonPhase = left.getDouble("moonPhase") ?: 0.0,
            predictionConfidence = left.getDouble("predictionConfidence") ?: 0.0,
            predictionLabel = AuroraPredictionLabel.valueOf(left.getString("predictionLabel") ?: "NOT_PREDICTED")
        )
    }

    override fun singleFromRightToLeft(right: ImageInfo): DocumentSnapshot {
        TODO("Not yet implemented")
    }
}