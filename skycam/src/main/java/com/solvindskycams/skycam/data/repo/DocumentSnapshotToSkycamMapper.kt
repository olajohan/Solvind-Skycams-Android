package com.solvindskycams.skycam.data.repo

import com.google.firebase.firestore.DocumentSnapshot
import com.solvindskycams.common.IMapper
import com.solvindskycams.skycam.domain.model.Skycam
import com.solvindskycams.skycam.domain.model.SkycamLocation
import com.solvindskycams.skycam.domain.model.SkycamLocationCoordinates
import javax.inject.Inject

class DocumentSnapshotToSkycamMapper @Inject constructor() : IMapper<DocumentSnapshot, Skycam> {

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
        mostRecentImageId = left.getString("mostRecentImageId").orEmpty()
    )

    override fun singleFromRightToLeft(right: Skycam): DocumentSnapshot {
        TODO("Not yet implemented")
    }

}