package com.solvindskycams.skycam

import com.solvindskycams.skycam.domain.enums.AuroraObservedState
import com.solvindskycams.skycam.domain.enums.AuroraPredictionLabel
import com.solvindskycams.skycam.domain.model.*


fun getSkycam(
    skycamKey: String = "lyngennorth",
    mainImage: String = "lyngennorth-main.jpg",
    location: SkycamLocation = getSkycamLocation(),
    mostRecentImageName: String = "lyngennorth-1601433250.jpg"
) = Skycam(skycamKey, mainImage, location, mostRecentImageName)

fun getSkycamLocation() = SkycamLocation(
    coordinates = SkycamLocationCoordinates(
        latitude = 69.7626,
        longitude = 20.4649
    ),
    mas = 23,
    name = "Lyngen North",
    region = "Norway",
    timezone = "Europe/Oslo"
)

fun getSkycamImage() = SkycamImageDetails(
    skycamKey = "lyngennorth",
    imageId= "lyngennorth-1602018569.jpg",
    timestamp = 123456789L,
    sunElevation = -5.0,
    moonPhase = 10.0,
    predictionConfidence = 1.0,
    predictionLabel = AuroraPredictionLabel.NOT_PREDICTED,
    votes = null
)

fun getSkycamImageUserVote(
    uid: String = "3eLt8xSOD5ZVs4MmYQpt06cqyyF2",
    imageId: String = getSkycamImage().imageId,
    auroraObservedState: AuroraObservedState = AuroraObservedState.VISIBLE_AURORA
) = SkycamImageDetailsUserVote(
    uid,
    imageId,
    auroraObservedState
)