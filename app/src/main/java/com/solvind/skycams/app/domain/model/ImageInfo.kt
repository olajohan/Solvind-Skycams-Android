package com.solvind.skycams.app.domain.model

import com.solvind.skycams.app.domain.enums.AuroraPrediction


data class ImageInfo(
    val skycamKey: String,
    val imageId: String,
    val storageLocation: String,
    val timestamp: Long,
    val sunElevation: Double,
    val moonPhase: Double,
    val prediction: AuroraPrediction,
)