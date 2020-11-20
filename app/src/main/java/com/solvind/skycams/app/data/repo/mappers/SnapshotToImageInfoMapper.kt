package com.solvind.skycams.app.data.repo.mappers

import com.google.firebase.firestore.DocumentSnapshot
import com.solvind.skycams.app.core.IMapper
import com.solvind.skycams.app.domain.enums.AuroraPrediction
import com.solvind.skycams.app.domain.model.ImageInfo
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
            prediction = when (left.getString("predictionLabel")) {
                "visibleAurora" -> AuroraPrediction.VisibleAurora(confidence = left.getDouble("predictionConfidence") ?: 0.51)
                "notAurora" -> AuroraPrediction.NotAurora(confidence = left.getDouble("predictionConfidence") ?: 0.51)
                else -> AuroraPrediction.NotPredicted()
            }
        )
    }

    override fun singleFromRightToLeft(right: ImageInfo): DocumentSnapshot {
        TODO("Not yet implemented")
    }
}