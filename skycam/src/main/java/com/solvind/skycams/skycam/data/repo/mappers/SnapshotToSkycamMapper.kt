package com.solvind.skycams.skycam.data.repo.mappers

import com.google.firebase.firestore.DocumentSnapshot
import com.solvind.skycams.core.IMapper
import com.solvind.skycams.skycam.domain.enums.AuroraPredictionLabel
import com.solvind.skycams.skycam.domain.model.Skycam
import com.solvind.skycams.skycam.domain.model.ImageInfo
import com.solvind.skycams.skycam.domain.model.SkycamLocation
import com.solvind.skycams.skycam.domain.model.SkycamLocationCoordinates
import javax.inject.Inject

class SnapshotToSkycamMapper @Inject constructor() : IMapper<DocumentSnapshot, Skycam> {

    override fun listFromLeftToRight(left: List<DocumentSnapshot>): List<Skycam> = left.map { singleFromLeftToRight(it) }
    override fun listFromRightToLeft(right: List<Skycam>): List<DocumentSnapshot> = right.map { singleFromRightToLeft(it) }

    override fun singleFromLeftToRight(left: DocumentSnapshot): Skycam = Skycam(
        skycamKey = left.getString("skycamKey").orEmpty(),
        mainImage = left.getString("mainImage").orEmpty(),
        location = SkycamLocation(
            name = left.getString("location.name").orEmpty(),
            coordinates = SkycamLocationCoordinates(
                latitude = left.getGeoPoint("location.coordinates")?.latitude ?: 0.0,
                longitude = left.getGeoPoint("location.coordinates")?.longitude ?: 0.0
            ),
            mas = left.getLong("mas")?.toInt() ?: 0,
            region = left.getString("region").orEmpty(),
            timezone = left.getString("timezone").orEmpty()
        ),
        mostRecentImage = ImageInfo(
            skycamKey = left.getString("mostRecentImage.skycamKey").orEmpty(),
            imageId = left.getString("mostRecentImage.imageId").orEmpty(),
            storageLocation = left.getString("mostRecentImage.storageLocation").orEmpty(),
            timestamp = left.getLong("mostRecentImage.timestamp") ?: 0L,
            sunElevation = left.getDouble("mostRecentImage.sunElevation") ?: 0.0,
            moonPhase = left.getDouble("mostRecentImage.moonPhase") ?: 0.0,
            predictionConfidence = left.getDouble("mostRecentImage.predictionConfidence") ?: 0.0,
            predictionLabel = left.get("mostRecentImage.predictionLabel") as? AuroraPredictionLabel ?: AuroraPredictionLabel.NOT_PREDICTED
        )
    )

    override fun singleFromRightToLeft(right: Skycam): DocumentSnapshot {
        TODO("Not yet implemented")
    }

}